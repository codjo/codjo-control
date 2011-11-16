/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.gui.plugin;
import net.codjo.agent.UserId;
import net.codjo.control.gui.data.DbFilterData;
import net.codjo.control.gui.data.DetailData;
import net.codjo.control.gui.data.QuarantineGuiData;
import net.codjo.control.gui.data.TabData;
import net.codjo.control.gui.data.WindowData;
import net.codjo.mad.client.plugin.MadConnectionPluginMock;
import net.codjo.mad.gui.base.GuiConfigurationMock;
import net.codjo.mad.gui.framework.DefaultGuiContext;
import net.codjo.mad.gui.framework.LocalGuiContext;
import net.codjo.security.common.api.UserMock;
import net.codjo.workflow.common.schedule.WorkflowConfiguration;
import net.codjo.workflow.gui.plugin.WorkflowGuiPlugin;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.Action;
import junit.framework.TestCase;

public class QuarantineManagerTest extends TestCase {
    private QuarantineManager manager;
    private GuiConfigurationMock configurationMock;
    private WorkflowConfiguration workflowConfiguration;


    public void test_action() throws Exception {
        Action action = configurationMock.getLastRegisteredAction();
        assertNotNull(action);
        assertNotNull(action.getValue(AbstractAction.SMALL_ICON));
        assertEquals("Emetteur", action.getValue(AbstractAction.NAME));
        assertEquals("Affiche la liste des emetteurs", action.getValue(AbstractAction.SHORT_DESCRIPTION));
    }


    public void test_guiData() throws Exception {

        Collection<QuarantineGuiData> dataList = manager.getList().getDataList();
        assertEquals(4, dataList.size());

        QuarantineGuiData guiData = getLastData(dataList);

        assertSame(workflowConfiguration, guiData.getWorkflowConfiguration());

        WindowData windowData = guiData.getWindow();
        DetailData detailData = guiData.getDetail();

        assertEquals("Liste quarantaine des emetteurs", windowData.getTitle());
        assertEquals("QUserIssuerWindow", windowData.getPreference());
        assertEquals("issuerCode",
                     ((DbFilterData)windowData.getDbFilters().toArray()[0]).getDbFilterColumnName());

        assertEquals(3, windowData.getDbFilters().size());
        assertEquals("source",
                     ((DbFilterData)windowData.getDbFilters().toArray()[1]).getDbFilterColumnName());
        assertEquals("net.codjo.control.gui.plugin.ComboBoxRenderer",
                     ((DbFilterData)windowData.getDbFilters().toArray()[1]).getRenderer());
        assertEquals("user", ((DbFilterData)windowData.getDbFilters().toArray()[2]).getDbFilterColumnName());
        assertNull("", ((DbFilterData)windowData.getDbFilters().toArray()[2]).getRenderer());

        assertEquals("Detail quarantaine des emetteurs", detailData.getTitle());
        assertEquals(5, detailData.getNbFieldsByPage());

        Collection tabs = detailData.getTabs();
        Iterator tabIter = tabs.iterator();
        TabData caracTab = (TabData)tabIter.next();
        assertEquals("Caracteristiques", caracTab.getTitle());
        Collection caracFields = caracTab.getFields();
        Iterator caracFieldsIter = caracFields.iterator();

        assertEquals("issuerCode", caracFieldsIter.next());
        assertEquals("issuerName", caracFieldsIter.next());
        assertFalse(caracFieldsIter.hasNext());

        TabData auditTab = (TabData)tabIter.next();
        assertEquals("Audit", auditTab.getTitle());
        Collection auditFields = auditTab.getFields();
        Iterator auditFieldsIter = auditFields.iterator();

        assertEquals("date", auditFieldsIter.next());
        assertEquals("user", auditFieldsIter.next());
        assertEquals("source", auditFieldsIter.next());
        assertFalse(auditFieldsIter.hasNext());

        assertFalse(tabIter.hasNext());
    }


    private QuarantineGuiData getLastData(Collection<QuarantineGuiData> dataList) {
        QuarantineGuiData guiData = null;

        for (QuarantineGuiData aDataList : dataList) {
            guiData = aDataList;
        }
        return guiData;
    }


    @Override
    protected void setUp() throws Exception {
        UserId userId = UserId.createId("login", "pwd");
        workflowConfiguration = new WorkflowConfiguration();
        manager = new QuarantineManager(QuarantineManager.class.getResource("QuarantineGuiTest.xml"),
                                        userId,
                                        workflowConfiguration);

        DefaultGuiContext defaultGuiContext = new DefaultGuiContext();
        defaultGuiContext.setUser(new UserMock().mockIsAllowedTo(true));

        LocalGuiContext guiCtxt = new LocalGuiContext(defaultGuiContext);

        configurationMock = new GuiConfigurationMock();
        manager.addMenuTo(new ControlGuiPlugin(new MadConnectionPluginMock(), new WorkflowGuiPlugin()),
                          configurationMock,
                          guiCtxt);
    }
}
