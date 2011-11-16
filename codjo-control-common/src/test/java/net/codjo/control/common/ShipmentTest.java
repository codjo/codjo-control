/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
import java.sql.Connection;
import java.sql.SQLException;
import junit.framework.TestCase;
/**
 * DOCUMENT ME!
 *
 * @author $Author: galaber $
 * @version $Revision: 1.5 $
 */
public class ShipmentTest extends TestCase {
    private Shipment shipment;

    public void test_defaultProcessor() {
        assertEquals(DefaultShipmentProcessor.class.getName(),
            shipment.getProcessorClass());

        assertEquals(DefaultShipmentProcessor.class, shipment.getProcessor().getClass());
    }


    public void test_customProcessor() throws Exception {
        shipment.setProcessorClass(MockShipmentProcessor.class.getName());

        assertEquals(MockShipmentProcessor.class.getName(), shipment.getProcessorClass());
        assertEquals(MockShipmentProcessor.class, shipment.getProcessor().getClass());

        shipment.execute(null, new Dictionary());

        assertEquals(true,
            ((MockShipmentProcessor)shipment.getProcessor()).isExecuteCalled);
    }


    @Override
    protected void setUp() {
        shipment = new Shipment();
        shipment.setFrom("from");
        shipment.setTo("to");
    }

    public static class MockShipmentProcessor implements ShipmentProcessor {
        private boolean isExecuteCalled = false;

        public void execute(Connection con, Dictionary dico, Shipment shipment)
                throws SQLException {
            isExecuteCalled = true;
        }
    }
}
