/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
import net.codjo.shipment.DataField;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
/**
 * DOCUMENT ME!
 *
 * @author $Author: galaber $
 * @version $Revision: 1.3 $
 */
public class DefaultShipmentProcessorTest extends TestCase {
    private MockDataShipmentFactory factory;
    private DefaultShipmentProcessor shipment;

    public DefaultShipmentProcessorTest(String testName) {
        super(testName);
    }

    public void test_buildDataFieldList() throws Exception {
        shipment.setDsFactory(factory);
        Map<String,Integer> src = new HashMap<String,Integer>();
        src.put("ID", Types.VARCHAR);

        DataField[] dfs = shipment.getBuilder().buildDataFieldList(null, src, src);

        assertEquals("ID", factory.destField);
        assertEquals("ID", factory.sourceFieldName);
        assertEquals(Types.VARCHAR, factory.sourceTypeSQLField);
        assertEquals(Types.VARCHAR, factory.destTypeSQLField);

        assertEquals(1, dfs.length);
        assertEquals(null, dfs[0]);
    }


    @Override
    protected void setUp() {
        factory = new MockDataShipmentFactory();
        shipment = new DefaultShipmentProcessor();
    }


    @Override
    protected void tearDown() {}

    static class MockDataShipmentFactory
        extends DefaultShipmentProcessor.DataShipmentFactory {
        private String destField;
        private int destTypeSQLField;
        private String sourceFieldName;
        private int sourceTypeSQLField;

        @Override
        public DataField buildDataField(Connection con, String sourceFieldNameParam,
            int sourceTypeSQLFieldParam, String destFieldParam, int destTypeSQLFieldParam)
                throws SQLException {
            this.destField = destFieldParam;
            this.destTypeSQLField = destTypeSQLFieldParam;
            this.sourceFieldName = sourceFieldNameParam;
            this.sourceTypeSQLField = sourceTypeSQLFieldParam;
            return null;
        }
    }
}
