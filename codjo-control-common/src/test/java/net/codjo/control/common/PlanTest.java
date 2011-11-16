/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.easymock.MockControl;
/**
 * Description of the Class
 *
 * @author $Author: gonnot $
 * @version $Revision: 1.7 $
 */
public class PlanTest extends TestCase {
    private Dictionary dico;
    private Connection mockConnection;
    private Plan plan;
    private ControlContext context;
    Map<Step, StepAudit> stepAudit;


    /**
     * Verifie l'ordre d'execution des Step (par priorité ascendante) en mode JAVA.
     *
     * @throws Exception
     */
    public void test_executeJAVA_StepOrder() throws Exception {
        FakeStep step2 = new FakeStep(2, Step.FOR_BATCH);
        FakeStep step3 = new FakeStep(3, Step.FOR_USER);
        FakeStep step1 = new FakeStep(1, Step.FOR_BATCH);

        plan.setType("sql");

        plan.addStep(step2);
        plan.addStep(step3);
        plan.addStep(step1);

        plan.setType(Plan.JAVA_TYPE);

        final Object obj = new Object();
        plan.executeJAVA(obj, dico, context, Step.FOR_BATCH, stepAudit);

        assertTrue(step1.called);
        assertTrue(step2.called);
        assertTrue(!step3.called);
        assertTrue(step1.time < step2.time);

        // Verification des arguments
        assertEquals(context, step1.ctxt);
        assertEquals(context, step2.ctxt);
        assertEquals(dico, step1.dico);
        assertEquals(dico, step2.dico);
        assertEquals(obj, step1.obj);
        assertEquals(obj, step2.obj);

        assertEquals(3, stepAudit.size());
        assertStepAudit(step1, 1, 0);
        assertStepAudit(step2, 1, 0);
        assertStepAudit(step3, 0, 1);
    }


    /**
     * Verifie que si une étape en mode JAVA échoue, on arrête le processus.
     */
    public void test_executeJAVA_failure() {
        FakeStep step2 = new FakeStep(2, Step.FOR_BATCH);
        FakeStep step1 = new FakeStep(1, new ControlException(0, ""));

        plan.addStep(step2);
        plan.addStep(step1);

        plan.setType(Plan.JAVA_TYPE);

        try {
            Map<Step, StepAudit> stepAudit = new HashMap<Step, StepAudit>();
            plan.executeJAVA(new Object(), dico, context, Step.FOR_BATCH, stepAudit);
            fail("Une étape doit échouer");
        }
        catch (ControlException e) {
            ;
        }

        assertTrue(step1.called);
        assertTrue(!step2.called);
    }


    public void test_hasStepFor() {
        FakeStep step2 = new FakeStep(2, Step.FOR_BATCH);

        plan.addStep(step2);

        plan.setType(Plan.JAVA_TYPE);

        assertEquals(true, plan.hasStepFor(Step.FOR_BATCH));
        assertEquals(false, plan.hasStepFor(Step.FOR_ALL));
        assertEquals(false, plan.hasStepFor(Step.FOR_USER));
    }


    /**
     * Verifie que si le plan n'est pas en mode JAVA, l'appel a executeJAVA echoue.
     *
     * @throws Exception
     */
    public void test_executeJAVA_failure_badMode()
          throws Exception {
        FakeStep step2 = new FakeStep(2, Step.FOR_BATCH);

        plan.addStep(step2);
        plan.setType("sql");
        try {
            plan.executeJAVA(new Object(), dico, context, Step.FOR_BATCH, stepAudit);
            fail("Echec car plan n'est pas de type JAVA");
        }
        catch (IllegalArgumentException e) {
            ;
        }

        assertTrue(!step2.called);
    }


    /**
     * Verifie l'ordre d'execution des Step (par priorité ascendante) en mode SQL.
     *
     * @throws SQLException
     * @throws ControlException
     */
    public void test_executeSQL_StepOrder() throws SQLException, ControlException {
        FakeStep step2 = new FakeStep(2, Step.FOR_BATCH);
        FakeStep step3 = new FakeStep(3, Step.FOR_USER);
        FakeStep step1 = new FakeStep(1, Step.FOR_BATCH);

        plan.addStep(step2);
        plan.addStep(step3);
        plan.addStep(step1);

        plan.executeMASS(mockConnection, dico, null, Step.FOR_BATCH, null, stepAudit);

        assertTrue(step1.called);
        assertTrue(step2.called);
        assertTrue(!step3.called);
        assertTrue(step1.time < step2.time);
    }


