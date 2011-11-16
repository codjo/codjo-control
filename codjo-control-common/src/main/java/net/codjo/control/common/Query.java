/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
/**
 * Decrit une requete SQL.
 *
 * @author $Author: nadaud $
 * @version $Revision: 1.3 $
 */
public class Query implements Cloneable {
    private String sql;
    private String temporaryTable;
    private String ignoreWarningCode;

    public Query() {}


    public Query(String sql) {
        setSql(sql);
    }

    @Override
    public Object clone() {
        Query newQuery = new Query();
        newQuery.setSql(this.sql);
        newQuery.setTemporaryTable(this.temporaryTable);
        newQuery.setIgnoreWarningCode(this.ignoreWarningCode);
        return newQuery;
    }


    public void setSql(String sql) {
        this.sql = sql;
    }


    public void setTemporaryTable(String temporaryTable) {
        this.temporaryTable = temporaryTable;
    }


    public String getSql() {
        return sql;
    }


    public String getTemporaryTable() {
        return temporaryTable;
    }


    public boolean useTemporaryTable() {
        return getTemporaryTable() != null;
    }


    public String getIgnoreWarningCode() {
        return ignoreWarningCode;
    }


    public void setIgnoreWarningCode(String ignoreWarningCode) {
        this.ignoreWarningCode = ignoreWarningCode;
    }
}
