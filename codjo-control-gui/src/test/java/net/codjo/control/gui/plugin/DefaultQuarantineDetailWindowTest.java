/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.gui.plugin;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.xml.parsers.ParserConfigurationException;
import net.codjo.agent.UserId;
import net.codjo.control.gui.ControlGuiContext;
import net.codjo.control.gui.data.DetailData;
import net.codjo.control.gui.data.QuarantineGuiData;
import net.codjo.control.gui.data.QuarantineGuiDataList;
import net.codjo.gui.toolkit.LabelledItemPanel;
import net.codjo.mad.client.request.Result;
import net.codjo.mad.client.request.Row;
import net.codjo.mad.common.structure.DefaultStructureReader;
import net.codjo.mad.common.structure.StructureReader;
import net.codjo.mad.common.structure.TableStructure;
import net.codjo.mad.gui.request.DetailDataSource;
import net.codjo.xml.XmlException;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.Window;
import org.xml.sax.SAXException;
public class DefaultQuarantineDetailWindowTest extends UISpecTestCase {
    private DefaultQuarantineDetailWindow detailWindow;
    private QuarantineGuiData guiData;
    private StructureReader reader;
    private QuarantineManager manager;


    @Override
    protected void setUp() throws Exception {
        reader = getReader();
        manager = new QuarantineManager(QuarantineManager.class.getResource("QuarantineGuiTest.xml"),
                                        UserId.createId("login", "pwd"));
    }


    private DetailDataSource getDataSource(int quarantineNb)
          throws IOException, ParserConfigurationException, SAXException {

        Result result = new Result();
        result.addRow(new Row(Collections.singletonMap("errorType", "10")));

        ControlGuiContext ctxt = new ControlGuiContext();
        DetailDataSource dataSource = new DetailDataSource(ctxt);
        dataSource.setLoadResult(result);

        guiData = (QuarantineGuiData)manager.getList().getDataList().toArray()[quarantineNb];

        ctxt.putProperty(DefaultQuarantineWindow.QUARANTINE_GUI_DATA, guiData);
        ctxt.putProperty("TEST_STRUCTURE_READER", getReader());
        return dataSource;
    }


    private StructureReader getReader()
          throws IOException, ParserConfigurationException, SAXException {
        reader = new DefaultStructureReader(getClass().getResourceAsStream("StructureDef.xml"));
        return reader;
    }


    public void test_benchmarkQuarantineDetailWindow() throws Exception {
        detailWindow = new DefaultQuarantineDetailWindow(getDataSource(0));

        Button forceButton = new Window(detailWindow).getButton("Forcer");
        assertEquals("Forcer", forceButton.getLabel());

        assertEquals("Quarantaine des benchs", detailWindow.getTitle());
        JTabbedPane tabbedPane = detailWindow.getMainTabbedPane();

        assertEquals(1, tabbedPane.getTabCount());

        Collection fieldsInGui = guiData.getDetail().getFields();
        assertEquals(2, fieldsInGui.size());

        LabelledItemPanel labelledItemPanel = (LabelledItemPanel)tabbedPane.getComponentAt(0);
        Panel labelPanel = new Panel(labelledItemPanel);
        assertEquals("Code ptf", labelPanel.getTextBox("Quarantaine des benchs_Code ptf.label").getText());
        assertEquals("Code du bench", labelPanel.getTextBox("Quarantaine des benchs_Code du bench.label").getText());

        assertEquals(800.0, detailWindow.getPreferredSize().getWidth());
        assertEquals(600.0, detailWindow.getPreferredSize().getHeight());
    }


    public void test_emetteurQuarantineDetailWindow() throws Exception {
        detailWindow = new DefaultQuarantineDetailWindow(getDataSource(3));
        assertEquals("Detail quarantaine des emetteurs", detailWindow.getTitle());

        detailWindow.declareFields(getDataSource(3));
        JTabbedPane tabbedPane = detailWindow.getMainTabbedPane();

        assertEquals(2, tabbedPane.getTabCount());
        assertEquals("Caracteristiques", tabbedPane.getComponentAt(0).getName());
        assertEquals("Audit", tabbedPane.getComponentAt(1).getName());

        LabelledItemPanel labelledItemPanel = (LabelledItemPanel)tabbedPane.getComponentAt(0);
        assertEquals(2, countNumberOfTextField(labelledItemPanel));
        labelledItemPanel = (LabelledItemPanel)tabbedPane.getComponentAt(1);
        assertEquals(3, countNumberOfTextField(labelledItemPanel));

        assertEquals(484.0, detailWindow.getPreferredSize().getWidth());
        assertEquals(490.0, detailWindow.getPreferredSize().getHeight());
    }


    public void test_compareFields() throws Exception {
        QuarantineGuiDataList list = manager.getList();
        for (QuarantineGuiData qGuiData : list.getDataList()) {
            TableStructure table = reader.getTableBySqlName(qGuiData.getQuarantine());
            compare(table, qGuiData);
        }
    }


    private void compare(TableStructure table, QuarantineGuiData qGuiData) throws Exception {
        DetailData detailData = qGuiData.getDetail();
        if (detailData != null && detailData.getFields() != null) {
            Collection fieldsInGui = detailData.getFields();
            for (Object key : table.getFieldsByJavaKey().keySet()) {
                String field = (String)key;
                if ("quarantineId".equals(field)) {
                    continue;
                }
                assertTrue("Le champ " + field
                           + " de la Quarantaine devrait etre affiché dans l'écran détail >"
                           + qGuiData.getName() + "<", fieldsInGui.contains(field));
            }
        }
    }


    private int countNumberOfTextField(LabelledItemPanel labelledItemPanel) {
        int number = 0;
        Component[] liste = labelledItemPanel.getComponents();
        for (Component component : liste) {
            if (component instanceof JTextField) {
                number++;
            }
        }
        return number;
    }


    private static DetailDataSource getDataSourceChiotte(int quarantineNb)
          throws IOException, ParserConfigurationException, SAXException, XmlException {

        Result result = new Result();
        result.addRow(new Row(Collections.singletonMap("errorType", "10")));

        ControlGuiContext ctxt = new ControlGuiContext();
        DetailDataSource dataSource = new DetailDataSource(ctxt);
        dataSource.setLoadResult(result);
        QuarantineManager manager =
              new QuarantineManager(QuarantineManager.class.getResource("QuarantineGuiTest.xml"),
                                    UserId.createId("login", "pwd"));
        QuarantineGuiData guiData = (QuarantineGuiData)manager.getList().getDataList().toArray()[quarantineNb];

        ctxt.putProperty(DefaultQuarantineWindow.QUARANTINE_GUI_DATA, guiData);
        ctxt.putProperty("TEST_STRUCTURE_READER",
                         new DefaultStructureReader(DefaultQuarantineDetailWindowTest.class.getResourceAsStream(
                               "StructureDef.xml")));
        return dataSource;
    }


    public static void main(String[] args) throws Exception {
        DetailDataSource dataSourceChiotte = getDataSourceChiotte(0);
        DefaultQuarantineDetailWindow window = new DefaultQuarantineDetailWindow(dataSourceChiotte);
        assertEquals("Quarantaine des benchs", window.getTitle());

        JFrame frame = new JFrame("Test Renderer");
        frame.getContentPane().add(window.getMainTabbedPane());
        frame.pack();
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                System.exit(0);
            }
        });
    }
}
