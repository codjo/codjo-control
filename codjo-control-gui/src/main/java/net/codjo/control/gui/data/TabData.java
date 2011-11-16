/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.gui.data;
import java.util.ArrayList;
import java.util.Collection;

/**
 */
public class TabData {
    private String title;
    private Collection<String> fields;


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public Collection<String> getFields() {
        return fields;
    }


    public void setFields(Collection<String> fields) {
        this.fields = fields;
    }


    public void removeField(String fieldName) {
        fields.remove(fieldName);
    }


    public void addField(String fieldName) {
        if (fields == null) {
            fields = new ArrayList<String>();
        }
        fields.add(fieldName);
    }
}
