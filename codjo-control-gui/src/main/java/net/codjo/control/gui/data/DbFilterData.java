package net.codjo.control.gui.data;
/**
 *
 */
public class DbFilterData {
    private String dbFilterColumnName;
    private String renderer;
    private String sorter;

    public String getDbFilterColumnName() {
        return dbFilterColumnName;
    }


    public String getRenderer() {
        return renderer;
    }


    public void setDbFilterColumnName(String dbFilterColumnName) {
        this.dbFilterColumnName = dbFilterColumnName;
    }


    public void setRenderer(String renderer) {
        this.renderer = renderer;
    }


    public String getSorter() {
        return sorter;
    }


    public void setSorter(String sorter) {
        this.sorter = sorter;
    }
}
