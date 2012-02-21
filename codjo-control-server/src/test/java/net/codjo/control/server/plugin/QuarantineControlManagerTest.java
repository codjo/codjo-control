package net.codjo.control.server.plugin;
import net.codjo.aspect.Aspect;
import net.codjo.aspect.AspectContext;
import net.codjo.aspect.AspectException;
import net.codjo.aspect.AspectManager;
import net.codjo.aspect.JoinPoint;
import net.codjo.control.common.ControlContext;
import net.codjo.control.common.ControlException;
import net.codjo.control.common.IntegrationPlan;
import net.codjo.control.common.message.ControlJobRequest;
import net.codjo.control.server.api.ControlAspectContext;
import net.codjo.database.common.api.DatabaseFactory;
import net.codjo.database.common.api.JdbcFixture;
import net.codjo.test.common.LogString;
import net.codjo.test.common.mock.ConnectionMock;
import java.sql.Connection;
import java.sql.SQLException;
import junit.framework.TestCase;
public class QuarantineControlManagerTest extends TestCase {
    private static final String CONTROL_TABLE = "CTRL_TABLE";
    private static final String QUARANTINE_TABLE = "Q_VL";
    private JdbcFixture jdbc;
    private LogString log = new LogString();
    private Connection connection = new ConnectionMock().getStub();
    private QuarantineControlManager controlManager;
    private IntegrationPlanMock planMock;
    private AspectManager manager;


    @Override
    protected void setUp() throws Exception {
        jdbc = new DatabaseFactory().createJdbcFixture();
        manager = new AspectManager();
        planMock = new IntegrationPlanMock();

        ControlPreference controlPreference = new ControlPreference(null, manager) {
            @Override
            public IntegrationPlan getPlan(String quarantineTable) {
                return planMock;
            }
        };
        controlManager = new QuarantineControlManager(controlPreference);
        jdbc.doSetUp();
    }


    @Override
    protected void tearDown() throws Exception {
        jdbc.doTearDown();
    }


    public void test_execute() throws Exception {
        createTable(QUARANTINE_TABLE);
        jdbc.executeUpdate("insert into " + QUARANTINE_TABLE + " values (1, 0, null)");
        jdbc.executeUpdate("insert into " + QUARANTINE_TABLE + " values (2, 0, null)");
        jdbc.executeUpdate("insert into " + QUARANTINE_TABLE + " values (3, 0, null)");

        planMock.mockExecuteBatchControls("update " + CONTROL_TABLE + " set ERROR_TYPE=5 where Q_ID=2");

        ControlJobRequest jobRequest = new ControlJobRequest(QUARANTINE_TABLE);
        jobRequest.setInitiatorLogin("user");
        jobRequest.setId("control-2");
        jobRequest.addPath("p1");

        PostControlAudit postControlAudit =
              controlManager.doQuarantineControls(jdbc.getConnection(), jobRequest);

        jdbc.assertContent(net.codjo.database.common.api.structure.SqlTable.table(QUARANTINE_TABLE),
                           new String[][]{
                                 {"1", "0", null}
                                 , {"2", "5", null}
                                 , {"3", "0", null}
                           });

        log.assertContent("executeShipmemt()"
                          + ", executeBatchControls(context:user/control-2)"
                          + ", executeDispatch()");

        assertNotNull(postControlAudit);
        assertEquals(2, postControlAudit.getValidLineCount());
        assertEquals(1, postControlAudit.getBadLineCount());
    }


    public void test_execute_technicalError() throws Exception {
        createTable(QUARANTINE_TABLE);
        jdbc.executeUpdate("insert into " + QUARANTINE_TABLE + " values (1, 0, null)");

        planMock.mockExecuteBatchControlsFailure(new SQLException("bad sql statement"));
        ControlJobRequest jobRequest = new ControlJobRequest(QUARANTINE_TABLE);
        jobRequest.setInitiatorLogin("user");
        jobRequest.setId("control-2");
        jobRequest.addPath("p1");

        try {
            controlManager.doQuarantineControls(jdbc.getConnection(), jobRequest);
            fail();
        }
        catch (QuarantineControlException ex) {
            assertEquals("java.sql.SQLException: bad sql statement", ex.getMessage());
            assertNotNull(ex.getCause());
        }

        jdbc.assertContent(net.codjo.database.common.api.structure.SqlTable.table(QUARANTINE_TABLE),
                           new String[][]{
                                 {"1", "499", "ERREUR TECHNIQUE : bad sql statement"}
                           });
    }


