/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
import net.codjo.shipment.DataField;
import net.codjo.shipment.DataShipment;
import net.codjo.shipment.DataShipmentHome;
import net.codjo.shipment.QuarantineError;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            Map<String, Integer> src =
                  SQLUtil.determineFieldDef(con, dico.replaceVariables(data.getFrom()), null);
            Map<String, Integer> dest =
                  SQLUtil.determineFieldDef(con, dico.replaceVariables(data.getTo()),
                                            "tempdb");
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

    private static final class SQLUtil {
        private SQLUtil() {
        }


        private static Map<String, Integer> determineFieldDef(Connection con, String dbTableName,
                                                              String catalog) throws SQLException {
            if (dbTableName.startsWith("#")) {
                dbTableName =
                      dbTableName.substring(0, Math.min(13, dbTableName.length())) + "%";
            }
            DatabaseMetaData md = con.getMetaData();
            ResultSet rs = md.getColumns(catalog, null, dbTableName, null);
            APP.debug("analyse des champs de la table "
                      + ((catalog != null) ? catalog + "." : "") + dbTableName);
            Map<String, Integer> fieldDef = new HashMap<String, Integer>();
            while (rs.next()) {
                String dbFieldName = rs.getString(4);
                int sqlType = rs.getInt(5);
                APP.debug("  champ " + dbFieldName + " de type " + sqlType);
                fieldDef.put(dbFieldName, sqlType);
            }
            return fieldDef;
        }
    }
}
