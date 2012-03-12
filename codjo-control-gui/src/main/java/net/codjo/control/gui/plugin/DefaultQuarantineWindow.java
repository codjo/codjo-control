package net.codjo.control.gui.plugin;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.UserId;
import net.codjo.control.common.message.TransferJobRequest;
import net.codjo.control.gui.data.DbFilterData;
import net.codjo.control.gui.data.QuarantineGuiData;
import net.codjo.control.gui.data.WindowData;
import net.codjo.control.gui.plugin.ControlGuiPlugin.QuarantineToolbarCustomizer;
import net.codjo.control.gui.util.ForceAction;
import net.codjo.control.gui.util.QuarantineUtil;
import net.codjo.control.gui.util.ValidationAction;
import net.codjo.gui.toolkit.progressbar.ProgressBarLabel;
import net.codjo.gui.toolkit.table.TableFilter;
import net.codjo.gui.toolkit.table.TableFilterCombo;
import net.codjo.gui.toolkit.util.ErrorDialog;
import net.codjo.gui.toolkit.waiting.WaitingPanel;
import net.codjo.i18n.common.TranslationManager;
import net.codjo.i18n.gui.InternationalizableContainer;
import net.codjo.i18n.gui.TranslationNotifier;
import net.codjo.mad.client.request.FieldsList;
import net.codjo.mad.client.request.RequestException;
import net.codjo.mad.client.request.Result;
import net.codjo.mad.client.request.Row;
import net.codjo.mad.client.request.SelectRequest;
import net.codjo.mad.common.structure.StructureReader;
import net.codjo.mad.common.structure.TableStructure;
import net.codjo.mad.gui.base.GuiPlugin;
import net.codjo.mad.gui.framework.AbstractGuiAction;
import net.codjo.mad.gui.framework.FilterPanel;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.mad.gui.framework.LocalGuiContext;
import net.codjo.mad.gui.framework.SwingRunnable;
import net.codjo.mad.gui.i18n.InternationalizationUtil;
import net.codjo.mad.gui.request.ListDataSource;
import net.codjo.mad.gui.request.PreferenceFactory;
import net.codjo.mad.gui.request.RequestComboBox;
import net.codjo.mad.gui.request.RequestRecordCountField;
import net.codjo.mad.gui.request.RequestTable;
import net.codjo.mad.gui.request.RequestToolBar;
import net.codjo.mad.gui.request.event.DataSourceAdapter;
import net.codjo.mad.gui.request.event.DataSourceEvent;
import net.codjo.mad.gui.request.factory.SelectFactory;
import net.codjo.mad.gui.structure.StructureCache;
import net.codjo.util.string.StringUtil;
import net.codjo.workflow.common.schedule.ScheduleLauncher;

import static net.codjo.control.gui.i18n.InternationalizationUtil.QUARANTINE_WINDOW_TITLE;

class DefaultQuarantineWindow extends JInternalFrame implements InternationalizableContainer {
    private static final String QUARANTINE_TO_USER = "DefaultQuarantineWindow.transfertUser";
    private static final String USER_TO_QUARANTINE = "DefaultQuarantineWindow.transfertQuarantine";
    public static final String QUARANTINE_GUI_DATA = "QuarantineGuiData";
    private static final String TOUT = "Tout";

    private TranslationManager translationManager;
    private TranslationNotifier translationNotifier;

    private QuarantineGuiData guiData;
    private final UserId userId;
    private GuiContext guiContext;
    private RequestTable requestTable;
    private boolean preventReloadFilterPanel;
    private QuarantineFilterPanel filterPanel;
    private RequestToolBar toolBar;
    private ProgressBarLabel progressBarLabel;
    protected FieldsList allFieldsSelector;
    private WaitingPanel waitingPanel;
    private JButton sendButton;


