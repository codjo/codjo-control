/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.util;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/**
 * Ensemble de méthode utilitaire SQL.
 *
 * @author $Author: rivierv $
 * @version $Revision: 1.4 $
 */
public final class SQLUtil {
    @SuppressWarnings({"ConstantNamingConvention"})
    private static final Map<Class, Integer> classToSql = new java.util.HashMap<Class, Integer>();


    static {
        buildSqlToClass();
    }


    private SQLUtil() {
    }


    /**
     * Construit la requete d'insertion des champs defini dans <code>columns </code>. La requete construite peut etre
     * utilise pour un <code>PreparedStatement</code>
     *
     * @param dbTableName Le nom physique de la table
     * @param columns     Les colonnes a inserer
     *
     * @return La requete insert (eg. "insert into MA_TABLE (CA, CB) values (?, ?)")
     */
    public static String buildInsertQuery(String dbTableName, List columns) {
        return "insert into " + dbTableName + " " + "(" + buildDBFieldNameList(columns)
               + ")" + " " + buildDBFieldValuesList(columns.size());
    }


    /**
     * Construit la requete d'update des champs defini dans <code>columns</code> . La ligne a mettre a jour est defini
     * par la liste <code>whereList</code> . La requete construite peut etre utilise pour un <code>PreparedStatement
     * </code>
     *
     * @param dbTableName Le nom physique de la table
     * @param columns     Les colonnes a inserer
     * @param whereList   Les colonnes utilise dans la clause where
     *
     * @return La requete update (eg. "update MA_TABLE set CA=? where CX=?")
     */
    public static String buildUpdateQuery(String dbTableName, Collection columns,
                                          Collection whereList) {
        return "update " + dbTableName + " set " + buildClause(columns, " , ")
               + " where " + buildClause(whereList, " and ");
    }


    /**
     * Construit la requete de selection des champs defini dans <code>columns</code> .
     *
     * @param dbTableName Le nom physique de la table
     * @param columns     Les colonnes a selectionner
     *
     * @return La requete update (eg. "select CA, CB from MA_TABLE")
     */
    public static String buildSelectQuery(String dbTableName, Collection columns) {
        String cols = "*";
        if (columns != null) {
            cols = buildDBFieldNameList(columns);
        }
        return "select " + cols + " from " + dbTableName;
    }


    /**
     * Convertion du type JAVA vers le type SQL. <br> Exemple : Integer.class devient Types.INTEGER.
     *
     * @param clazz une classe
     *
     * @return un type SQL
     *
     * @throws IllegalArgumentException Argument == null
     */
    public static int classToSqlType(Class clazz) {
        Integer sqlType = classToSql.get(clazz);
        if (sqlType == null) {
            throw new IllegalArgumentException("Type Sql inconnue pour " + clazz);
        }
        return sqlType;
    }


    public static void deleteTable(Connection con, String tableName)
          throws SQLException {
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.executeUpdate("delete " + tableName);
        }
        finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }


    /**
     * Drop d'une table.
     *
     * @param con       la connection
     * @param tableName La table
     *
     * @throws SQLException Erreur SQL
     * @deprecated Methode à ne plus utiliser (sans remplacement).
     */
    @Deprecated
    public static void dropTable(Connection con, String tableName)
          throws SQLException {
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.executeUpdate("drop table " + tableName);
        }
        catch (SQLException ex) {
            ; // si la table n'existe pas...
        }
        finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }


    /**
     * Construit la liste des noms de colonne.
     *
     * @param columns Liste des colonnes a inserer
     *
     * @return La liste. La liste est de la forme : "COL1, COL2..."
     */
    private static String buildDBFieldNameList(Collection columns) {
        StringBuilder nameList = new StringBuilder();

        for (Iterator iter = columns.iterator(); iter.hasNext(); ) {
            nameList.append((String)iter.next());
            if (iter.hasNext()) {
                nameList.append(", ");
            }
        }
        return nameList.toString();
    }


    /**
     * Construction d'une clause liste.
     *
     * @param list Liste des champs pour la clause
     * @param sep  Le separateur de champs (ex " and ")
     *
     * @return la clause : "PERIOD=? and P=?"
     */
    private static String buildClause(Collection list, String sep) {
        StringBuilder buffer = new StringBuilder();
        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            buffer.append(iter.next()).append("=?");
            if (iter.hasNext()) {
                buffer.append(sep);
            }
        }
        return buffer.toString();
    }


    /**
     * Construit le squelette liste des valeurs à inserer dans la BD.
     *
     * @param nbOfValues Nombre de valeurs
     *
     * @return le squelette, de la forme : "values (?, ?...)"
     */
    private static String buildDBFieldValuesList(int nbOfValues) {
        StringBuilder buffer = new StringBuilder("values (");

        for (int i = 0; i < nbOfValues; i++) {
            buffer.append("?");
            if (i < nbOfValues - 1) {
                buffer.append(", ");
            }
        }
        buffer.append(")");

        return buffer.toString();
    }


    /**
     * Construit la table des correspondances type JAVA vers type SQL.
     *
     * <p> http://java.sun.com/docs/books/tutorial/jdbc/basics/_retrievingTable.html#table2 </p>
     *
     * @return map
     */
    private static void buildSqlToClass() {
        classToSql.put(byte.class, Types.TINYINT);
        classToSql.put(short.class, Types.SMALLINT);
        classToSql.put(int.class, Types.INTEGER);
        classToSql.put(Integer.class, Types.INTEGER);
        classToSql.put(long.class, Types.BIGINT);
        classToSql.put(float.class, Types.REAL);
        classToSql.put(double.class, Types.FLOAT);
        classToSql.put(double.class, Types.DOUBLE);
        classToSql.put(java.math.BigDecimal.class, Types.NUMERIC);
        classToSql.put(boolean.class, Types.BIT);
        classToSql.put(Boolean.class, Types.BIT);
        classToSql.put(String.class, Types.CHAR);
        classToSql.put(String.class, Types.VARCHAR);
        classToSql.put(java.sql.Date.class, Types.DATE);
        classToSql.put(java.sql.Time.class, Types.TIME);
        classToSql.put(java.sql.Timestamp.class, Types.TIMESTAMP);
    }


    public static List<String> determineDbFieldList(Connection con, String dbTableName) throws SQLException {
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
}
