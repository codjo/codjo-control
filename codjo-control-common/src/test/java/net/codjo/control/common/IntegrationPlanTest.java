/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Map;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import net.codjo.control.common.i18n.InternationalizationFixture;
import net.codjo.control.common.message.SourceOfData;
import net.codjo.control.common.util.EntityHelper;
import net.codjo.control.common.util.EntityIterator;
import net.codjo.control.common.util.EntityResultState;
import org.easymock.MockControl;
/**
 * DOCUMENT ME!
 *
 * @author $Author: torrent $
 * @version $Revision: 1.7 $
 */
public class IntegrationPlanTest extends TestCase {
    private InternationalizationFixture i18nFixture = new InternationalizationFixture();
    private MockControl connectionControl;
    private Dictionary dico;
    private MockPlan dispatch;
    private MockEntityHelper entityHelper;
    private IntegrationPlan integrationPlan;
    private EntityIterator iterator;
    private MockControl iteratorControl;
    private Connection mockConnection;
    private Statement mockStatement;
    private MockPlan planJava;
    private MockPlan planSQL;
    private MockShipment shipment;
    private MockControl statementControl;
    private MockEntity mockEntity;
    private ControlContext context;


    /**
     * Verifie la création de la table temporaire des controles.
     */
    public void test_createControlTable() throws Exception {
        mockCreateTemporaryTable();
        activateMock();

        integrationPlan.createControlTable(mockConnection);

        verifyMock();
    }


    public void test_proceedDeletedEntity_Java() throws Exception {
        integrationPlan.getPlanListForDelete().getPlans().add(planJava);

        integrationPlan.proceedDeletedEntity(mockConnection, new Object(), context);

        assertTrue("Pas d'insertion car seulement un plan Java",
                   !entityHelper.insertObjectCalled);
        assertTrue("Pas de creation de entityHelper",
                   !mockEntity.getEntityHelperCalled
                   && !mockEntity.getEntityHelperForBatchCalled);

        assertTrue("Pas de load", !entityHelper.updateObjectCalled);
        assertTrue("Plan java est appelé", planJava.executeJAVACalled);
        assertTrue("Pas de shipment", !shipment.called);
        assertTrue("Pas de dispatch", !dispatch.executeSQLCalled);
    }


    public void test_proceedDeletedEntity_NoStep()
          throws Exception {
        integrationPlan.setPlanListForDelete(null);

        integrationPlan.proceedDeletedEntity(mockConnection, new Object(), context);

        assertTrue("Pas d'insertion car seulement un plan Java",
                   !entityHelper.insertObjectCalled);
        assertTrue("Pas de creation de entityHelper",
                   !mockEntity.getEntityHelperCalled
                   && !mockEntity.getEntityHelperForBatchCalled);

        assertTrue("Pas de load", !entityHelper.updateObjectCalled);
        assertTrue("Pas de shipment", !shipment.called);
        assertTrue("Pas de dispatch", !dispatch.executeSQLCalled);
    }


    /**
     * Verifie que le traitement d'un plan Java marche.
     */
    public void test_proceedEntity_Java() throws Exception {
        integrationPlan.getPlanList().getPlans().add(planJava);

        integrationPlan.proceedNewEntity(mockConnection, new Object(), context);

        assertTrue("Pas d'insertion car seulement un plan Java",
                   !entityHelper.insertObjectCalled);
        assertTrue("Pas de creation de entityHelper",
                   !mockEntity.getEntityHelperCalled
                   && !mockEntity.getEntityHelperForBatchCalled);
        assertTrue("Pas de load", !entityHelper.updateObjectCalled);
        assertTrue("Plan java est appelé", planJava.executeJAVACalled);
        assertControlContext();
        assertTrue("Pas de shipment", !shipment.called);
        assertTrue("Pas de dispatch", !dispatch.executeSQLCalled);
    }


    /**
     * Verifie que le traitement d'un plan Java marche.
     */
    public void test_proceedEntity_Java_nostep_for_user()
          throws Exception {
        integrationPlan.getPlanList().getPlans().add(planJava);
        planJava.hasStepForReturn = false;

        integrationPlan.proceedNewEntity(mockConnection, new Object(), context);

        assertTrue("Pas d'insertion car pas de Step", !entityHelper.insertObjectCalled);
        assertTrue("Pas de creation de entityHelper",
                   !mockEntity.getEntityHelperCalled
                   && !mockEntity.getEntityHelperForBatchCalled);
        assertTrue("Pas de load", !entityHelper.updateObjectCalled);
        assertTrue("Plan java n'est pas appelé", !planJava.executeJAVACalled);
        assertTrue("Pas de shipment", !shipment.called);
        assertTrue("Pas de dispatch", !dispatch.executeSQLCalled);
    }


