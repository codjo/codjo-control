package net.codjo.control.gui.plugin;
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
import net.codjo.mad.gui.request.ListDataSource;
import net.codjo.mad.gui.request.PreferenceFactory;
import net.codjo.mad.gui.request.RequestComboBox;
import net.codjo.mad.gui.request.RequestRecordCountField;
import net.codjo.mad.gui.request.RequestTable;
import net.codjo.mad.gui.request.RequestToolBar;
import net.codjo.mad.gui.request.factory.SelectFactory;
import net.codjo.mad.gui.structure.StructureCache;
import net.codjo.util.string.StringUtil;
import net.codjo.workflow.common.schedule.ScheduleLauncher;
import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

class DefaultQuarantineWindow extends JInternalFrame {
    private static final String QUARANTINE_TO_USER = "Erreur lors du transfert de quarantaine vers user";
    private static final String USER_TO_QUARANTINE = "Erreur lors du transfert de user vers quarantaine";
    public static final String QUARANTINE_GUI_DATA = "QuarantineGuiData";
    private static final String TOUT = "Tout";

    private QuarantineGuiData guiData;
    private final UserId userId;
    private GuiContext guiContext;
    private RequestTable requestTable;
    private boolean preventReloadFilterPanel;
    private FilterPanel filterPanel;
    private RequestToolBar toolBar;
    private ProgressBarLabel progressBarLabel;
    protected FieldsList allFieldsSelector;
    private WaitingPanel waitingPanel = new WaitingPanel("Traitements en cours ...");


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

        guiContext.executeTask(new QuarantineRunnable("Chargement",
                                                      TransferJobRequest.Transfer.QUARANTINE_TO_USER,
                                                      QUARANTINE_TO_USER));
    }


    private void initRequestTable(ListDataSource mainDataSource) {
        requestTable = new RequestTable(mainDataSource);
        requestTable.setPreference(PreferenceFactory.getPreference(guiData.getWindow().getPreference()));
        requestTable.setName("requestTable");
    }


    private void initLayout() {
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
              new EtchedBorder(EtchedBorder.RAISED, Color.white, new Color(134, 134, 134)), "Filtres"));

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(mainPanel, BorderLayout.CENTER);
        this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        this.getContentPane().add(filterPanel, BorderLayout.NORTH);

        final WindowData window = guiData.getWindow();
        setPreferredSize(new Dimension(window.getWindowWidth(), window.getWindowHeight()));
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

        JButton sendButton = new JButton("Envoyer");
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
                    ListDataSource source = comboBox.getDataSource();
                    source.load();
                    Result loadResult = source.getLoadResult();
                    List<Row> rows = loadResult.getRows();
                    if (rows == null) {
                        rows = new ArrayList<Row>();
                    }
                    Row newRow = new Row();
                    newRow.addField("value", TOUT);
                    rows.add(0, newRow);
                    loadResult.setRows(rows);
                    comboBox.getDataSource().setLoadResult(loadResult);
                    comboBox.setSelectedIndex(0);
                    comboBox.setPreferredSize(comboBox.getPreferredSizeForContent());
                    preventReloadFilterPanel = false;
                }
            }
        }
        catch (RequestException exception) {
            ErrorDialog.show(this, "Erreur lors du chargement des filtres DB.", exception);
        }
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
            ErrorDialog.show(this, "Chargement de la table en erreur !", ex);
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
                ErrorDialog.show(this, "Chargement des noms des colonnes en erreur !", exception);
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


    private void updateGuiAfterTransfert(TransferJobRequest.Transfer transfer) {
        loadRequestTable(transfer);
        loadFilters();
    }


    private class QuarantineRunnable extends SwingRunnable {
        private TransferJobRequest.Transfer transfer;
        private String errorMessage;


        QuarantineRunnable(String title, TransferJobRequest.Transfer transfer, String errorMessage) {
            super(title + " des lignes en cours...");
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
            updateGuiAfterTransfert(transfer);
        }
    }

    private class QuarantineFilterPanel extends FilterPanel {
        private QuarantineFilterPanel(RequestTable requestTable) {
            super(requestTable);
        }


        @Override
        protected void preSearch(FieldsList selector) throws RequestException {
            for (Iterator<String> it = allFieldsSelector.fieldNames(); it.hasNext();) {
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
                            updateGuiAfterTransfert(TransferJobRequest.Transfer.USER_TO_QUARANTINE);
                        }
                        catch (Exception ex) {
                            ErrorDialog.show(DefaultQuarantineWindow.this, USER_TO_QUARANTINE, ex);
                        }
                    }
                });
            }
            else {
                guiContext.executeTask(new QuarantineRunnable("Traitement",
                                                              TransferJobRequest.Transfer.USER_TO_QUARANTINE,
                                                              USER_TO_QUARANTINE));
            }
        }
    }
}
