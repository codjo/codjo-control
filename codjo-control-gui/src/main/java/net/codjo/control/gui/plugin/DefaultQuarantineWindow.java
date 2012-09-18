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
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.UserId;
import net.codjo.control.common.message.TransferJobRequest;
import net.codjo.control.common.message.TransferJobRequest.Transfer;
import net.codjo.control.common.util.FilterConstants;
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
import net.codjo.i18n.gui.InternationalizableContainer;
import net.codjo.i18n.gui.TranslationNotifier;
import net.codjo.mad.client.request.FieldsList;
import net.codjo.mad.client.request.RequestException;
import net.codjo.mad.client.request.Result;
import net.codjo.mad.client.request.Row;
import net.codjo.mad.common.structure.StructureReader;
import net.codjo.mad.common.structure.TableStructure;
import net.codjo.mad.gui.base.GuiPlugin;
import net.codjo.mad.gui.framework.AbstractGuiAction;
import net.codjo.mad.gui.framework.FilterPanel;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.mad.gui.framework.LocalGuiContext;
import net.codjo.mad.gui.framework.SwingRunnable;
import net.codjo.mad.gui.request.ListDataSource;
import net.codjo.mad.gui.request.PreferenceFactory;
import net.codjo.mad.gui.request.RequestComboBox;
import net.codjo.mad.gui.request.RequestRecordCountField;
import net.codjo.mad.gui.request.RequestTable;
import net.codjo.mad.gui.request.RequestToolBar;
import net.codjo.mad.gui.request.action.DeleteAction;
import net.codjo.mad.gui.request.event.DataSourceAdapter;
import net.codjo.mad.gui.request.event.DataSourceEvent;
import net.codjo.mad.gui.request.factory.CommandFactory;
import net.codjo.mad.gui.request.factory.SelectFactory;
import net.codjo.mad.gui.structure.StructureCache;
import net.codjo.util.string.StringUtil;
import net.codjo.workflow.common.schedule.ScheduleLauncher;

import static net.codjo.control.common.util.SqlNameCodec.decodeList;
import static net.codjo.control.gui.i18n.InternationalizationUtil.QUARANTINE_WINDOW_TITLE;
import static net.codjo.mad.gui.i18n.InternationalizationUtil.retrieveTranslationNotifier;
import static net.codjo.mad.gui.i18n.InternationalizationUtil.translate;

class DefaultQuarantineWindow extends JInternalFrame implements InternationalizableContainer {
    public static final String QUARANTINE_GUI_DATA = "QuarantineGuiData";
    protected static final String QUARANTINE_TO_USER = "DefaultQuarantineWindow.transfertUser";
    private static final String USER_TO_QUARANTINE = "DefaultQuarantineWindow.transfertQuarantine";

    private QuarantineGuiData guiData;
    private final UserId userId;
    protected GuiContext guiContext;
    private RequestTable requestTable;
    private boolean preventReloadFilterPanel;
    private QuarantineFilterPanel filterPanel;
    private RequestToolBar toolBar;
    private ProgressBarLabel progressBarLabel;
    protected FieldsList allFieldsSelector;
    private WaitingPanel waitingPanel;
    private JButton sendButton;
    private JButton validationButton;
    private JButton forceButton;


    DefaultQuarantineWindow(GuiContext context,
                            QuarantineGuiData guiData,
                            UserId userId,
                            ListDataSource mainDataSource) throws Exception {
        super(guiData.getWindow().getTitle(), true, true, true, true);
        this.setClosable(true);
        this.setIconifiable(true);
        this.setResizable(true);
        this.setMinimumSize(new Dimension(400, 200));
        this.guiData = guiData;
        this.userId = userId;
        this.progressBarLabel = new ProgressBarLabel();
        this.allFieldsSelector = new FieldsList();
        initGuiContext(context);
        initRequestTable(mainDataSource);
        initLayout();
        initToolbar();
        initFilters();
        initDbFilters();
        TranslationNotifier translationNotifier = retrieveTranslationNotifier(context);
        translationNotifier.addInternationalizableContainer(this);
        initQuarantineLoad();
    }


