/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.gui.plugin;
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
import net.codjo.control.gui.data.DetailData;
import net.codjo.control.gui.data.QuarantineGuiData;
import net.codjo.control.gui.data.TabData;
import net.codjo.control.gui.util.QuarantineUtil;
import net.codjo.gui.toolkit.LabelledItemPanel;
import net.codjo.gui.toolkit.util.ErrorDialog;
import net.codjo.i18n.common.TranslationManager;
import net.codjo.i18n.gui.InternationalizableContainer;
import net.codjo.i18n.gui.TranslationNotifier;
import net.codjo.mad.client.request.RequestException;
import net.codjo.mad.common.structure.StructureReader;
import net.codjo.mad.common.structure.TableStructure;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.mad.gui.i18n.InternationalizationUtil;
import net.codjo.mad.gui.request.DetailDataSource;
import net.codjo.mad.gui.request.event.DataSourceAdapter;
import net.codjo.mad.gui.request.event.DataSourceEvent;
import net.codjo.mad.gui.request.util.ButtonPanelLogic;
import net.codjo.mad.gui.request.wrapper.GuiWrapper;
import net.codjo.mad.gui.structure.StructureCache;

import static net.codjo.control.gui.i18n.InternationalizationUtil.QUARANTINE_DETAIL_TITLE;

public class DefaultQuarantineDetailWindow extends JInternalFrame implements InternationalizableContainer {
    private static final String ERROR_TYPE = "errorType";
    private static final String ERROR_LOG = "errorLog";
    private TranslationManager translationManager;
    private TranslationNotifier translationNotifier;
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

        GuiContext guiContext = dataSource.getGuiContext();
        translationNotifier = InternationalizationUtil.retrieveTranslationNotifier(guiContext);
        translationManager = InternationalizationUtil.retrieveTranslationManager(guiContext);

        this.guiData = (QuarantineGuiData)guiContext.getProperty(DefaultQuarantineWindow.QUARANTINE_GUI_DATA);
        this.dataSource = dataSource;
        this.mainTabbedPane.setName("TabbedPane");

        final DetailData detailData = guiData.getDetail();
        this.setTitle(detailData.getTitle());
        this.setPreferredSize(new Dimension(detailData.getWindowWidth(), detailData.getWindowHeight()));

        translationNotifier.addInternationalizableContainer(this);

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


    protected void declareFields(DetailDataSource detailDataSource)
          throws RequestException {
        StructureReader reader = getStructureReader(detailDataSource.getGuiContext());
        if (guiData.getDetail().getFields() != null) {
            for (String name : guiData.getDetail().getFields()) {
                if (!ERROR_LOG.equals(name) && !ERROR_TYPE.equals(name)) {
                    addDeclaredField(detailDataSource, reader, name, null);
                }
            }
        }

        if (guiData.getDetail().getTabs() != null) {
            for (TabData tabData : guiData.getDetail().getTabs()) {
                for (String name : tabData.getFields()) {
                    if (!ERROR_LOG.equals(name) && !ERROR_TYPE.equals(name)) {
                        addDeclaredField(detailDataSource, reader, name, tabData.getTitle());
                    }
                }
            }
        }
    }


    private void addDeclaredField(DetailDataSource detailDataSource, StructureReader reader,
                                  String fieldName, String tabName) {
        TableStructure tableBySqlName = reader.getTableBySqlName(guiData.getQuarantine());
        String label = tableBySqlName.getFieldByJava(fieldName).getLabel();
        String entityName = tableBySqlName.getJavaName();
        addField(detailDataSource, fieldName, getTranslation(entityName, label), new JTextField(), tabName);
    }


    public void addInternationalizableComponents(TranslationNotifier notifier) {
        if (guiData.getDetail().getTitle().startsWith(QUARANTINE_DETAIL_TITLE)) {
            notifier.addInternationalizableComponent(this, guiData.getDetail().getTitle());
        }
        notifier.addInternationalizableComponent(forceButton, "DefaultQuarantineDetailWindow.forceButton", null);
    }


    private String getTranslation(String prefix, String key) {
        if (key.startsWith(prefix)) {
            return translationManager.translate(key, translationNotifier.getLanguage());
        }
        return key;
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


    protected void addField(DetailDataSource detailDataSource, String fieldName, String label,
                            JComponent comp, String tabName) {
        comp.setName(this.getTitle() + "_" + label);
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
        currentMainPanel.setName(translationManager.translate(
              "DefaultQuarantineDetailWindow.tabLabel", translationNotifier.getLanguage()) + " "
                                 + mainPanelList.size());
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
        addField(this.dataSource,
                 ERROR_LOG,
                 translationManager.translate("DefaultQuarantineDetailWindow.errorLog",
                                              translationNotifier.getLanguage()),
                 jta,
                 null);
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
