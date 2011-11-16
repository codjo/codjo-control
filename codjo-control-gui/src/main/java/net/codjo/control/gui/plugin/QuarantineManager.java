/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.gui.plugin;
import net.codjo.agent.UserId;
import net.codjo.control.gui.data.QuarantineGuiData;
import net.codjo.control.gui.data.QuarantineGuiDataList;
import net.codjo.mad.gui.base.GuiConfiguration;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.workflow.common.schedule.WorkflowConfiguration;
import net.codjo.xml.XmlException;
import net.codjo.xml.easyxml.EasyXMLMapper;
import java.io.IOException;
import java.net.URL;
/**
 *
 */
class QuarantineManager {
    private QuarantineGuiDataList list;
    private final UserId userId;


    QuarantineManager(URL quarantineFile, UserId userId) throws IOException, XmlException {
        this(quarantineFile, userId, new WorkflowConfiguration());
    }


    QuarantineManager(URL quarantineFile, UserId userId, WorkflowConfiguration workflowConfiguration)
          throws IOException, XmlException {
        URL rulesFile = QuarantineManager.class.getResource("QuarantineGuiDataRules.xml");

        EasyXMLMapper easyXMLMapper = new EasyXMLMapper(quarantineFile, rulesFile);
        list = (QuarantineGuiDataList)easyXMLMapper.load();

        for (QuarantineGuiData data : getList().getDataList()) {
            data.setWorkflowConfiguration(workflowConfiguration);
        }

        list.compileDataList();
        this.userId = userId;
    }


    public QuarantineGuiDataList getList() {
        return list;
    }


    public void addMenuTo(ControlGuiPlugin guiPlugin,
                          GuiConfiguration configuration,
                          GuiContext actionCtxt) {
        for (QuarantineGuiData data : getList().getDataList()) {
            DefaultQuarantineAction action = new DefaultQuarantineAction(actionCtxt, data, userId);
            configuration.registerAction(guiPlugin, data.getName(), action);
        }
    }
}
