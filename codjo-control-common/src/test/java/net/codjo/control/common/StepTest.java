/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.easymock.MockControl;
/**
 * Description of the Class
 *
 * @author $Author: galaber $
 * @version $Revision: 1.5 $
 */
public class StepTest extends TestCase {
    private MockControl connectionControl;
    private Dictionary dico;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private MockControl statementControl;
    private Step step;
    private ControlContext context;


    /**
     * Verification pour le cas d'une requete simple sans parametres ou variable (en mode SQL).
     *
     * @throws Exception
     */
    public void testSQLexecute() throws Exception {
        String query = "update table set FIELD='1' where FIELD !=1";
        mockCreatePrepareStatement(query);
        mockCleanup();
        activateMock();

        step.setQuery(new Query(query));
        step.execute(mockConnection, dico, "", false, null);

        verifyMock();
    }


    /**
     * Verification pour le cas d'un control java.
     *
     * @throws Exception
     */
    public void testJAVAexecute() throws Exception {
        SingletonFakeControl.singleton = null;

        step.setControlClass(SingletonFakeControl.class.getName());
        step.setErrorCode(5);
        step.execute("object to be tested", dico, context);

        assertNotNull(SingletonFakeControl.singleton);
        assertEquals("object to be tested", SingletonFakeControl.singleton.getControledObject());
        assertEquals(5, SingletonFakeControl.singleton.getErrorCode());
        assertEquals(context, SingletonFakeControl.singleton.getContext());
    }


    /**
     * Verification pour le cas d'une requete simple sans parametres ou variable (en mode SQL), avec un
     * warning.
     *
     * @throws Exception
     */
    public void testSQLexecuteWarning() throws Exception {
        String query = "update table set FIELD='1' where FIELD !=1";
        mockCreatePrepareStatement(query);
        mockStatement.getWarnings();
        statementControl.setReturnValue(new SQLWarning("grr", "010P4"), 1);
        mockStatement.close();
        statementControl.setVoidCallable(1);

        activateMock();

        step.setQuery(new Query(query));
        try {
            step.execute(mockConnection, dico, "", false, null);
            fail("Car un warning");
        }
        catch (SQLException ex) {
            ;
        }

        verifyMock();
    }


    /**
     * Verification pour le cas d'une requete simple sans parametres ou variable (en mode SQL), avec un
     * warning ignoré.
     *
     * @throws Exception
     */
    public void testSQLexecuteWarningIgnored()
          throws Exception {
        String queryStr = "update table set FIELD='1' where FIELD !=1";
        mockCreatePrepareStatement(queryStr);
        mockStatement.getWarnings();
        statementControl.setReturnValue(new SQLWarning("grr", "010P4"), 1);
        mockStatement.close();
        statementControl.setVoidCallable(1);

        activateMock();

        Query query = new Query(queryStr);
        query.setIgnoreWarningCode("010P4");
        step.setQuery(query);
        step.execute(mockConnection, dico, "", false, null);

        verifyMock();
    }


    /**
     * Verification pour le cas d'une requete simple avec parametres et avec variable (en mode SQL).
     *
     * @throws Exception
     */
    public void testSQLexecuteParametres() throws Exception {
        String query = "update TABLE set FIELD=? where FIELD !=1";
        mockCreatePrepareStatement(query);
        mockStatement.setString(1, "toto");
        mockStatement.setInt(2, 5);
        mockCleanup();
        activateMock();

        step.addParameter(new Parameter(2, "int", "5"));
        step.addParameter(new Parameter(1, "string", "toto"));
        step.setQuery(new Query("update $table$ set FIELD=? where FIELD !=1"));
        dico.addVariable("table", "TABLE");

        step.execute(mockConnection, dico, "", false, null);

        verifyMock();
    }


    /**
     * Verification pour le cas d'une requete simple avec parametres et avec la variable automatique (en mode
     * SQL).
     *
     * @throws Exception
     */
    public void testSQLexecuteParametresErrorCode()
          throws Exception {
        String query = "update TABLE set FIELD=? where FIELD !=1";
        mockCreatePrepareStatement(query);
        mockStatement.setString(1, "toto");
        mockStatement.setInt(2, 5);
        mockCleanup();
        activateMock();

        step.setErrorCode(5);
        step.addParameter(new Parameter(2, "int", "$" + Step.ERROR_CODE + "$"));
        step.addParameter(new Parameter(1, "string", "toto"));
        step.setQuery(new Query("update $table$ set FIELD=? where FIELD !=1"));
        dico.addVariable("table", "TABLE");

        step.execute(mockConnection, dico, "", false, null);

        verifyMock();
    }


    /**
     * Verification pour le cas d'une requete utilisant une table temporaire (en mode SQL).
     *
     * @throws Exception
     */
    public void testSQLexecuteTemporaryTable()
          throws Exception {
        String sql = "update xx";
        mockCreatePrepareStatement(sql);
        mockCreateStatement("drop table #TEMP");
        mockStatement.close();
        statementControl.setVoidCallable(1);
        mockCleanup();
        activateMock();

        Query query = new Query(sql);
        query.setTemporaryTable("#TEMP");
        step.setQuery(query);
        step.execute(mockConnection, dico, "", false, null);

        verifyMock();
    }


