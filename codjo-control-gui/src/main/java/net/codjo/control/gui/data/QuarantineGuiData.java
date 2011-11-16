/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.gui.data;
import net.codjo.workflow.common.schedule.WorkflowConfiguration;

public class QuarantineGuiData {
    private String name;
    private String tooltip;
    private WindowData window;
    private DetailData detail;
    private QuarantineGuiDataList myList;
    private String icon;
    private String quarantine;
    private String quser;
    private WorkflowConfiguration workflowConfiguration;


    public QuarantineGuiDataList getMyList() {
        return myList;
    }


    public void setMyList(QuarantineGuiDataList myList) {
        this.myList = myList;
    }


    public WindowData getWindow() {
        return window;
    }


    public void setWindow(WindowData window) {
        this.window = window;
    }


    public DetailData getDetail() {
        return detail;
    }


    public void setDetail(DetailData detail) {
        this.detail = detail;
    }


    public String getRequestTopic() {
        return myList.getRequestTopic();
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getTooltip() {
        return tooltip;
    }


    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }


    public String getIcon() {
        return icon;
    }


    public void setIcon(String icon) {
        this.icon = icon;
    }


    public String getQuarantine() {
        return quarantine;
    }


    public void setQuarantine(String quarantine) {
        this.quarantine = quarantine;
    }


    public String getQuser() {
        return quser;
    }


    public void setQuser(String quser) {
        this.quser = quser;
    }


    public WorkflowConfiguration getWorkflowConfiguration() {
        return workflowConfiguration;
    }


    public void setWorkflowConfiguration(WorkflowConfiguration workflowConfiguration) {
        this.workflowConfiguration = workflowConfiguration;
    }
}
