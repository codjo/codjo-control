/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.util;
import net.codjo.reflect.util.ReflectHelper;
import java.beans.IntrospectionException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * Assistant pour le control d'entité.
 *
 * @author $Author: gonnot $
 * @version $Revision: 1.6 $
 */
public class EntityHelper {
    private static final String ERROR_LOG = "ERROR_LOG";
    private static final String ERROR_TYPE = "ERROR_TYPE";
    private static final String QUARANTINE_ID = "QUARANTINE_ID";
    private static final EntityResultState NO_ERROR_STATE =
          new DefaultEntityResultState();
    private PropertyConverter[] converters = {};
    private String[] convertedProperty;
    private ReflectHelper objectDef;


    public EntityHelper() {
    }


    public EntityHelper(Class clazz) throws IntrospectionException {
        objectDef = new ReflectHelper(clazz);
    }


    public void setBeanClassName(String className) {
        if ("NONE".equals(className)) {
            return;
        }
        try {
            objectDef = new ReflectHelper(Class.forName(className));
        }
        catch (Exception ex) {
            throw new IllegalArgumentException("Erreur d'initialisation " + className);
        }
    }


    public void setConverters(PropertyConverter[] converters) {
        this.converters = converters;
        convertedProperty = null;
        getConvertedProperty();
    }


    public Class getBeanClass() {
        if (objectDef != null) {
            return objectDef.getBeanClass();
        }
        return null;
    }


    public String getBeanClassName() {
        return objectDef.getBeanClass().getName();
    }


    public PropertyConverter[] getConverters() {
        return converters;
    }


    /**
     * Insertion de l'objet dans la table de control.
     *
     * @param con
     * @param bean
     * @param tableName
     *
     * @throws SQLException
     */
    public void insertIntoTable(Connection con, Object bean, String tableName)
          throws SQLException {
        String inserQuery = buildSQLInsert(tableName);
        PreparedStatement stmt = con.prepareStatement(inserQuery);
        try {
            fillPreparedStatement(stmt, bean, true);
            stmt.executeUpdate();
            if (stmt.getWarnings() != null) {
                throw stmt.getWarnings();
            }
        }
        finally {
            stmt.close();
        }
    }


    public void updateTable(Connection con, Object vo, String tableName)
          throws SQLException {
        String updateQuery = buildSQLUpdate(tableName);
        PreparedStatement stmt = con.prepareStatement(updateQuery);
        try {
            fillPreparedStatement(stmt, vo, false);
            stmt.executeUpdate();
            if (stmt.getWarnings() != null) {
                throw stmt.getWarnings();
            }
        }
        finally {
            stmt.close();
        }
    }


    /**
     * Iterateur sur la table.
     *
     * @param con       connection
     * @param tableName table BD
     *
     * @return un EntityHelper
     *
     * @throws SQLException Erreur SQL
     */
    public EntityIterator iterator(Connection con, String tableName)
          throws SQLException {
        Statement stmt =
              con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
        try {
            ResultSet rs = stmt.executeQuery(buildSQLSelect(tableName));
            return new EntityIteratorImpl(this, rs, stmt);
        }
        catch (SQLException ex) {
            stmt.close();
            throw ex;
        }
    }


    /**
     * Mise a jours du bean avec les infos controlés.
     *
     * @param con
     * @param bean
     * @param tableName
     *
     * @return Erreur sur l'entité (ou null)
     *
     * @throws SQLException
     * @throws IllegalStateException Erreur interne
     */
    public EntityResultState updateObject(Connection con, Object bean, String tableName)
          throws SQLException {
        Statement stmt = con.createStatement();
        try {
            ResultSet rs =
                  stmt.executeQuery("select * from " + tableName + " where "
                                    + QUARANTINE_ID + "=1");

            if (!rs.next()) {
                throw new IllegalStateException("Enregistrement en control est absent");
            }

            int errorType = rs.getInt(ERROR_TYPE);
            if (errorType > EntityResultState.NO_ERROR) {
                return new DefaultEntityResultState(errorType, rs.getString(ERROR_LOG));
            }

            updateObject(bean, rs);

            return NO_ERROR_STATE;
        }
        finally {
            stmt.close();
        }
    }


    public String buildSQLUpdate(String tableName) {
        List<String> cols = new ArrayList<String>();
        for (Iterator i = objectDef.shortPropertyNames(getConvertedProperty());
             i.hasNext();) {
            cols.add(toSqlName((String)i.next()));
        }
        for (int i = 0; i < getConvertedProperty().length; i++) {
            cols.add(toSqlName(getConvertedProperty()[i]));
        }

        List<String> where = new ArrayList<String>();
        where.add(QUARANTINE_ID);
        return SQLUtil.buildUpdateQuery(tableName, cols, where);
    }


