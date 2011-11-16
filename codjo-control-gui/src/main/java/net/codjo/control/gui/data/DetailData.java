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
public class DetailData {
    private String title;
    private int nbFieldsByPage = 10;
    private int windowWidth = 484;
    private int windowHeight = 490;
    private Collection<String> fields;
    private Collection<TabData> tabs;


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public int getNbFieldsByPage() {
        return nbFieldsByPage;
    }


    public void setNbFieldsByPage(int nbFieldsByPage) {
        this.nbFieldsByPage = nbFieldsByPage;
    }


    public int getWindowWidth() {
        return windowWidth;
    }


    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }


    public int getWindowHeight() {
        return windowHeight;
    }


    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
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


    public void addTab(TabData tab) {
        if (tabs == null) {
            tabs = new ArrayList<TabData>();
        }
        tabs.add(tab);
    }


    public Collection<TabData> getTabs() {
        return tabs;
    }
}
