/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.loader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
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

        Statement statement = con.createStatement();
        try {
            ResultSet rs = statement.executeQuery("select * from " + dbTableName + " where 1 = 0");
            ResultSetMetaData rsmd = rs.getMetaData();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                fields.add(rsmd.getColumnLabel(i));
            }
            rs.close();
        }
        finally {
            statement.close();
        }
        Collections.sort(fields);
        return fields;
    }


    private void buildQueries(Connection con) throws SQLException {
        q2user = new ArrayList<String>();

        List<String> quarantineList = determineDbFieldList(con, quarantine);

        if (replaceUserData) {
            // TODO : Attention requete encore specifique Sybase, ne pas activer replaceUserData en Oracle 
            q2user.add("delete " + user
                       + " from " + user
                       + " inner join " + quarantine
                       + " on "
                       + toString(matchingCols, "convert(varchar," + user + ".", ")", " +'µ²'+ ")
                       + " = "
                       + toString(matchingCols, "convert(varchar," + quarantine + ".", ")",
                                  " +'µ²'+ ") + " where " + quarantine + "." + ERROR_TYPE + " > 0 ");
        }

        q2user.add("insert into " + user + " " + "( " + toString(quarantineList) + " ) "
                   + "select " + toString(quarantineList, quarantine + ".")
                   + " from " + quarantine
                   + " where " + quarantine + "." + ERROR_TYPE + " > 0");
        q2user.add("delete " + quarantine
                   + " where QUARANTINE_ID in ("
                   + "   select " + user + ".QUARANTINE_ID"
                   + "   from " + quarantine + " INNER JOIN " + user
                   + "   on " + quarantine + ".QUARANTINE_ID = " + user + ".QUARANTINE_ID)");

        quarantineList.remove(QUARANTINE_ID);

        user2q = new ArrayList<String>();
        user2q.add("insert into " + quarantine + " " + "( " + toString(quarantineList) + " ) "
                   + "select " + toString(quarantineList, user + ".")
                   + " from " + user
                   + " where " + user + "." + ERROR_TYPE + " <= 0");
        user2q.add("delete " + user
                   + " where " + ERROR_TYPE + " <= 0");
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
