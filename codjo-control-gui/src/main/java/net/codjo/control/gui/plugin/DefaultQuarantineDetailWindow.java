/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.gui.plugin;
import net.codjo.control.gui.data.DetailData;
import net.codjo.control.gui.data.QuarantineGuiData;
import net.codjo.control.gui.data.TabData;
import net.codjo.control.gui.util.QuarantineUtil;
import net.codjo.gui.toolkit.LabelledItemPanel;
import net.codjo.gui.toolkit.util.ErrorDialog;
import net.codjo.mad.client.request.RequestException;
import net.codjo.mad.common.structure.FieldStructure;
import net.codjo.mad.common.structure.StructureReader;
import net.codjo.mad.common.structure.TableStructure;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.mad.gui.request.DetailDataSource;
import net.codjo.mad.gui.request.event.DataSourceAdapter;
import net.codjo.mad.gui.request.event.DataSourceEvent;
import net.codjo.mad.gui.request.util.ButtonPanelLogic;
import net.codjo.mad.gui.request.wrapper.GuiWrapper;
import net.codjo.mad.gui.structure.StructureCache;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;
/**
 * Description of the Class
 *
 * @author $Author: palmont $
 * @version $Revision: 1.8 $
 */
public class DefaultQuarantineDetailWindow extends JInternalFrame {
    private static final String ERROR_TYPE = "errorType";
    private static final String ERROR_LOG = "errorLog";
    private ButtonPanelLogic buttonPanelLogic = new ButtonPanelLogic();
    private LabelledItemPanel currentMainPanel = null;
    private int fieldsCount = 0;
    private JButton forceButton = new JButton("Forcer");
    private List<LabelledItemPanel> mainPanelList = new ArrayList<LabelledItemPanel>();
    private JTabbedPane mainTabbedPane = new JTabbedPane();
    protected DetailDataSource dataSource;
    private QuarantineGuiData guiData;
    private boolean isFormClean = false;


    public DefaultQuarantineDetailWindow(DetailDataSource dataSource)
          throws RequestException {
        super("", true, true, true, true);
        this.guiData =
              (QuarantineGuiData)dataSource.getGuiContext()
                    .getProperty(DefaultQuarantineWindow.QUARANTINE_GUI_DATA);
        this.dataSource = dataSource;
        this.mainTabbedPane.setName("TabbedPane");

        final DetailData detailData = guiData.getDetail();
        this.setTitle(detailData.getTitle());
        this.setPreferredSize(new Dimension(detailData.getWindowWidth(), detailData.getWindowHeight()));

        declareFields(dataSource);
        declareErrorFields();

        dataSource.load();
        initButtonPanel();
        addForceButton();

        if (dataSource.getLoadResult() != null) {
            dataSource.addDataSourceListener(new DataSourceAdapter() {
                @Override
                public void beforeSaveEvent(DataSourceEvent event) {
                    updateErrorType();
                }
            });
        }

        addFormPanels();
    }


    private void initButtonPanel() {
        int errorType = Integer.parseInt(dataSource.getFieldValue(ERROR_TYPE));
        if (errorType >= QuarantineUtil.FIRST_BLOCKING_CONTROL
            && errorType < QuarantineUtil.FIRST_OVERRIDABLE_CONTROL) {
            dataSource.setUpdateFactory(null);

            Map guiWrappers = dataSource.getDeclaredFields();

            for (Object object : guiWrappers.values()) {
                GuiWrapper wrapper = (GuiWrapper)object;
                if (wrapper.getGuiComponent() instanceof JTextComponent) {
                    ((JTextComponent)wrapper.getGuiComponent()).setEditable(false);
                }
                else {
                    wrapper.getGuiComponent().setEnabled(false);
                }
            }
        }
        buttonPanelLogic.setMainDataSource(dataSource);
    }


    public boolean isFormClean() {
        return isFormClean;
    }


    /**
     * Declaration des champs quarantine.
     *
     * @param detailDataSource
     *
     * @throws RequestException
     */
    protected void declareFields(DetailDataSource detailDataSource)
          throws RequestException {
        StructureReader reader = getStructureReader(detailDataSource.getGuiContext());
        if (guiData.getDetail().getFields() != null) {
            for (String name : guiData.getDetail().getFields()) {
                if (!ERROR_LOG.equals(name) && !ERROR_TYPE.equals(name)) {
                    addField(detailDataSource, name,
                             reader.getTableBySqlName(guiData.getQuarantine())
                                   .getFieldByJava(name).getLabel(), new JTextField(), null);
                }
            }
        }

        if (guiData.getDetail().getTabs() != null) {
            for (TabData tabData : guiData.getDetail().getTabs()) {
                for (String name : tabData.getFields()) {
                    if (!ERROR_LOG.equals(name) && !ERROR_TYPE.equals(name)) {
                        TableStructure tableBySqlName =
                              reader.getTableBySqlName(guiData.getQuarantine());
                        FieldStructure fieldByJava = tableBySqlName.getFieldByJava(name);
                        String label = fieldByJava.getLabel();
                        addField(detailDataSource, name, label, new JTextField(),
                                 tabData.getTitle());
                    }
                }
            }
        }
    }