    /**
     * Verification pour le cas d'une requete simple sans parametres mais avec variable (en mode SQL).
     *
     * @throws Exception
     */
    public void testSQLexecuteVariable() throws Exception {
        String query = "update TABLE set FIELD='1' where FIELD !=1";
        mockCreatePrepareStatement(query);
        mockCleanup();
        activateMock();

        step.setQuery(new Query("update $table$ set FIELD='1' where FIELD !=1"));
        dico.addVariable("table", "TABLE");
        step.execute(mockConnection, dico, "", false, null);

        verifyMock();
    }


    public void test_isStepFor() {
        step.setStepFor(null);
        assertTrue(step.isStepFor(Step.FOR_ALL));
        assertTrue(step.isStepFor(Step.FOR_USER));
        assertTrue(step.isStepFor(Step.FOR_USER_ADD));
        assertTrue(step.isStepFor(Step.FOR_USER_UPDATE));
        assertTrue(step.isStepFor(Step.FOR_BATCH));

        step.setStepFor(Step.FOR_ALL);
        assertTrue(step.isStepFor(Step.FOR_ALL));
        assertTrue(step.isStepFor(Step.FOR_USER));
        assertTrue(step.isStepFor(Step.FOR_USER_ADD));
        assertTrue(step.isStepFor(Step.FOR_USER_UPDATE));
        assertTrue(step.isStepFor(Step.FOR_BATCH));

        step.setStepFor(Step.FOR_USER);
        assertTrue(!step.isStepFor(Step.FOR_ALL));
        assertTrue(step.isStepFor(Step.FOR_USER));
        assertTrue(step.isStepFor(Step.FOR_USER_ADD));
        assertTrue(step.isStepFor(Step.FOR_USER_UPDATE));
        assertTrue(!step.isStepFor(Step.FOR_BATCH));

        step.setStepFor(Step.FOR_BATCH);
        assertTrue(!step.isStepFor(Step.FOR_ALL));
        assertTrue(!step.isStepFor(Step.FOR_USER));
        assertTrue(!step.isStepFor(Step.FOR_USER_ADD));
        assertTrue(!step.isStepFor(Step.FOR_USER_UPDATE));
        assertTrue(step.isStepFor(Step.FOR_BATCH));

        step.setStepFor(Step.FOR_USER_ADD);
        assertTrue(!step.isStepFor(Step.FOR_ALL));
        assertTrue(step.isStepFor(Step.FOR_USER));
        assertTrue(step.isStepFor(Step.FOR_USER_ADD));
        assertTrue(!step.isStepFor(Step.FOR_USER_UPDATE));
        assertTrue(!step.isStepFor(Step.FOR_BATCH));

        step.setStepFor(Step.FOR_USER_UPDATE);
        assertTrue(!step.isStepFor(Step.FOR_ALL));
        assertTrue(step.isStepFor(Step.FOR_USER));
        assertTrue(!step.isStepFor(Step.FOR_USER_ADD));
        assertTrue(step.isStepFor(Step.FOR_USER_UPDATE));
        assertTrue(!step.isStepFor(Step.FOR_BATCH));

        step.setStepFor("import");
        assertFalse(step.isStepFor("transferq"));
        assertFalse(step.isStepFor("batch.transferq"));
        assertTrue(step.isStepFor("import"));
        assertFalse(step.isStepFor("batch.import"));
        assertFalse(step.isStepFor("test/t2/batch.import"));
        assertTrue(step.isStepFor("test/t2/import"));
        assertTrue(step.isStepFor("test/t2/import/ui/ol"));
        assertFalse(step.isStepFor("test/t2/batch.import.spe1"));
        step.setStepFor("batch");
        assertTrue(step.isStepFor("test/t2/batch/import.spe1"));

    }


    @Override
    protected void setUp() {
        step = new Step();
        dico = new Dictionary();
        context = new ControlContext("user", "prevId",null);
        initMockStuff();
    }


    @Override
    protected void tearDown() {
    }


    private void activateMock() {
        connectionControl.replay();
        statementControl.replay();
    }


    private void initMockStuff() {
        connectionControl = MockControl.createControl(Connection.class);
        mockConnection = (Connection)connectionControl.getMock();
        statementControl = MockControl.createControl(PreparedStatement.class);
        mockStatement = (PreparedStatement)statementControl.getMock();
    }


    private void mockCleanup() throws SQLException {
        mockStatement.getWarnings();
        statementControl.setReturnValue(null, 1);
        mockStatement.close();
        statementControl.setVoidCallable(1);
    }


    private void mockCreatePrepareStatement(final String query)
          throws SQLException {
        mockConnection.prepareStatement(query);
        connectionControl.setReturnValue(mockStatement, 1);
        mockStatement.executeUpdate();
        statementControl.setReturnValue(5, 1);
    }


    private void mockCreateStatement(final String query)
          throws SQLException {
        mockConnection.createStatement();
        connectionControl.setReturnValue(mockStatement, 1);
        mockStatement.executeUpdate(query);
        statementControl.setReturnValue(5, 1);
    }


    private void verifyMock() throws AssertionFailedError {
        connectionControl.verify();
        statementControl.verify();
    }


    static class SingletonFakeControl extends FakeControl {
        public static SingletonFakeControl singleton;


        SingletonFakeControl() {
            singleton = this;
        }
    }
}