    /**
     * Verifie que le traitement passe correctement d'un plan Java a un plan SQL.
     */
    public void test_proceedEntity_Java_to_SQL() throws Exception {
        integrationPlan.getPlanList().getPlans().add(planJava);
        integrationPlan.getPlanList().getPlans().add(planSQL);
        mockCreateTemporaryTable();
        mockDropTemporaryTable();
        activateMock();
        dico.setNow(Timestamp.valueOf("1999-12-10 10:00:00.0"));

        integrationPlan.proceedNewEntity(mockConnection, new Object(), context);

        verifyMock();
        assertTrue(entityHelper.insertObjectCalled);
        assertTrue(entityHelper.updateObjectCalled);
        assertTrue("Creation de entityHelper de type bean",
                   mockEntity.getEntityHelperCalled && !mockEntity.getEntityHelperForBatchCalled);
        assertTrue(planSQL.executeSQLCalled);
        assertTrue(planJava.executeJAVACalled);
        assertTrue(!shipment.called);
        assertTrue(!dispatch.executeSQLCalled);
    }


    /**
     * Verifie que le traitement passe correctement d'un plan Java a un plan SQL.
     */
    public void test_proceedEntity_SQL_Java() throws Exception {
        integrationPlan.getPlanList().getPlans().add(planSQL);
        integrationPlan.getPlanList().getPlans().add(planJava);
        mockCreateTemporaryTable();
        mockDropTemporaryTable();
        activateMock();
        dico.setNow(Timestamp.valueOf("1999-12-10 10:00:00.0"));

        integrationPlan.proceedNewEntity(mockConnection, new Object(), context);

        verifyMock();
        assertTrue("Creation de entityHelper de type bean",
                   mockEntity.getEntityHelperCalled && !mockEntity.getEntityHelperForBatchCalled);
        assertTrue(entityHelper.insertObjectCalled);
        assertTrue(planSQL.executeSQLCalled);
        assertTrue(entityHelper.updateObjectCalled);
        assertTrue(planJava.executeJAVACalled);
        assertTrue(entityHelper.insertObjectCalled);
        assertTrue(planSQL.executeSQLCalled);

        assertTrue(!shipment.called);
        assertTrue(!dispatch.executeSQLCalled);
    }


    /**
     * Bug : duplicate key. Si l'integration possede 2 plans SQL, l'objet integrationPlan tente d'inserer 2 fois le bean
     * à controler.
     */
    public void test_proceedEntity_SQL_SQL() throws Exception {
        MockPlan planSQLbis = new MockPlan();

        integrationPlan.getPlanList().getPlans().add(planSQL);
        integrationPlan.getPlanList().getPlans().add(planSQLbis);
        mockCreateTemporaryTable();
        mockDropTemporaryTable();
        activateMock();

        dico.setNow(Timestamp.valueOf("1999-12-10 10:00:00.0"));
        integrationPlan.proceedNewEntity(mockConnection, new Object(), context);

        verifyMock();
        assertTrue(planSQL.executeSQLCalled);
        assertTrue(planSQLbis.executeSQLCalled);
        assertTrue(entityHelper.insertObjectCalled);
        assertEquals(1, entityHelper.insertObjectNbcalled);
        assertTrue(entityHelper.updateTableCalled);
        assertEquals(1, entityHelper.updateTableNbcalled);
    }


    public void test_proceedDeletedEntity_SQL_SQL()
          throws Exception {
        MockPlan planSQLbis = new MockPlan();

        integrationPlan.getPlanListForDelete().getPlans().add(planSQL);
        integrationPlan.getPlanListForDelete().getPlans().add(planSQLbis);
        mockCreateTemporaryTable();
        mockDropTemporaryTable();
        activateMock();

        dico.setNow(Timestamp.valueOf("1999-12-10 10:00:00.0"));
        integrationPlan.proceedDeletedEntity(mockConnection, new Object(), context);

        verifyMock();
        assertTrue(planSQL.executeSQLCalled);
        assertTrue(planSQLbis.executeSQLCalled);
        assertTrue(entityHelper.insertObjectCalled);
        assertEquals(1, entityHelper.insertObjectNbcalled);
        assertTrue(entityHelper.updateObjectCalled);
        assertEquals(1, entityHelper.updateTableNbcalled);
    }


