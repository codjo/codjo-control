/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.loader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * Objet responsable du transfert des données de la table de quarantaine vers la table utilisateur.
 */
public class TransfertData {
    private static final String QUARANTINE_ID = "QUARANTINE_ID";
    private static final String ERROR_TYPE = "ERROR_TYPE";
    private String quarantine;
    private String user;
    private String q2user = null;
    private String user2q = null;
    private List<String> matchingCols = new ArrayList<String>();
    private boolean replaceUserData = false;


    public TransfertData() {
    }


    public TransfertData(String quarantine, String user) {
        this.quarantine = quarantine;
        this.user = user;
    }


    public String getQuarantine() {
        return quarantine;
    }


    public void setQuarantine(String quarantine) {
        this.quarantine = quarantine;
    }


    public String getUser() {
        return user;
    }


    public void setUser(String user) {
        this.user = user;
    }


    public void setReplaceUserData(boolean replace) {
        this.replaceUserData = replace;
    }


    public boolean isReplaceUserData() {
        return replaceUserData;
    }


    public List<String> getMatchingCols() {
        return Collections.unmodifiableList(matchingCols);
    }


    public void addMatchingCol(String column) {
        this.matchingCols.add(column);
    }


    public String getUserToQuarantineQuery(Connection connection)
          throws SQLException {
        if (user2q == null) {
            buildQueries(connection);
        }
        return user2q;
    }


    public String getQuarantineToUserQuery(Connection con)
          throws SQLException {
        if (q2user == null) {
            buildQueries(con);
        }
        return q2user;
    }


    private List<String> determineDbFieldList(Connection con, String dbTableName)
          throws SQLException {
        List<String> fields = new ArrayList<String>();
        DatabaseMetaData md = con.getMetaData();
        ResultSet rs = md.getColumns(null, null, dbTableName, null);
        while (rs.next()) {
            fields.add(rs.getString(4));
        }
        rs.close();
        Collections.sort(fields);
        return fields;
    }


    private void buildQueries(Connection con) throws SQLException {
        List<String> quarantineList = determineDbFieldList(con, quarantine);

        q2user =
              "INSERT INTO " + user + " " + "( " + toString(quarantineList) + " ) " + "SELECT "
              + toString(quarantineList, quarantine + ".") + " " + "FROM " + quarantine
              + " " + "WHERE " + quarantine + "." + ERROR_TYPE + " > 0" + " " + "DELETE "
              + quarantine + " " + "FROM " + quarantine + " " + "INNER JOIN " + user + " "
              + "ON " + quarantine + ".QUARANTINE_ID = " + user + ".QUARANTINE_ID";

        if (replaceUserData) {
            q2user =
                  "delete " + user + " from " + user + " inner join " + quarantine + " on "
                  + toString(matchingCols, "convert(varchar," + user + ".", ")", " +'µ²'+ ")
                  + " = "
                  + toString(matchingCols, "convert(varchar," + quarantine + ".", ")",
                             " +'µ²'+ ") + " where " + quarantine + "." + ERROR_TYPE + " > 0 "
                                         + q2user;
        }

        quarantineList.remove(QUARANTINE_ID);
        quarantineList.remove(QUARANTINE_ID);

        user2q =
              "INSERT INTO " + quarantine + " " + "( " + toString(quarantineList) + " ) "
              + "SELECT " + toString(quarantineList, user + ".") + " " + "FROM " + user + " "
              + "WHERE " + user + "." + ERROR_TYPE + " <= 0" + " " + "DELETE " + user + " "
              + "WHERE " + ERROR_TYPE + " <= 0";
    }


    private String toString(List fields, String fieldPrefix, String fieldPostfix,
                            String fieldSeparator) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < fields.size(); i++) {
            buffer.append(fieldPrefix).append(fields.get(i)).append(fieldPostfix);
            if ((i + 1) < fields.size()) {
                buffer.append(fieldSeparator);
            }
        }
        return buffer.toString();
    }


    private String toString(List fields, String fieldPrefix) {
        return toString(fields, fieldPrefix, "", ", ");
    }


    private String toString(List fields) {
        return toString(fields, "", "", ", ");
    }
}