    protected String buildSQLInsert(String tableName) {
        List<String> cols = new ArrayList<String>();
        cols.add(QUARANTINE_ID);
        for (Iterator i = objectDef.shortPropertyNames(getConvertedProperty());
             i.hasNext();) {
            cols.add(toSqlName((String)i.next()));
        }
        for (int i = 0; i < getConvertedProperty().length; i++) {
            cols.add(toSqlName(getConvertedProperty()[i]));
        }
        return SQLUtil.buildInsertQuery(tableName, cols);
    }


    protected String buildSQLSelect(String tableName) {
        // @ugly : Apres plusieurs essai, il s'avere que l'execution de la requete
        //      a partir de Weblogic necessite une 1ere colonne vide, sinon cela
        //      declenche une erreur sql JZ0SC.
        List<String> cols = new ArrayList<String>();
        cols.add("QUARANTINE_ID");
        cols.add("ERROR_TYPE");
        cols.add("ERROR_LOG");
        for (Iterator i = objectDef.shortPropertyNames(); i.hasNext();) {
            cols.add(toSqlName((String)i.next()));
        }
        return SQLUtil.buildSelectQuery(tableName, cols) + " where ERROR_TYPE <=0";
    }


    /**
     * Transforme le nom d'une propriétés en son nom SQL. Exemple : <code>pimsCode</code> devient
     * <code>PIMS_CODE</code>.
     *
     * @param propertyName propriété
     *
     * @return nom SQL
     */
    protected String toSqlName(String propertyName) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < propertyName.length(); i++) {
            if (Character.isUpperCase(propertyName.charAt(i)) && i > 0) {
                buffer.append('_');
            }
            buffer.append(propertyName.charAt(i));
        }

        return buffer.toString().toUpperCase();
    }


    Object newObjectFrom(ResultSet rs)
          throws SQLException, InstantiationException, IllegalAccessException {
        Object obj = getBeanClass().newInstance();
        updateObject(obj, rs);
        return obj;
    }


    void updateResultSet(ResultSet rs, Object obj, EntityResultState state)
          throws SQLException {
        // Zapp la colonne inutile QUARANTINE_ID
        if (state != null && state.getErrorType() != EntityResultState.NO_ERROR) {
            rs.updateInt(2, state.getErrorType());
            rs.updateString(3, state.getErrorLog());
            return;
        }

        int idx = 3;

        for (Iterator i = objectDef.shortPropertyNames(); i.hasNext();) {
            String propertyName = (String)i.next();
            Object value = objectDef.getPropertyValue(propertyName, obj);

            if (value == null) {
                rs.updateNull(++idx);
            }
            else {
                rs.updateObject(++idx, value);
            }
        }
    }


    private void setStatement(final PreparedStatement stmt, final int idx,
                              final Object obj, final String propertyName)
          throws SQLException {
        int sqlType = SQLUtil.classToSqlType(objectDef.getPropertyClass(propertyName));
        Object value = objectDef.getPropertyValue(propertyName, obj);
        if (value == null) {
            stmt.setNull(idx, sqlType);
        }
        else {
            stmt.setObject(idx, value, sqlType);
        }
    }


    private String[] getConvertedProperty() {
        if (convertedProperty == null) {
            convertedProperty = new String[converters.length];
            for (int i = 0; i < converters.length; i++) {
                convertedProperty[i] = converters[i].getPropertyName();
            }
        }
        return convertedProperty;
    }


    private void fillPreparedStatement(final PreparedStatement stmt, final Object obj,
                                       boolean pkFirst) throws SQLException {
        int idx = 0;

        // Positionnement de QUARANTINE_ID
        if (pkFirst) {
            stmt.setInt(++idx, 1);
        }

        for (Iterator i = objectDef.shortPropertyNames(getConvertedProperty());
             i.hasNext();) {
            String propertyName = (String)i.next();
            setStatement(stmt, ++idx, obj, propertyName);
        }

        for (int i = 0; i < getConverters().length; i++) {
            String propertyName = getConverters()[i].getPropertyPk();
            setStatement(stmt, ++idx, obj, propertyName);
        }

        // Positionnement de QUARANTINE_ID
        if (!pkFirst) {
            stmt.setInt(++idx, 1);
        }
    }


    private void updateObject(final Object bean, final ResultSet rs)
          throws SQLException {
        for (Iterator i = objectDef.shortPropertyNames(getConvertedProperty());
             i.hasNext();) {
            String shortPropertyName = (String)i.next();
            objectDef.setPropertyValue(shortPropertyName, bean,
                                       rs.getObject(toSqlName(shortPropertyName)));
        }

        for (int i = 0; i < getConverters().length; i++) {
            PropertyConverter converter = getConverters()[i];
            Object pk = rs.getObject(toSqlName(converter.getPropertyName()));
            objectDef.setPropertyValue(converter.getPropertyName(), bean,
                                       converter.load(null, pk));
        }
    }
}
