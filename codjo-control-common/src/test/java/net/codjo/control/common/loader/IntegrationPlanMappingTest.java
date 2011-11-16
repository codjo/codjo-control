/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.loader;
import net.codjo.control.common.Dictionary;
import net.codjo.control.common.Entity;
import net.codjo.control.common.IntegrationPlan;
import net.codjo.control.common.Plan;
import net.codjo.control.common.PlansList;
import net.codjo.control.common.Shipment;
import net.codjo.control.common.Step;
import net.codjo.control.common.Variable;
import java.util.Collection;
import java.util.Iterator;
import junit.framework.TestCase;
/**
 * DOCUMENT ME!
 *
 * @author $Author: blazart $
 * @version $Revision: 1.1 $
 */
public class IntegrationPlanMappingTest extends TestCase {
    public IntegrationPlanMappingTest(String testCaseName) {
        super(testCaseName);
    }

    public void test_mapping() throws Exception {
        IntegrationPlan ctrl =
            (IntegrationPlan)XmlMapperHelper.loadObject("../common/loader/IntegrationPlanTest.xml",
                IntegrationPlan.class, "IntegrationPlanRules.xml");

        // ################# TEST Integration Plan ########################
        assertEquals("vl_integration_plan", ctrl.getId());
        assertEquals("Plan d'intégration des VL", ctrl.getDescription());
        assertEquals("create table  $control.table$ (...)", ctrl.getControlTableDef());

        // ################# TEST DISPATCH ########################
        assertEquals(1, ctrl.getDispatch().getSteps().size());
        Step dispatchStep = (Step)ctrl.getDispatch().getSteps().toArray()[0];
        assertEquals("UpdateQuarantine", dispatchStep.getId());
        assertEquals("update $control.table$ set $bad.line$ where SEQUENCE = 'QT'",
            dispatchStep.getQuery().getSql());

        // ################# TEST DICTIONARY ########################
        assertEquals(3, ctrl.getDictionary().getVariables().size());
        Variable var = (Variable)ctrl.getDictionary().getVariables().toArray()[0];
        assertEquals("control.table", var.getName());
        assertEquals("#CTRL_TMP_VL", var.getValue());

        // ################# TEST ENTITY ########################
        Entity entity = ctrl.getEntity();
        assertEquals("net.codjo.pims.data.BatchDividend", entity.getBatchClassName());

        // ################# TEST SHIPMENT ########################
        Shipment shipment = ctrl.getShipment();
        assertEquals("$quarantine$", shipment.getFrom());
        assertEquals("QUARANTINE_ID", shipment.getFromPk());

        // ################# TEST PLANS ########################
        PlansList plansList = ctrl.getPlanList();
        assertEquals(1, plansList.getPlans().size());
        Plan plan1 = (Plan)plansList.getPlans().toArray()[0];
        Step step1 = (Step)plan1.getSteps().toArray()[0];
        assertEquals("FirstControl", step1.getId());

        // ################# TEST STEPS ########################
        Collection steps = plan1.getSteps();
        assertEquals(4, steps.size());
        Iterator stepIt = steps.iterator();
        stepIt.next();
        stepIt.next();
        stepIt.next();
        Step step4 = (Step)stepIt.next();
        Dictionary step4Dico = step4.getDictionary();
        assertEquals(2, step4Dico.getVariables().size());
        Variable step4Var = step4Dico.getVariable("toto");
        assertEquals("toto", step4Var.getName());
        assertEquals("titi", step4Var.getValue());
        step4Var = step4Dico.getVariable("tata");
        assertEquals("tata", step4Var.getName());
        assertEquals("tutu", step4Var.getValue());

        // ################# TEST PLANS DELETE ########################
        PlansList plansListforDel = ctrl.getPlanListForDelete();
        assertEquals(1, plansListforDel.getPlans().size());
        Plan plan2 = (Plan)plansListforDel.getPlans().toArray()[0];
        Step step2 = (Step)plan2.getSteps().toArray()[0];
        assertEquals("DeleteControl", step2.getId());
    }


    public void test_mapping_subPlan() throws Exception {
        IntegrationPlan ctrl =
            (IntegrationPlan)XmlMapperHelper.loadObject("../common/loader/MixedIntegrationPlanTest.xml",
                IntegrationPlan.class, "IntegrationPlanRules.xml");
        assertEquals(2, ctrl.getPlanList().getPlans().size());
    }
}