    DefaultQuarantineWindow(GuiContext context,
                            QuarantineGuiData guiData,
                            UserId userId,
                            ListDataSource mainDataSource)
          throws Exception {
        super(guiData.getWindow().getTitle(), true, true, true, true);
        this.setClosable(true);
        this.setIconifiable(true);
        this.setResizable(true);
        this.setMinimumSize(new Dimension(400, 200));
        translationNotifier = InternationalizationUtil.retrieveTranslationNotifier(context);
        translationManager = InternationalizationUtil.retrieveTranslationManager(context);
        this.guiData = guiData;
        this.userId = userId;
        this.progressBarLabel = new ProgressBarLabel();
        this.allFieldsSelector = new FieldsList();
        initRequestTable(mainDataSource);
        initLayout();
        initGuiContext(context);
        initToolbar();
        initFilters();
        initDbFilters();
        translationNotifier.addInternationalizableContainer(this);
        guiContext.executeTask(new QuarantineRunnable(translationManager.translate(
              "DefaultQuarantineWindow.lineUpload", translationNotifier.getLanguage()),
                                                      TransferJobRequest.Transfer.QUARANTINE_TO_USER,
                                                      translationManager.translate(QUARANTINE_TO_USER,
                                                                                   translationNotifier.getLanguage())));
    }


    private void initRequestTable(ListDataSource mainDataSource) {
        requestTable = new RequestTable(mainDataSource);
        requestTable.setPreference(PreferenceFactory.getPreference(guiData.getWindow().getPreference()));
        requestTable.setName("requestTable");
        mainDataSource.addDataSourceListener(new DataSourceAdapter() {
            @Override
            public void beforeLoadEvent(DataSourceEvent event) {
                if (!filterPanel.isRunning()) {
                    loadFilters();
                }
            }
        });
    }


    private void initLayout() {
        waitingPanel = new WaitingPanel(translationManager.translate(
              "DefaultQuarantineWindow.waitingPanel", translationNotifier.getLanguage()));

        setGlassPane(waitingPanel);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getViewport().add(requestTable, null);
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        toolBar = new RequestToolBar();
        toolBar.setBorder(BorderFactory.createEtchedBorder());
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(toolBar, BorderLayout.CENTER);

        filterPanel = new QuarantineFilterPanel(requestTable);
        filterPanel.setWithSearchButton(false);
        filterPanel.setPostponedLoad(true);
        filterPanel.setBorder(new TitledBorder(
              new EtchedBorder(EtchedBorder.RAISED, Color.white, new Color(134, 134, 134)),
              translationManager.translate("DefaultQuarantineWindow.filterPanel.title",
                                           translationNotifier.getLanguage())));

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(mainPanel, BorderLayout.CENTER);
        this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        this.getContentPane().add(filterPanel, BorderLayout.NORTH);

        final WindowData window = guiData.getWindow();
        setPreferredSize(new Dimension(window.getWindowWidth(), window.getWindowHeight()));
    }


    public void addInternationalizableComponents(TranslationNotifier notifier) {
        if (guiData.getWindow().getTitle().startsWith(QUARANTINE_WINDOW_TITLE)) {
            notifier.addInternationalizableComponent(this, guiData.getWindow().getTitle());
        }
        notifier.addInternationalizableComponent(sendButton, "DefaultQuarantineWindow.sendButton", null);
    }


    private void initGuiContext(GuiContext context) {
        LocalGuiContext localGuiContext = new LocalGuiContext(context, progressBarLabel);
        localGuiContext.putProperty(QUARANTINE_GUI_DATA, this.guiData);
        guiContext = localGuiContext;
    }


    private void initToolbar() throws Exception {
        toolBar.setHasExcelButton(true);
        toolBar.init(guiContext, requestTable);
        toolBar.setLeftComponent(progressBarLabel);
        toolBar.addSeparator();

        if (guiData.getWindow().getExportAction() != null) {
            AbstractGuiAction action = (AbstractGuiAction)newAction(guiData.getWindow().getExportAction());
            toolBar.replace(RequestToolBar.ACTION_EXPORT_ALL_PAGES, action);
        }

        ForceAction forceAction;
        if (guiData.getWindow().getForceAction() != null) {
            forceAction = (ForceAction)newAction(guiData.getWindow().getForceAction());
        }
        else {
            forceAction = new ForceAction(guiContext, requestTable);
        }
        JButton forceButton = toolBar.add(forceAction);
        forceButton.setName(QuarantineUtil.QUARANTINE_FORCE_BUTTON_NAME);

        ValidationAction validationAction;
        if (guiData.getWindow().getValidationAction() != null) {
            validationAction = (ValidationAction)newAction(guiData.getWindow().getValidationAction());
        }
        else {
            validationAction = new ValidationAction(guiContext, requestTable);
        }
        JButton validationButton = toolBar.add(validationAction);
        validationButton.setName(QuarantineUtil.QUARANTINE_OK_BUTTON_NAME);
        toolBar.addSeparator();

        sendButton = new JButton();
        sendButton.setName(QuarantineUtil.QUANRANTINE_SEND_BUTTON_NAME);
        toolBar.add(sendButton);

        sendButton.addActionListener(new SendButtonActionListener());
        progressBarLabel.setName("progressBarLabel");

        customizeToolbar();
    }