    private StructureReader getStructureReader(GuiContext guiCtxt) {
        String testStructureReaderConst = "TEST_STRUCTURE_READER";
        StructureReader reader;
        if (guiCtxt.hasProperty(testStructureReaderConst)) {
            reader = (StructureReader)guiCtxt.getProperty(testStructureReaderConst);
        }
        else {
            reader =
                  ((StructureCache)guiCtxt.getProperty(StructureCache.STRUCTURE_CACHE))
                        .getStructureReader();
        }
        return reader;
    }


    protected void addField(DetailDataSource detailDataSource, String fieldName,
                            String label, JComponent comp, String tabName) {
        addBasicField(label, comp, tabName);
        detailDataSource.declare(fieldName, comp);
        comp.setName(this.getTitle() + "_" + label);
    }


    private LabelledItemPanel getCurrentPanel(String tabName) {
        if (tabName == null) {
            fieldsCount++;
            if (currentMainPanel == null
                || fieldsCount > guiData.getDetail().getNbFieldsByPage()) {
                createUnamedTab();
            }
        }
        else {
            if (currentMainPanel == null || !currentMainPanel.getName().equals(tabName)) {
                currentMainPanel = new LabelledItemPanel();
                currentMainPanel.setName(tabName);
                mainPanelList.add(currentMainPanel);
            }
        }
        return currentMainPanel;
    }


    private void createUnamedTab() {
        fieldsCount = 0;
        currentMainPanel = new LabelledItemPanel();
        mainPanelList.add(currentMainPanel);
        currentMainPanel.setName("Page " + mainPanelList.size());
    }


    protected void addBasicField(String label, JComponent comp, String tabName) {
        if (comp instanceof JTextArea) {
            ((JTextArea)comp).setLineWrap(true);
            ((JTextArea)comp).setWrapStyleWord(true);
            getCurrentPanel(tabName).addItem(label,
                                             new JScrollPane(comp, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                                             JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        }
        else {
            getCurrentPanel(tabName).addItem(label, comp);
        }
    }


    private void addForceButton() {
        forceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                isFormClean = !buttonPanelLogic.getGui().getOkButton().isEnabled();

                int errorType =
                      Integer.parseInt(dataSource.getFieldValue(ERROR_TYPE));
                dataSource.setFieldValue(ERROR_TYPE, Integer.toString(-errorType));
                try {
                    dataSource.save();
                    dispose();
                }
                catch (RequestException ex) {
                    ErrorDialog.show(null, ex.getLocalizedMessage(), ex);
                }
            }
        });

        int errorType = Integer.parseInt(dataSource.getFieldValue(ERROR_TYPE));
        if (errorType >= QuarantineUtil.FIRST_OVERRIDABLE_CONTROL) {
            forceButton.setEnabled(true);
        }
        else {
            forceButton.setEnabled(false);
        }
        buttonPanelLogic.getGui().add(forceButton, 2);
    }


    private void addFormPanels() {
        this.setBackground(UIManager.getColor("Panel.background"));
        this.getContentPane().add(mainTabbedPane, BorderLayout.CENTER);
        for (LabelledItemPanel panel : mainPanelList) {
            mainTabbedPane.addTab(panel.getName(), panel);
        }
        this.getContentPane().add(buttonPanelLogic.getGui(), BorderLayout.SOUTH);
    }


    private void declareErrorFields() {
        JTextArea jta = new JTextArea();
        jta.setRows(5);
        jta.setEditable(false);
        addField(this.dataSource, ERROR_LOG, "Erreur", jta, null);
        dataSource.declare(ERROR_TYPE, new JTextField());
    }


    private void updateErrorType() {
        int errorType = Integer.parseInt(dataSource.getFieldValue(ERROR_TYPE));
        if (QuarantineUtil.FLAG_OK < errorType) {
            dataSource.setFieldValue(ERROR_TYPE, Long.toString(QuarantineUtil.FLAG_OK));
        }
    }


    protected JTabbedPane getMainTabbedPane() {
        return mainTabbedPane;
    }


    protected QuarantineGuiData getGuiData() {
        return guiData;
    }
}
