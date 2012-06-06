/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.codjo.control.common.util.SQLUtil;
import net.codjo.shipment.DataField;
import net.codjo.shipment.DataShipment;
import net.codjo.shipment.DataShipmentHome;
import net.codjo.shipment.QuarantineError;
import org.apache.log4j.Logger;
/**
 * Implantation par defaut d'un RequestProcessor. Cette classe utilise la librairie codjo-shipment.
 *
 * @version $Revision: 1.5 $
 */
class DefaultShipmentProcessor implements ShipmentProcessor {
    private static final Logger APP = Logger.getLogger(DefaultShipmentProcessor.class);
    private DataShipmentBuilder builder = new DataShipmentBuilder();
    private DataShipmentFactory dsFactory = new DataShipmentFactory();


    public DataShipmentBuilder getBuilder() {
        return builder;
    }


    public DataShipmentFactory getDsFactory() {
        return dsFactory;
    }


    public void setDsFactory(DataShipmentFactory dsFactory) {
        this.dsFactory = dsFactory;
    }


    public void execute(Connection con, Dictionary dico, Shipment shipment)
          throws SQLException {
        DataShipment ds = builder.buildDataShipment(con, dico, shipment);
        ds.proceed(con);
    }


    static class DataShipmentFactory {
        public DataField buildDataField(Connection con, String sourceFieldName,
                                        int sourceTypeSQLField, String destField, int destTypeSQLField)
              throws SQLException {
            return DataShipmentHome.buildDataField(con, sourceFieldName,
                                                   sourceTypeSQLField, destField, destTypeSQLField);
        }
    }

    class DataShipmentBuilder {
        DataField[] buildDataFieldList(Connection con,
                                       Map<String, Integer> srceFields,
                                       Map<String, Integer> destFields)
              throws SQLException {
            List<DataField> dataFields = new ArrayList<DataField>();
            for (Map.Entry<String, Integer> src : srceFields.entrySet()) {
                APP.debug("  transfert de " + src.getKey() + " type " + src.getValue()
                          + " vers type " + destFields.get(src.getKey()));

                dataFields.add(dsFactory.buildDataField(con, src.getKey(),
                                                        src.getValue(),
                                                        src.getKey(),
                                                        getDestType(src.getKey(), destFields)));
            }
            return dataFields.toArray(new DataField[]{});
        }


        DataShipment buildDataShipment(Connection con, Dictionary dico, Shipment data)
              throws SQLException {
            Map<String, Integer> src = SQLUtil.determineDbFieldDef(con, dico.replaceVariables(data.getFrom()), null);
            Map<String, Integer> dest = SQLUtil.determineDbFieldDef(con, dico.replaceVariables(data.getTo()), "tempdb");
            DataField[] dataFields = buildDataFieldList(con, src, dest);

            DataShipment ds =
                  new DataShipment(dico.replaceVariables(data.getFrom()), dataFields,
                                   dico.replaceVariables(data.getTo()),
                                   new QuarantineError(data.getFromPk(), "ERROR_LOG", "ERROR_TYPE"));
            ds.setSelectWhereClause(dico.replaceVariables(data.getSelectWhereClause()));
            return ds;
        }


        private int getDestType(Object srcField, Map destFields) {
            if (!destFields.containsKey(srcField)) {
                throw new IllegalArgumentException("Champs " + srcField
                                                   + "ne possède pas d'équivalent dans la table des controles");
            }

            return ((Number)destFields.get(srcField)).intValue();
        }
    }
}
