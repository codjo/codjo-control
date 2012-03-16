/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.gui.data;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
/**
 * Parametrage de l'affichage en liste d'une quarantaine.
 */
public class WindowData {
    private static final String[] DEFAULT_FILTERS = {"errorType", "sourceFile"};
    private String title;
    private String preference;
    private int windowWidth = 800;
    private int windowHeight = 480;
    private Collection<String> filters;
    private Collection<DbFilterData> dbFilters;
    private String forceAction;
    private String validationAction;
    private String exportAction;
    private String toolbarCustomizer;


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public String getPreference() {
        return preference;
    }


    public void setPreference(String preference) {
        this.preference = preference;
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


    public void addFilter(String filter) {
        if (filters == null) {
            filters = new ArrayList<String>();
        }
        filters.add(filter);
    }


    public Collection<String> getFilters() {
        return filters;
    }


    public void setFilters(Collection<String> filters) {
        this.filters = filters;
    }


    public Collection<DbFilterData> getDbFilters() {
        return dbFilters;
    }


    public void addDbFilter(DbFilterData dbFilterData) {
        if (dbFilters == null) {
            dbFilters = new ArrayList<DbFilterData>();
        }
        dbFilters.add(dbFilterData);
    }


    public String getForceAction() {
        return forceAction;
    }


    public void setForceAction(String forceActionClassName) {
        this.forceAction = forceActionClassName;
    }


    public String getValidationAction() {
        return validationAction;
    }


    public void setValidationAction(String validationActionClassName) {
        this.validationAction = validationActionClassName;
    }


    public String getExportAction() {
        return exportAction;
    }


    public void setExportAction(String exportActionClassName) {
        this.exportAction = exportActionClassName;
    }


    public void setToolbarCustomizer(String toolbarCustomizer) {
        this.toolbarCustomizer = toolbarCustomizer;
    }


    public String getToolbarCustomizer() {
        return toolbarCustomizer;
    }


    void compile() {
        if (filters == null && dbFilters == null) {
            filters = Arrays.asList(DEFAULT_FILTERS);
        }
    }
}
