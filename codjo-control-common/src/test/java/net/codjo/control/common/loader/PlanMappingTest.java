/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.loader;
import net.codjo.control.common.Parameter;
import net.codjo.control.common.Plan;
import net.codjo.control.common.Step;
import java.util.Iterator;
import junit.framework.TestCase;
/**
 * DOCUMENT ME!
 *
 * @author $Author: blazart $
 * @version $Revision: 1.1 $
 */
public class PlanMappingTest extends TestCase {
    public PlanMappingTest(String testCaseName) {
        super(testCaseName);
    }

    public void test_mapping() throws Exception {
        Plan ctrl =
            (Plan)net.codjo.control.common.loader.XmlMapperHelper.loadObject("../common/loader/PlanTest.xml",
                Plan.class, "PlanRules.xml");
        assertEquals(2, ctrl.getSteps().size());
        Iterator iter = ctrl.getSteps().iterator();
        Step var;
        assertTrue(iter.hasNext());
        var = (Step)iter.next();
        assertEquals("FirstControl", var.getId());
        assertEquals("Control sur les quantitées", var.getDescription());
        assertEquals("507", ((Parameter)var.getParameters().toArray()[0]).getValue());
        assertEquals("update $control.table$ set $bad.line$ where QUANTITY <> 0",
            var.getQuery().getSql());
        assertTrue(iter.hasNext());
        var = (Step)iter.next();
        assertEquals("SequenceControl", var.getId());
    }


    public void test_mapping2() throws Exception {
        Plan ctrl =
            (Plan)XmlMapperHelper.loadObject("../common/loader/PlanTestMass.xml",
                Plan.class, "PlanRules.xml");
        assertEquals(2, ctrl.getSteps().size());
        Iterator iter = ctrl.getSteps().iterator();
        Step var;
        assertTrue(iter.hasNext());
        var = (Step)iter.next();
        assertEquals("FirstControl", var.getId());
        assertEquals("Control sur les quantitees", var.getDescription());
        assertEquals("507", ((Parameter)var.getParameters().toArray()[0]).getValue());
        assertEquals("update $control.table$ set $bad.line$ where QUANTITY <> 0",
            var.getQuery().getSql());
        assertTrue(iter.hasNext());
        var = (Step)iter.next();
        assertEquals("SequenceControl", var.getId());
        assertEquals("net.codjo.control.common.loader.FakeControl", var.getControlClass());
    }
}