    /**
     * Verifie le traitement de la table des Quarantaine en mode java.
     */
    public void test_proceedQuarantine_java() throws Exception {
        integrationPlan.getPlanList().getPlans().add(planJava);
        Object obj = new Object();

        mockCreateTemporaryTable();

        iterator.hasNext();
        iteratorControl.setReturnValue(true, 1);

        iterator.next();
        iteratorControl.setReturnValue(obj, 1);

        iterator.update(obj, null);
        iteratorControl.setVoidCallable(1);

        iterator.hasNext();
        iteratorControl.setReturnValue(false, 1);

        iterator.close();
        iteratorControl.setVoidCallable(1);

        mockDropTemporaryTable();
        activateMock();
        dico.setNow(Timestamp.valueOf("1999-12-10 10:00:00.0"));

        integrationPlan.proceedQuarantine(mockConnection, context);

        verifyMock();
        assertTrue("Creation de entityHelper de type batch",
                   !mockEntity.getEntityHelperCalled && mockEntity.getEntityHelperForBatchCalled);
        assertTrue(planJava.executeJAVACalled);
        assertTrue(shipment.called);
        assertTrue(dispatch.executeSQLCalled);
        assertEquals(new Timestamp(System.currentTimeMillis()).toString().substring(0, 10),
                     dico.getNow().toString().substring(0, 10));
        assertControlContext();
    }


    /**
     * Verifie le traitement de la table des Quarantaine.
     */
    public void test_proceedQuarantine_sql() throws Exception {
        integrationPlan.getPlanList().getPlans().add(planSQL);

        mockCreateTemporaryTable();
        mockDropTemporaryTable();
        activateMock();
        dico.setNow(Timestamp.valueOf("1999-12-10 10:00:00.0"));

        integrationPlan.proceedQuarantine(mockConnection, context);

        verifyMock();
        assertTrue("Pas de creation de entityHelper car pas de plan JAVA",
                   !mockEntity.getEntityHelperCalled
                   && !mockEntity.getEntityHelperForBatchCalled);
        assertTrue(planSQL.executeSQLCalled);
        assertEquals(SourceOfData.IMPORT, planSQL.stepFor);
        assertTrue(shipment.called);
        assertTrue(dispatch.executeSQLCalled);
        assertEquals(new Timestamp(System.currentTimeMillis()).toString().substring(0, 10),
                     dico.getNow().toString().substring(0, 10));
    }


    /**
     * The JUnit setup method
     */
    @Override
    protected void setUp() throws Exception {
        i18nFixture.doSetUp();

        dico = new Dictionary();
        context = new ControlContext("user", "prevId", SourceOfData.IMPORT);
        planSQL = new MockPlan();
        planJava = new MockPlan();
        planJava.setType(Plan.JAVA_TYPE);
        entityHelper = new MockEntityHelper();
        dispatch = new MockPlan();
        shipment = new MockShipment();
        shipment.setTo("TABLE");
        mockEntity = new MockEntity();

        integrationPlan = new IntegrationPlan();
        integrationPlan.setPlanList(new PlansList());
        integrationPlan.setPlanListForDelete(new PlansList());
        integrationPlan.setDispatch(dispatch);
        integrationPlan.setDictionary(dico);
        integrationPlan.setShipment(shipment);
        integrationPlan.setEntity(mockEntity);

        initMockStuff();
    }


    /**
     * The teardown method for JUnit
     */
    @Override
    protected void tearDown() throws Exception {
        i18nFixture.doTearDown();
    }


    /**
     * Description of the Method
     */
    private void activateMock() {
        connectionControl.replay();
        statementControl.replay();
        iteratorControl.replay();
    }


    /**
     * Description of the Method
     */
    private void initMockStuff() {
        connectionControl = MockControl.createControl(Connection.class);
        mockConnection = (Connection)connectionControl.getMock();
        statementControl = MockControl.createControl(Statement.class);
        mockStatement = (Statement)statementControl.getMock();
        iteratorControl = MockControl.createControl(EntityIterator.class);
        iterator = (EntityIterator)iteratorControl.getMock();
    }


    /**
     * Description of the Method
     *
     * @throws SQLException Description of the Exception
     */
    private void mockCleanup() throws SQLException {
        mockStatement.getWarnings();
        statementControl.setReturnValue(null, 1);
        mockStatement.close();
        statementControl.setVoidCallable(1);
    }


