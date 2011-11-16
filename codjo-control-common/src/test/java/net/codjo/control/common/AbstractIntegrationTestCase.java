package net.codjo.control.common;
import net.codjo.control.common.loader.XmlMapperHelper;
import net.codjo.tokio.TokioFixture;
import java.sql.Connection;
import java.sql.SQLException;
import junit.framework.TestCase;
/**
 *
 */
public abstract class AbstractIntegrationTestCase extends TestCase {
    private IntegrationPlan integrationPlan;
    private TokioFixture tokioFixture = new TokioFixture(getClass());
    private boolean isInitialized = false;


    protected AbstractIntegrationTestCase(String integrationPlanId) {
        loadIntegrationPlan(integrationPlanId);
    }


    protected void assertControlExecution(String stepId,
                                          String storyId,
                                          boolean deleteBeforeInsert) throws SQLException, ControlException {
        assertTrue(verifyStep(integrationPlan.getStep(stepId), storyId, deleteBeforeInsert));
    }


    protected void assertDispatchExecution(String stepId, String storyId, boolean deleteBeforeInsert)
          throws SQLException, ControlException {
        Step step = getIntegrationPlan().getDispatch().getStep(stepId);
        assertTrue(verifyStep(step, storyId, deleteBeforeInsert));
    }


    protected void assertControlDeleteExecution(String stepId, String storyId, boolean deleteBeforeInsert)
          throws SQLException, ControlException {
        assertTrue(verifyStep(getStepDelete(stepId), storyId, deleteBeforeInsert));
    }


    protected void assertAllControlOk(String storyName, boolean deleteBeforeInsert)
          throws Exception {
        getTokioFixture().insertInputInDb(storyName, deleteBeforeInsert);

        IntegrationPlan ctrl = getIntegrationPlan();
        Connection connection = getTokioFixture().getConnection();
        ctrl.init(connection);
        ctrl.executeShipmemt(connection);
        ctrl.executeBatchControls(connection, null);
        ctrl.executeDispatch(connection, null);

        getTokioFixture().assertAllOutputs(storyName);
    }


    protected void assertAllControlOk(String storyName) throws Exception {
        assertAllControlOk(storyName, true);
    }


    protected IntegrationPlan getIntegrationPlan() {
        return integrationPlan;
    }


    protected TokioFixture getTokioFixture() {
        return tokioFixture;
    }


    protected Connection getConnection() {
        return getTokioFixture().getConnection();
    }


    protected void initIntegrationPlan() throws SQLException {
        if (!isInitialized) {
            getIntegrationPlan().init(getConnection());
            isInitialized = true;
        }
    }


    @Override
    protected final void setUp() throws Exception {
        super.setUp();
        tokioFixture.doSetUp();
        try {
            doSetUp();
        }
        catch (Exception e) {
            tokioFixture.doTearDown();
            throw e;
        }
    }


    protected void doSetUp() throws Exception {
    }


    @Override
    protected final void tearDown() throws Exception {
        super.tearDown();
        tokioFixture.doTearDown();
        doTearDown();
    }


    protected void doTearDown() throws Exception {
    }


    private Step getStepDelete(String stepId) {
        for (Plan plan : integrationPlan.getPlanListForDelete().getPlans()) {
            for (Step step : plan.getSteps()) {
                if (step.getId().equals(stepId)) {
                    return step;
                }
            }
        }
        throw new IllegalArgumentException("Step Delete" + stepId + " is unknown");
    }


    private boolean verifyStep(Step step, String storyId, boolean deleteBeforeInsert)
          throws SQLException, ControlException {
        initIntegrationPlan();

        getTokioFixture().insertInputInDb(storyId, deleteBeforeInsert);

        executeStep(step);

        return getTokioFixture().getJDBCScenario(storyId).verifyAllOutputs(getConnection());
    }


    private void executeStep(Step step) throws SQLException, ControlException {
        step.execute(getConnection(),
                     integrationPlan.getDictionary(),
                     integrationPlan.getControlTableName(),
                     true,
                     new ControlContext());
    }


    private void loadIntegrationPlan(String integrationPlanId) {
        try {
            integrationPlan = XmlMapperHelper.loadPlan(integrationPlanId, this.getClass());
        }
        catch (Exception cause) {
            IllegalArgumentException error =
                  new IllegalArgumentException("Imposssible de charger le plan d'intégration '"
                                               + integrationPlanId + "'");
            error.initCause(cause);
            throw error;
        }
    }
}
