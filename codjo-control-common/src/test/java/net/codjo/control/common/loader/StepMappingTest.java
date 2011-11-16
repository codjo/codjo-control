/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.loader;
import net.codjo.control.common.Parameter;
import net.codjo.control.common.Step;
import java.util.Iterator;
import junit.framework.TestCase;
/**
 * DOCUMENT ME!
 *
 * @author $Author: blazart $
 * @version $Revision: 1.1 $
 */
public class StepMappingTest extends TestCase {
    public StepMappingTest(String testCaseName) {
        super(testCaseName);
    }

    public void test_mapping() throws Exception {
        Step ctrl =
            (Step)net.codjo.control.common.loader.XmlMapperHelper.loadObject("../common/loader/StepTest.xml",
                Step.class, "StepRules.xml");

        assertEquals("SequenceControl", ctrl.getId());
        assertEquals("control", ctrl.getType());
        assertEquals("user", ctrl.getStepFor());
        assertEquals(70, ctrl.getPriority());
        assertEquals(5, ctrl.getErrorCode());
        assertEquals("une déscription", ctrl.getDescription());
        assertEquals("QUANTITY <> 0", ctrl.getQuery().getSql());
        assertEquals("#TEMP", ctrl.getQuery().getTemporaryTable());
        assertEquals("010P4", ctrl.getQuery().getIgnoreWarningCode());

        Iterator iter = ctrl.getParameters().iterator();
        Parameter param;
        assertTrue(iter.hasNext());
        param = (Parameter)iter.next();
        assertEquals(1, param.getIndex());
        assertEquals("int", param.getType());
        assertEquals("507", param.getValue());
        assertEquals("parametre(1,int,507)", param.toString());
        assertTrue(iter.hasNext());
        param = (Parameter)iter.next();
        assertEquals("parametre(2,string,Séquence non gérée.)", param.toString());
    }
}