    protected void initQuarantineLoad() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                waitingPanel.exec(new QuarantineRunnable(Transfer.QUARANTINE_TO_USER,
                                                         QUARANTINE_TO_USER));
            }
        });
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
        waitingPanel = new WaitingPanel(translate("DefaultQuarantineWindow.waitingPanel", guiContext));

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
              translate("DefaultQuarantineWindow.filterPanel.title", guiContext)));

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
        notifier.addInternationalizableComponent(forceButton, null, "DefaultQuarantineWindow.forceButton.tooltip");
        notifier.addInternationalizableComponent(validationButton,
                                                 null, "DefaultQuarantineWindow.validateButton.tooltip");
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

        Action deleteAction = toolBar.getAction(RequestToolBar.ACTION_DELETE);
        if (deleteAction != null) {
            DeleteControlAction deleteControlAction = new DeleteControlAction(guiContext, requestTable, waitingPanel);
            /*
             * The Method replace used below modifies the component name linked to the action.
             * In order to keep the same name, we retrieve its previous name before replacing.
             */
            JButton button = toolBar.getButtonInToolBar(deleteAction);
            String deleteActionName = null;
            if (button != null) {
                deleteActionName = button.getName();
            }
            toolBar.replace(RequestToolBar.ACTION_DELETE, deleteControlAction);
            if (deleteActionName != null) {
                button.setName(deleteActionName);
            }
        }

        ForceAction forceAction;
        if (guiData.getWindow().getForceAction() != null) {
            forceAction = (ForceAction)newValidateAction(guiData.getWindow().getForceAction());
        }
        else {
            forceAction = new ForceAction(guiContext, requestTable, waitingPanel);
        }
        forceButton = toolBar.add(forceAction);
        forceButton.setName(QuarantineUtil.QUARANTINE_FORCE_BUTTON_NAME);

        ValidationAction validationAction;
        if (guiData.getWindow().getValidationAction() != null) {
            validationAction = (ValidationAction)newValidateAction(guiData.getWindow().getValidationAction());
        }
        else {
            validationAction = new ValidationAction(guiContext, requestTable, waitingPanel);
        }
        validationButton = toolBar.add(validationAction);
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
            fieldList.addField("sorter", dbFilterData.getSorter());
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
            String message = translate("DefaultQuarantineWindow.errorFilter", guiContext);
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
        newRow.addField("value", FilterConstants.ALL);
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
                    if (FilterConstants.ALL.equals(value)) {
                        value = translate("DefaultQuarantineWindow.filterComboBox.all", guiContext);
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
            String message = translate("DefaultQuarantineWindow.errorTable", guiContext);
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
        if (transferType.equals(Transfer.QUARANTINE_TO_USER)) {
            buildSelectorColumns();
        }
    }


    private void buildSelectorColumns() {

        Collection<DbFilterData> dbFilters = guiData.getWindow().getDbFilters();
        if (dbFilters == null || dbFilters.size() < 1) {
            return;
        }
        try {
            CommandFactory commandFactory = new CommandFactory("selectAllQuarantineColumnsFromTable");
            commandFactory.init(new FieldsList("tableName", guiData.getQuser()));

            Result result = guiContext.getSender().send(commandFactory.buildRequest(null));

            for (String column : decodeList(fromExtractedResult(result))) {
                allFieldsSelector.addField(StringUtil.sqlToJavaName(column), FilterConstants.ALL);
            }
        }
        catch (RequestException exception) {
            String message = translate("DefaultQuarantineWindow.errorColumn", guiContext);
            ErrorDialog.show(this, message, exception);
        }
    }


    private static String fromExtractedResult(Result result) {
        return result.getRow(0).getField(0).getValue();
    }


    private Object newAction(String className)
          throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
                 IllegalAccessException, InstantiationException {
        Class actionClass = Class.forName(className);
        Constructor constructor = actionClass.getConstructor(GuiContext.class, RequestTable.class);
        return constructor.newInstance(guiContext, requestTable);
    }


    private Object newValidateAction(String className)
          throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
                 IllegalAccessException, InstantiationException {
        Class actionClass = Class.forName(className);
        Constructor constructor = actionClass.getConstructor(GuiContext.class, RequestTable.class, WaitingPanel.class);
        return constructor.newInstance(guiContext, requestTable, waitingPanel);
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


    private class QuarantineFilterPanel extends FilterPanel {
        private QuarantineFilterPanel(RequestTable requestTable) {
            super(requestTable);
        }


        @Override
        protected void preSearch(FieldsList selector) throws RequestException {
            for (Iterator<String> it = allFieldsSelector.fieldNames(); it.hasNext(); ) {
                String column = it.next();
                if (!selector.contains(column)) {
                    selector.addField(column, FilterConstants.ALL);
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
            waitingPanel.exec(new QuarantineRunnable(TransferJobRequest.Transfer.USER_TO_QUARANTINE,
                                                     USER_TO_QUARANTINE));
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
            if (FilterConstants.ALL.equals(value)) {
                value = translate("DefaultQuarantineWindow.filterComboBox.all", guiContext);
            }
            return actualRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    protected class QuarantineRunnable extends SwingRunnable {
        private Transfer transferType;
        private String keyMessage;


        protected QuarantineRunnable(Transfer transferType, String keyMessage) {
            this.transferType = transferType;
            this.keyMessage = keyMessage;
        }


        public void run() {
            try {
                proceedTransfert(transferType);
                loadRequestTable(transferType);
            }
            catch (Exception ex) {
                ErrorDialog.show(DefaultQuarantineWindow.this, translate(keyMessage, guiContext), ex);
            }
        }
    }

    protected class DeleteControlAction extends DeleteAction {
        private WaitingPanel waitingPanel;


        protected DeleteControlAction(GuiContext ctxt, RequestTable table, WaitingPanel waitingPanel) {
            super(ctxt, table);
            this.waitingPanel = waitingPanel;
        }


        @Override
        public void actionPerformed(final ActionEvent event) {
            waitingPanel.exec(new Runnable() {
                public void run() {
                    DeleteControlAction.super.actionPerformed(event);
                }
            });
        }
    }
}
