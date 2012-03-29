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
import net.codjo.sql.server.util.SqlTransactionalExecutor;
/**
 * Objet responsable du transfert des données de la table de quarantaine vers la table utilisateur.
 */
public class TransfertData {
    private static final String QUARANTINE_ID = "QUARANTINE_ID";
    private static final String ERROR_TYPE = "ERROR_TYPE";
    private String quarantine;
    private String user;
    private List<String> q2user = null;
    private List<String> user2q = null;
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


    public SqlTransactionalExecutor getUserToQuarantineQuery(Connection connection) throws SQLException {
        if (user2q == null) {
            buildQueries(connection);
        }
        return createExecutorFrom(connection, user2q);
    }


    public SqlTransactionalExecutor getQuarantineToUserQuery(Connection connection) throws SQLException {
        if (q2user == null) {
            buildQueries(connection);
        }
        return createExecutorFrom(connection, q2user);
    }


    private SqlTransactionalExecutor createExecutorFrom(Connection connection,
                                                        List<String> queryList) throws SQLException {
        SqlTransactionalExecutor executor = SqlTransactionalExecutor.init(connection);
        for (String query : queryList) {
            executor.prepare(query);
        }
        return executor;
    }


    private List<String> determineDbFieldList(Connection con, String dbTableName) throws SQLException {
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
        q2user = new ArrayList<String>();

        List<String> quarantineList = determineDbFieldList(con, quarantine);

        if (replaceUserData) {
            q2user.add("delete " + user
                       + " from " + user
                       + " inner join " + quarantine
                       + " on "
                       + toString(matchingCols, "convert(varchar," + user + ".", ")", " +'µ²'+ ")
                       + " = "
                       + toString(matchingCols, "convert(varchar," + quarantine + ".", ")",
                                  " +'µ²'+ ") + " where " + quarantine + "." + ERROR_TYPE + " > 0 ");
        }

        q2user.add("INSERT INTO " + user + " " + "( " + toString(quarantineList) + " ) "
                   + "SELECT " + toString(quarantineList, quarantine + ".")
                   + " FROM " + quarantine
                   + " WHERE " + quarantine + "." + ERROR_TYPE + " > 0");
        q2user.add("DELETE " + quarantine
                   + " FROM " + quarantine
                   + " INNER JOIN " + user
                   + " ON " + quarantine + ".QUARANTINE_ID = " + user + ".QUARANTINE_ID");

        quarantineList.remove(QUARANTINE_ID);
        quarantineList.remove(QUARANTINE_ID);

        user2q = new ArrayList<String>();
        user2q.add("INSERT INTO " + quarantine + " " + "( " + toString(quarantineList) + " ) "
                   + "SELECT " + toString(quarantineList, user + ".")
                   + " FROM " + user
                   + " WHERE " + user + "." + ERROR_TYPE + " <= 0");
        user2q.add("DELETE " + user
                   + " WHERE " + ERROR_TYPE + " <= 0");
    }


    private String toString(List fields, String fieldPrefix, String fieldPostfix, String fieldSeparator) {
        StringBuilder buffer = new StringBuilder();
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
