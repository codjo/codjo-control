/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.loader;
import net.codjo.control.common.Dictionary;
import net.codjo.control.common.Shipment;
import net.codjo.control.common.ShipmentProcessor;
import java.sql.Connection;
import junit.framework.TestCase;
/**
 * DOCUMENT ME!
 *
 * @author $Author: blazart $
 * @version $Revision: 1.1 $
 */
public class ShipmentMappingTest extends TestCase {
    public ShipmentMappingTest(String testCaseName) {
        super(testCaseName);
    }

    public void test_mapping() throws Exception {
        Shipment sh =
            (Shipment)XmlMapperHelper.loadObject("../common/loader/ShipmentTest.xml",
                Shipment.class, "ShipmentRules.xml");

        assertEquals("$quarantine$", sh.getFrom());
        assertEquals("$control.table$", sh.getTo());
        assertEquals("QUARANTINE_ID", sh.getFromPk());
        assertEquals("$ligne qui sont valide$", sh.getSelectWhereClause());
    }


    public void test_mapping_customMapping() throws Exception {
        Shipment sh =
            (Shipment)XmlMapperHelper.loadObject("../common/loader/ShipmentCustomTest.xml",
                Shipment.class, "ShipmentRules.xml");

        assertEquals("$quarantine$", sh.getFrom());
        assertEquals("$control.table$", sh.getTo());
        assertEquals("QUARANTINE_ID", sh.getFromPk());
        assertEquals("$ligne qui sont valide$", sh.getSelectWhereClause());

        assertEquals(MockShipmentProcessor.class.getName(), sh.getProcessorClass());
    }

    public static class MockShipmentProcessor implements ShipmentProcessor {

        public void execute(Connection con, Dictionary dico, Shipment shipment) {
        }
    }
}