    /**
     * Description of the Method
     *
     * @param query Description of the Parameter
     *
     * @throws SQLException Description of the Exception
     */
    private void mockCreateStatement(final String query)
          throws SQLException {
        mockConnection.createStatement();
        connectionControl.setReturnValue(mockStatement, 1);
        mockStatement.executeUpdate(query);
        statementControl.setReturnValue(5, 1);
    }


    /**
     * Description of the Method
     *
     * @throws SQLException Description of the Exception
     */
    private void mockCreateTemporaryTable() throws SQLException {
//        mockDropTemporaryTable();
        mockCreateStatement("create TABLE (...)");
        mockCleanup();
        integrationPlan.setControlTableDef("create $table$ (...)");
        dico.addVariable("table", "TABLE");
    }


    /**
     * Description of the Method
     *
     * @throws SQLException Description of the Exception
     */
    private void mockDropTemporaryTable() throws SQLException {
        mockCreateStatement("drop table TABLE");
        mockStatement.close();
        statementControl.setVoidCallable(1);
    }


    /**
     * Description of the Method
     *
     * @throws AssertionFailedError Description of the Exception
     */
    private void verifyMock() throws AssertionFailedError {
        connectionControl.verify();
        statementControl.verify();
        iteratorControl.verify();
    }


    private void assertControlContext() {
        assertNotNull("Plan java est appelé avec context", planJava.ctxt);
        assertEquals("Plan java est appelé avec context immutable",
                     ImmutableControlContext.class, planJava.ctxt.getClass());
        assertEquals("Plan java est appelé avec context immutable (user)",
                     context.getUser(), planJava.ctxt.getUser());
        assertEquals("Plan java est appelé avec context immutable (user)",
                     context.getCurrentRequestId(), planJava.ctxt.getCurrentRequestId());
    }


    /**
     * Description of the Class
     *
     * @author BULTEZ
     * @version $Revision: 1.7 $
     */
    private static class MockPlan extends Plan {
        private boolean executeJAVACalled = false;
        private boolean executeSQLCalled = false;
        private boolean hasStepForReturn = true;
        private String stepFor = null;
        private ControlContext ctxt;


        @Override
        public void executeJAVA(Object obj,
                                Dictionary dico,
                                ControlContext context,
                                String pathOfRequest,
                                Map<Step, StepAudit> stepAuditMap) throws ControlException {
            executeJAVACalled = true;
            this.ctxt = context;
        }


        @Override
        public void executeMASS(Connection con,
                                Dictionary dico,
                                ControlContext context,
                                String stepForParam,
                                String pathOfRequest,
                                Map<Step, StepAudit> stepAuditMap) throws SQLException, ControlException {
            executeSQLCalled = true;
            this.stepFor = stepForParam;
        }


        @Override
        public boolean hasStepFor(String stepForParam) {
            return hasStepForReturn;
        }
    }

    /**
     * Description of the Class
     *
     * @author BULTEZ
     * @version $Revision: 1.7 $
     */
    private static class MockShipment extends Shipment {
        private boolean called = false;


        @Override
        public void execute(Connection con, Dictionary dico)
              throws SQLException {
            called = true;
        }
    }

    private class MockEntity extends Entity {
        private boolean getEntityHelperCalled = false;
        private boolean getEntityHelperForBatchCalled = false;


        @Override
        public EntityHelper getEntityHelper() {
            getEntityHelperCalled = true;
            return entityHelper;
        }


        @Override
        public EntityHelper getEntityHelperForBatch() {
            getEntityHelperForBatchCalled = true;
            return entityHelper;
        }
    }

    /**
     * Description of the Class
     *
     * @author BULTEZ
     * @version $Revision: 1.7 $
     */
    private class MockEntityHelper extends EntityHelper {
        private boolean insertObjectCalled = false;
        private int insertObjectNbcalled = 0;
        private boolean updateObjectCalled = false;
        private boolean updateTableCalled = false;
        private int updateTableNbcalled = 0;


        @Override
        public void updateTable(Connection con, Object vo, String tableName) {
            updateTableCalled = true;
            updateTableNbcalled++;
        }


        @Override
        public void insertIntoTable(Connection con, Object bean, String tableName) {
            insertObjectCalled = true;
            insertObjectNbcalled++;
        }


        @Override
        public EntityIterator iterator(Connection con, String tableName) {
            return iterator;
        }


        @Override
        public EntityResultState updateObject(Connection con, Object bean,
                                              String tableName) {
            updateObjectCalled = true;
            return new ControlException(EntityResultState.NO_ERROR, "");
        }
    }
}