    public void test_executeDispatch_aspect() throws Exception {
        // Aspect
        manager.addAspect("aspectBefore",
                          newJoinPoint(JoinPoint.CALL_BEFORE, "control.dispatch", QUARANTINE_TABLE),
                          FakeBeforeAspect.class);

        // Appel
        controlManager.executeDispatch(planMock, createContext("user", "myCurrentRequestId"));

        // Verification de l'appel
        assertEquals("setUp, run, cleanUp", FakeBeforeAspect.executeList.toString());

        // Verification du contexte
        ControlAspectContext context = new ControlAspectContext(FakeBeforeAspect.aspectCtxt);
        assertNotNull(FakeBeforeAspect.aspectCtxt);
        assertSame(connection, context.getConnection());
        assertEquals("user", context.getUser());
        assertEquals(CONTROL_TABLE, context.getControlTableName());
        assertEquals("myCurrentRequestId", context.getJobRequestId());
    }


    public void test_getTechnicalErrorMessage() throws Exception {
        assertEquals("ERREUR TECHNIQUE : Erreur",
                     controlManager.getTechnicalErrorMessage(new RuntimeException("Erreur")));
        assertEquals(254,
                     controlManager
                           .getTechnicalErrorMessage(new RuntimeException(new String(new char[600])))
                           .length());
    }


    public void test_getTechnicalErrorMessage_null() throws Exception {
        assertEquals("ERREUR TECHNIQUE : java.lang.RuntimeException",
                     controlManager.getTechnicalErrorMessage(new RuntimeException((String)null)));
    }


    private ControlAspectContext createContext(String user, String requestId) {
        ControlAspectContext context = new ControlAspectContext();
        context.setUser(user);
        context.setJobRequestId(requestId);
        context.setConnection(connection);
        context.setControlTableName(CONTROL_TABLE);
        context.setQuarantineTable(QUARANTINE_TABLE);
        return context;
    }


    private JoinPoint[] newJoinPoint(int call, String point, String argument) {
        JoinPoint joinPoint = new JoinPoint();
        joinPoint.setArgument(argument);
        joinPoint.setPoint(point);
        joinPoint.setCall(call);
        return new JoinPoint[]{joinPoint};
    }


    private void createTable(String tableName) throws SQLException {
        jdbc.create(net.codjo.database.common.api.structure.SqlTable.table(tableName), "Q_ID int not null, "
                                                                                     + "ERROR_TYPE int null, "
                                                                                     + "ERROR_LOG varchar(255) null");
    }


    public static class FakeBeforeAspect implements Aspect {
        /**
         * @noinspection StaticNonFinalField
         */
        private static StringBuffer executeList = null;

        /**
         * @noinspection StaticNonFinalField
         */
        private static AspectContext aspectCtxt = null;


        public void setUp(AspectContext context, JoinPoint joinPoint)
              throws AspectException {
            executeList = new StringBuffer();
            executeList.append("setUp");
            aspectCtxt = context;
        }


        public void run(AspectContext context) throws AspectException {
            executeList.append(", run");
        }


        public void cleanUp(AspectContext context)
              throws AspectException {
            executeList.append(", cleanUp");
        }
    }

    private class IntegrationPlanMock extends IntegrationPlan {
        private SQLException batchControlsFatalFailure;
        private String batchControlQuery;


        @Override
        public void executeShipmemt(final Connection connection) throws SQLException {
            log.call("executeShipmemt");
            createTable(CONTROL_TABLE);
            connection.createStatement().executeUpdate("insert into " + CONTROL_TABLE +
                                                       " select * from " + QUARANTINE_TABLE);
        }


        @Override
        public void executeDispatch(Connection connection, ControlContext context) throws SQLException {
            log.call("executeDispatch");
            if (QuarantineControlManagerTest.this.connection == connection) {
                return;
            }
            // NB :  HSQLDB ne supporte pas les requetes "update ... from ... inner"
            connection.createStatement().executeUpdate("update " + QUARANTINE_TABLE
                                                       + " set ERROR_TYPE = ( "
                                                       + "select ERROR_TYPE from " + CONTROL_TABLE
                                                       + " where " + CONTROL_TABLE + ".Q_ID = "
                                                       + QUARANTINE_TABLE + ".Q_ID)");
        }


        @Override
        public void executeBatchControls(final Connection connection, final ControlContext context)
              throws SQLException, ControlException {
            log.call("executeBatchControls",
                     "context:" + context.getUser() + "/" + context.getCurrentRequestId());
            if (batchControlQuery != null) {
                connection.createStatement().executeUpdate(batchControlQuery);
            }
            if (batchControlsFatalFailure != null) {
                batchControlsFatalFailure.fillInStackTrace();
                throw batchControlsFatalFailure;
            }
        }


        @Override
        public String getControlTableName() {
            return CONTROL_TABLE;
        }


        @Override
        public String getQuarantineTable() {
            return QUARANTINE_TABLE;
        }


        public void mockExecuteBatchControlsFailure(SQLException failure) {
            batchControlsFatalFailure = failure;
        }


        public void mockExecuteBatchControls(String query) {
            batchControlQuery = query;
        }
    }
}