    /**
     * Verifie l'ordre d'execution des Step (par priorité ascendante) en simulant Castor.
     *
     * @throws SQLException
     * @throws ControlException
     */
    public void test_executeSQL_StepOrder_Castor()
          throws SQLException, ControlException {
        FakeStep step2 = new FakeStep(2, Step.FOR_BATCH);
        FakeStep step3 = new FakeStep(3, Step.FOR_USER);
        FakeStep step1 = new FakeStep(1, Step.FOR_BATCH);

        plan.setSteps(new ArrayList<Step>());
        plan.getSteps().add(step2);
        plan.getSteps().add(step3);
        plan.getSteps().add(step1);

        plan.executeMASS(mockConnection, dico, null, Step.FOR_BATCH, null, stepAudit);

        assertTrue(step1.called);
        assertTrue(step2.called);
        assertTrue(!step3.called);
        assertTrue(step1.time < step2.time);
    }


    /**
     * Verifie q'on peux pas ajouter 2 steps avec la meme priority.
     */
    public void test_executeSQL_StepOrder_SamePriority() {
        FakeStep step2 = new FakeStep(2, Step.FOR_BATCH);
        FakeStep step1 = new FakeStep(2, Step.FOR_BATCH);

        plan.setSteps(new ArrayList<Step>());
        plan.getSteps().add(step2);
        try {
            plan.getSteps().add(step1);
            fail("Impossible d'avoir 2 fois la meme priority");
        }
        catch (Exception ex) {
            ;
        }
    }


    /**
     * Verifie que si une étape en mode SQL échoue, on arrête le processus.
     *
     * @throws ControlException
     */
    public void test_executeSQL_failure() throws ControlException {
        FakeStep step2 = new FakeStep(2, Step.FOR_BATCH);
        FakeStep step1 = new FakeStep(1, new SQLException());

        plan.addStep(step2);
        plan.addStep(step1);

        try {
            plan.executeMASS(mockConnection, dico, null, Step.FOR_BATCH, null, stepAudit);
            fail("Une étape doit échouer");
        }
        catch (SQLException e) {
            ;
        }

        assertTrue(step1.called);
        assertTrue(!step2.called);
    }


    /**
     * Verifie que si le plan n'est pas en mode SQL, l'appel a executeSQL echoue.
     *
     * @throws SQLException
     * @throws ControlException
     */
    public void test_executeSQL_failure_badMode()
          throws SQLException, ControlException {
        FakeStep step2 = new FakeStep(2, Step.FOR_BATCH);

        plan.addStep(step2);
        plan.setType(Plan.JAVA_TYPE);
        try {
            plan.executeMASS(mockConnection, dico, null, Step.FOR_BATCH, null, stepAudit);
            fail("Echec car plan de type JAVA");
        }
        catch (IllegalArgumentException e) {
            ;
        }

        assertTrue(!step2.called);
    }


    private void assertStepAudit(FakeStep step, int okRunningCount, int notOkRunningCount) {
        assertEquals(okRunningCount, stepAudit.get(step).getOkRunningCount());
        assertEquals(notOkRunningCount, stepAudit.get(step).getNotOkRunningCount());
    }


    @Override
    protected void setUp() {
        initMockStuff();
        plan = new Plan();
        dico = new Dictionary();
        context = new ControlContext("user", "prevId", null);
        stepAudit = new HashMap<Step, StepAudit>();
    }


    @Override
    protected void tearDown() {
    }


    private void initMockStuff() {
        MockControl control = MockControl.createControl(Connection.class);
        mockConnection = (Connection)control.getMock();
    }


    private class FakeStep extends Step {
        private boolean called = false;
        private long time = 0;
        private ControlContext ctxt;
        private Object obj;
        private Dictionary dico;
        private Exception error;


        FakeStep(int priority, String stepFor) {
            setPriority(priority);
            setId("" + priority);
            setStepFor(stepFor);
        }


        FakeStep(int priority, Exception error) {
            setPriority(priority);
            setId("" + priority);
            setStepFor(FOR_ALL);
            this.error = error;
        }


        @Override
        public void execute(Object object, Dictionary dictionary, ControlContext context)
              throws ControlException {
            called = true;
            this.ctxt = context;
            this.dico = dictionary;
            this.obj = object;
            time = System.currentTimeMillis();
            if (error != null) {
                throw (ControlException)error;
            }
            try {
                Thread.sleep(100);
            }
            catch (Exception e) {
                ;
            }
        }


        @Override
        public void execute(Connection con, Dictionary dictionary,
                            String controlTableName, boolean mass, ControlContext context)
              throws SQLException {
            called = true;
            time = System.currentTimeMillis();
            if (error != null) {
                throw (SQLException)error;
            }
            try {
                Thread.sleep(100);
            }
            catch (Exception e) {
                ;
            }
        }
    }
}