    private void customizeToolbar() throws Exception {
        QuarantineToolbarCustomizer quarantineToolBarCustomizer = newQuarantineToolbarCustomizer();
        if (quarantineToolBarCustomizer != null) {
            quarantineToolBarCustomizer.customize(guiContext, toolBar);
        }
    }


    private void initFilters() {
        if (guiData.getWindow().getFilters() == null) {
            return;
        }
        TableFilter filterModel = new TableFilter(requestTable.getModel());
        requestTable.setModel(filterModel);

        StructureReader reader = getStructureReader(guiContext);
        TableStructure table = reader.getTableBySqlName(guiData.getQuarantine());

        for (String fieldName : guiData.getWindow().getFilters()) {
            TableFilterCombo filterCombo = new TableFilterCombo();
            filterCombo.setTableFilter(filterModel, requestTable.convertFieldNameToViewIndex(fieldName));
            filterCombo.setName(fieldName);

            filterPanel.addComboFilter(table.getFieldByJava(fieldName).getLabel(), filterCombo);
        }
    }


    private void initDbFilters() throws Exception {
        if (guiData.getWindow().getDbFilters() == null) {
            return;
        }
        StructureReader reader = getStructureReader(guiContext);
        TableStructure table = reader.getTableBySqlName(guiData.getQuarantine());

        for (final DbFilterData dbFilterData : guiData.getWindow().getDbFilters()) {
            FieldsList fieldList = new FieldsList();
            fieldList.addField("tableName", guiData.getQuser());
            fieldList.addField("columnName", dbFilterData.getDbFilterColumnName());
            SelectFactory loadFactory = new SelectFactory("selectAllQuarantineValuesForFieldName");
            loadFactory.init(fieldList);
            ListDataSource filterDataSource = getFilterDataSource();
            filterDataSource.setColumns(new String[]{"value"});
            filterDataSource.setLoadFactory(loadFactory);
            RequestComboBox filterComboBox;
            if (dbFilterData.getRenderer() != null) {
                ListCellRenderer renderer =
                      (ListCellRenderer)Class.forName(dbFilterData.getRenderer()).newInstance();
                filterComboBox = new FilterRequestComboBox(renderer);
            }
            else {
                filterComboBox = new RequestComboBox();
            }
            filterComboBox.setModelFieldName("value");
            filterComboBox.setSortEnabled(false);
            filterComboBox.setName("DbFilter." + dbFilterData.getDbFilterColumnName());
            filterComboBox.setDataSource(filterDataSource);
            if (dbFilterData.getRenderer() != null) {
                filterComboBox.setRendererFieldName("value");
            }
            filterPanel.addComboFilter(table.getFieldByJava(dbFilterData.getDbFilterColumnName()).getLabel(),
                                       filterComboBox);
            filterPanel.setComponentToFieldName(filterComboBox.getName(),
                                                dbFilterData.getDbFilterColumnName());
        }
        requestTable.setSelector(allFieldsSelector);
    }


    ListDataSource getFilterDataSource() {
        return new ListDataSource();
    }


    StructureReader getStructureReader(GuiContext localGuiCtxt) {
        return ((StructureCache)localGuiCtxt.getProperty(StructureCache.STRUCTURE_CACHE))
              .getStructureReader();
    }


