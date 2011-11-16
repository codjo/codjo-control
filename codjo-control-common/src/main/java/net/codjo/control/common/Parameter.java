/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
import java.sql.PreparedStatement;
import java.sql.SQLException;
/**
 * Description of the Class
 *
 * @author $Author: gonnot $
 * @version $Revision: 1.2 $
 */
public class Parameter implements Comparable {
    private int index;
    private String type;
    private String value;

    public Parameter() {}


    public Parameter(int index, String type, String value) {
        setIndex(index);
        setType(type);
        setValue(value);
    }

    public void setIndex(int index) {
        this.index = index;
    }


    public void setType(String type) {
        this.type = type;
    }


    public void setValue(String value) {
        this.value = value;
    }


    public int getIndex() {
        return index;
    }


    public String getType() {
        return type;
    }


    public String getValue() {
        return value;
    }


    public int compareTo(Object obj) {
        return getIndex() - ((Parameter)obj).getIndex();
    }


    @Override
    public String toString() {
        return "parametre(" + getIndex() + "," + getType() + "," + getValue() + ")";
    }


    void initStatement(PreparedStatement stmt, Dictionary dico)
            throws SQLException {
        if ("int".equals(getType())) {
            stmt.setInt(getIndex(), Integer.parseInt(convertValue(dico)));
        }
        else if ("string".equals(getType())) {
            stmt.setString(getIndex(), convertValue(dico));
        }
        else if ("now".equals(getType())) {
            stmt.setTimestamp(getIndex(), dico.getNow());
        }
        else {
            throw new IllegalArgumentException("Type de parametre non supporté "
                + getType());
        }
    }


    private String convertValue(Dictionary dico) {
        return dico.replaceVariables(getValue());
    }
}
