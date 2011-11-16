/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.util;
/**
 * Convertion d'un attribut d'une entit\u00E9. Conversion depuis ou vers la BD.
 *
 * @author $Author: gonnot $
 * @version $Revision: 1.2 $
 */
public class PropertyConverter {
    private String primaryKey;
    private String propertyName;


    public PropertyConverter() {
    }


    public PropertyConverter(String propertyName, String primaryKey) {
        setPrimaryKey(primaryKey);
        setPropertyName(propertyName);
    }


    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }


    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }


    public String getPrimaryKey() {
        return primaryKey;
    }


    public String getPropertyName() {
        return propertyName;
    }


    public String getPropertyPk() {
        return propertyName + "." + primaryKey;
    }


    public Object load(Class clazz, Object pk) {
        throw new UnsupportedOperationException("La mécanique de conversion a été désactivée.");
    }


    protected String buildSelectByPk(Class clazz) {
        return "SELECT p FROM " + clazz.getName() + " p WHERE " + getPrimaryKey()
               + " = $1 ";
    }
}
