/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.loader;
import net.codjo.control.common.Dictionary;
import net.codjo.control.common.Variable;
import java.util.Iterator;
import junit.framework.TestCase;
/**
 * Description of the Class
 *
 * @author $Author: nadaud $
 * @version $Revision: 1.5 $
 */
public class DictionaryMappingTest extends TestCase {
    public DictionaryMappingTest(String testCaseName) {
        super(testCaseName);
    }

    public void test_mapping() throws Exception {
        Dictionary dico =
            (Dictionary)net.codjo.control.common.loader.XmlMapperHelper.loadObject("../common/loader/DictionaryTest.xml",
                Dictionary.class, "DictionaryRules.xml");

        assertEquals(3, dico.getVariables().size());
        Iterator iter = dico.getVariables().iterator();
        Variable var;
        assertTrue(iter.hasNext());
        var = dico.getVariable("control.table");
        assertEquals("control.table", var.getName());
        assertEquals("#CTRL_TMP_VL", var.getValue());
        assertTrue(iter.hasNext());
        var = dico.getVariable("line.ok");
        assertEquals("line.ok", var.getName());
        assertEquals("ANOMALY_TYPE=0", var.getValue());
        assertTrue(iter.hasNext());
        var = dico.getVariable("bad.line");
        assertEquals("bad.line", var.getName());
        assertEquals("ANOMALY_TYPE=? and ANOMALY_LOG=?", var.getValue());
    }
}