    void loadFilters() {
        try {
            for (JComponent filter : filterPanel.getFilters()) {
                if (filter instanceof RequestComboBox) {
                    RequestComboBox comboBox = (RequestComboBox)filter;
                    preventReloadFilterPanel = true;
                    String oldFilterSelection = getCurrentFilterSelection(comboBox);
                    reloadFilter(comboBox);

                    if (!selectFilter(comboBox, oldFilterSelection)) {
                        if (oldFilterSelection != null) {
                            preventReloadFilterPanel = false;
                        }
                        comboBox.setSelectedIndex(0);
                    }

                    comboBox.setPreferredSize(comboBox.getPreferredSizeForContent());

                    preventReloadFilterPanel = false;
                }
            }
        }
        catch (RequestException exception) {
            String message = translationManager.translate(
                  "DefaultQuarantineWindow.errorFilter",
                  translationNotifier.getLanguage());
            ErrorDialog.show(this, message, exception);
        }
    }


    private boolean selectFilter(RequestComboBox comboBox, String filter) {
        Object currentSelection = comboBox.getSelectedItem();
        comboBox.setSelectedItem(filter);
        return comboBox.getSelectedItem() != currentSelection;
    }


    private void reloadFilter(RequestComboBox comboBox) throws RequestException {
        comboBox.getDataSource().load();
        Result loadResult = comboBox.getDataSource().getLoadResult();
        List<Row> rows = loadResult.getRows();
        if (rows == null) {
            rows = new ArrayList<Row>();
        }
        Row newRow = new Row();
        newRow.addField("value", TOUT);
        rows.add(0, newRow);
        loadResult.setRows(rows);
        comboBox.getDataSource().setLoadResult(loadResult);
        ListCellRenderer actualRenderer = comboBox.getRenderer();
        if (actualRenderer == null) {
            comboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList list,
                                                              Object value,
                                                              int index,
                                                              boolean isSelected,
                                                              boolean cellHasFocus) {
                    if (TOUT.equals(value)) {
                        value = translationManager.translate("DefaultQuarantineWindow.filterComboBox.all",
                                                             translationNotifier.getLanguage());
                    }
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            });
        }
        else {
            comboBox.setRenderer(new TranslationRenderer(actualRenderer));
        }
    }


    private String getCurrentFilterSelection(RequestComboBox comboBox) {
        Row oldSelection = comboBox.getDataSource().getSelectedRow();
        String oldValue = null;
        if (oldSelection != null) {
            oldValue = oldSelection.getFieldValue("value");
        }
        return oldValue;
    }


    private void loadRequestTable(TransferJobRequest.Transfer transferType) {
        if (TransferJobRequest.Transfer.USER_TO_QUARANTINE.equals(transferType)) {
            requestTable.setSelector(allFieldsSelector);
        }
        try {
            progressBarLabel.setText("");
            requestTable.load();

            RequestRecordCountField field = new RequestRecordCountField();
            field.initialize(requestTable, guiContext);
            toolBar.setLeftComponent(field);
        }
        catch (RequestException ex) {
            String message = translationManager.translate(
                  "DefaultQuarantineWindow.errorTable", translationNotifier.getLanguage());
            ErrorDialog.show(this, message, ex);
        }
    }


    private void proceedTransfert(TransferJobRequest.Transfer transferType) throws Exception {
        AgentContainer container = ((AgentContainer)guiContext.getProperty(GuiPlugin.AGENT_CONTAINER_KEY));

        TransferJobRequest transferRequest = new TransferJobRequest();
        transferRequest.setTransferType(transferType);
        transferRequest.setQuarantine(guiData.getQuarantine());
        transferRequest.setUserQuarantine(guiData.getQuser());

        ScheduleLauncher launcher = new ScheduleLauncher(userId);
        launcher.setWorkflowConfiguration(guiData.getWorkflowConfiguration());
        launcher.executeWorkflow(container, transferRequest.toRequest());
        if (transferType.equals(TransferJobRequest.Transfer.QUARANTINE_TO_USER)) {
            Collection<DbFilterData> dbFilters = guiData.getWindow().getDbFilters();
            if (dbFilters == null || dbFilters.size() < 1) {
                return;
            }
            try {
                FieldsList fieldsList = new FieldsList();
                fieldsList.addField("tableName", guiData.getQuser());
                SelectFactory selectFactory = new SelectFactory("selectAllQuarantineColumnsFromTable");
                selectFactory.init(fieldsList);
                SelectRequest request = (SelectRequest)selectFactory.buildRequest(null);
                request.setPage(1, 1000);
                Result result = guiContext.getSender().send(request);
                for (Object aList : result.getRows()) {
                    Row row = (Row)aList;
                    allFieldsSelector.addField(StringUtil.sqlToJavaName(row.getFieldValue("value")), TOUT);
                }
            }
            catch (RequestException exception) {
                String message = translationManager.translate("DefaultQuarantineWindow.errorColumn",
                                                              translationNotifier.getLanguage());
                ErrorDialog.show(this, message, exception);
            }
        }
    }


    private Object newAction(String className)
          throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
                 IllegalAccessException, InstantiationException {
        Class actionClass = Class.forName(className);
        Constructor constructor = actionClass.getConstructor(GuiContext.class, RequestTable.class);
        return constructor.newInstance(guiContext, requestTable);
    }


    private QuarantineToolbarCustomizer newQuarantineToolbarCustomizer()
          throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
                 IllegalAccessException, InstantiationException {
        if (guiData.getWindow().getToolbarCustomizer() != null) {
            Class customizerClass = Class.forName(guiData.getWindow().getToolbarCustomizer());
            Constructor constructor = customizerClass.getConstructor();
            return (QuarantineToolbarCustomizer)constructor.newInstance();
        }
        return null;
    }


    private class QuarantineRunnable extends SwingRunnable {
        private TransferJobRequest.Transfer transfer;
        private String errorMessage;


        QuarantineRunnable(String title, TransferJobRequest.Transfer transfer, String errorMessage) {
            super(title);
            this.transfer = transfer;
            this.errorMessage = errorMessage;
        }


        public void run() {
            try {
                proceedTransfert(transfer);
            }
            catch (Exception reqex) {
                ErrorDialog.show(DefaultQuarantineWindow.this, errorMessage, reqex);
            }
        }


        @Override
        public void updateGui() {
            loadRequestTable(transfer);
        }
    }

    private class QuarantineFilterPanel extends FilterPanel {
        private QuarantineFilterPanel(RequestTable requestTable) {
            super(requestTable);
        }


        @Override
        protected void preSearch(FieldsList selector) throws RequestException {
            for (Iterator<String> it = allFieldsSelector.fieldNames(); it.hasNext(); ) {
                String column = it.next();
                if (!selector.contains(column)) {
                    selector.addField(column, TOUT);
                }
            }
        }


        @Override
        protected void reload(EventObject evt) {
            if (preventReloadFilterPanel) {
                return;
            }
            super.reload(evt);
        }
    }

    private static class FilterRequestComboBox extends RequestComboBox {
        private final ListCellRenderer listCellRenderer;


        private FilterRequestComboBox(ListCellRenderer listCellRenderer) {
            this.listCellRenderer = listCellRenderer;
        }


        @Override
        public void setRendererFieldName(String rendererFieldName) {
            this.rendererFieldName = rendererFieldName;
            this.setRenderer(listCellRenderer);
            model.fireAllContentsHasChangedEvent();
        }
    }

    private class SendButtonActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (guiData.getWindow().isSyncValidation()) {
                waitingPanel.exec(new Runnable() {
                    public void run() {
                        try {
                            proceedTransfert(TransferJobRequest.Transfer.USER_TO_QUARANTINE);
                            loadRequestTable(TransferJobRequest.Transfer.USER_TO_QUARANTINE);
                        }
                        catch (Exception ex) {
                            ErrorDialog.show(DefaultQuarantineWindow.this,
                                             translationManager.translate(USER_TO_QUARANTINE,
                                                                          translationNotifier.getLanguage()),
                                             ex);
                        }
                    }
                });
            }
            else {
                guiContext.executeTask(new QuarantineRunnable(translationManager.translate(
                      "DefaultQuarantineWindow.lineProcessing",
                      translationNotifier.getLanguage()), TransferJobRequest.Transfer.USER_TO_QUARANTINE,
                                                              translationManager.translate(USER_TO_QUARANTINE,
                                                                                           translationNotifier.getLanguage())));
            }
        }
    }

    private class TranslationRenderer implements ListCellRenderer {

        private ListCellRenderer actualRenderer;


        private TranslationRenderer(ListCellRenderer actualRenderer) {
            this.actualRenderer = actualRenderer;
        }


        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            if (TOUT.equals(value)) {
                value = translationManager.translate("DefaultQuarantineWindow.filterComboBox.all",
                                                     translationNotifier.getLanguage());
            }
            return actualRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
}
