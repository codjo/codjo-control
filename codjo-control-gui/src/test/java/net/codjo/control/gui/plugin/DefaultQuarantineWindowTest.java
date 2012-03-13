package net.codjo.control.gui.plugin;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JInternalFrame;
import net.codjo.agent.AgentContainerMock;
import net.codjo.agent.UserId;
import net.codjo.agent.test.Semaphore;
import net.codjo.control.gui.ControlGuiContext;
import net.codjo.control.gui.data.QuarantineGuiData;
import net.codjo.control.gui.util.QuarantineUtil;
import net.codjo.i18n.common.Language;
import net.codjo.i18n.gui.TranslationNotifier;
import net.codjo.mad.client.plugin.MadConnectionOperations;
import net.codjo.mad.client.request.Field;
import net.codjo.mad.client.request.FieldsList;
import net.codjo.mad.client.request.MadServerFixture;
import net.codjo.mad.client.request.Request;
import net.codjo.mad.client.request.RequestException;
import net.codjo.mad.client.request.RequestIdManager;
import net.codjo.mad.client.request.Result;
import net.codjo.mad.client.request.ResultManager;
import net.codjo.mad.client.request.Row;
import net.codjo.mad.common.structure.DefaultStructureReader;
import net.codjo.mad.common.structure.StructureReader;
import net.codjo.mad.gui.base.GuiPlugin;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.mad.gui.framework.Sender;
import net.codjo.mad.gui.request.ListDataSource;
import net.codjo.mad.gui.request.Mock;
import net.codjo.mad.gui.request.PreferenceFactory;
import net.codjo.mad.gui.request.RequestComboBox;
import net.codjo.mad.gui.request.RequestTable;
import net.codjo.security.common.api.UserMock;
import net.codjo.test.common.LogString;
import org.uispec4j.ComboBox;
import org.uispec4j.Trigger;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.Window;
import org.xml.sax.InputSource;

import static net.codjo.mad.gui.i18n.InternationalizationUtil.retrieveTranslationNotifier;

public class DefaultQuarantineWindowTest extends UISpecTestCase {
    private static final String PREFERENCES =
          "<?xml version=\"1.0\"?>                                             "
          + "<preferenceList>                                                  "
          + "<preference id=\"QUserIssuerWindow\">                             "
          + "   <selectAll>selectAllQUserDecisivEntry</selectAll>              "
          + "   <update>updateQUserDecisivEntry</update>                       "
          + "   <column fieldName=\"issuerCode\" label=\"Issuer Code\"/>       "
          + "   <column fieldName=\"user\" label=\"User\"/>                    "
          + "   <column fieldName=\"source\" label=\"Source\"/>                "
          + "</preference>                                                     "
          + "</preferenceList>                                                 ";

    private Window window;
    private RequestComboBox userCombo;
    private RequestComboBox sourceCombo;
    private LogString log = new LogString();
    private Semaphore sendRequestSemaphore = new Semaphore();
    private Semaphore loadSemaphore = new Semaphore();
    private QuarantineManager manager;
    private ControlGuiContext guiContext;
    private UserId userId;
    private TranslationNotifier translationNotifier;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        userId = UserId.createId("login", "pwd");
        manager = new QuarantineManager(QuarantineManager.class.getResource("QuarantineGuiTest.xml"), userId);
        PreferenceFactory.loadMapping(new InputSource(new StringReader(PREFERENCES)));
        guiContext = new ControlGuiContext();
        translationNotifier = retrieveTranslationNotifier(guiContext);
        guiContext.setSender(new Sender(new MadOperationsMock()));
        guiContext.putProperty(GuiPlugin.AGENT_CONTAINER_KEY, new AgentContainerMock(new LogString()));
        UserMock userMock = new UserMock();
        userMock.mockIsAllowedTo(true);
        guiContext.setUser(userMock);
        initWindow(3);
        RequestIdManager.getInstance().reset();
    }


    public void test_windowPreferredSize() throws Exception {
        assertEquals("Liste quarantaine des emetteurs",
                     ((JInternalFrame)window.getAwtComponent()).getTitle());

        assertEquals(800.0, window.getAwtComponent().getPreferredSize().getWidth());
        assertEquals(480.0, window.getAwtComponent().getPreferredSize().getHeight());

        initWindow(2);

        assertEquals("Liste quarantaine des emetteurs avec taille customisée",
                     ((JInternalFrame)window.getAwtComponent()).getTitle());

        assertEquals(500.0, window.getAwtComponent().getPreferredSize().getWidth());
        assertEquals(300.0, window.getAwtComponent().getPreferredSize().getHeight());
    }


    public void test_dbFilters_constructor() throws Exception {
        assertTrue(window.containsSwingComponent(RequestComboBox.class, "DbFilter.user"));
        assertTrue(window.containsSwingComponent(RequestComboBox.class, "DbFilter.source"));

        assertEquals("selectAllQuarantineValuesForFieldName",
                     userCombo.getDataSource().getLoadFactory().getId());
        assertEquals("selectAllQuarantineValuesForFieldName",
                     sourceCombo.getDataSource().getLoadFactory().getId());
    }


    public void test_dbFilters_loadOnInit() throws Exception {
        log.assertContent(
              "filter combo : data source loaded(), filter combo : data source loaded(), filter combo : data source loaded(), filters loaded()");

        assertEquals("Tout", userCombo.getDataSource().getSelectedRow().getFieldValue("value"));
        assertEquals("Tout", sourceCombo.getDataSource().getSelectedRow().getFieldValue("value"));
    }


    public void test_dbFilters_reloadOnTableLoad() throws Exception {
        MadServerFixture madServerFixture = new MadServerFixture();
        madServerFixture.doSetUp();
        guiContext.setSender(new Sender(madServerFixture.getOperations()));

        initWindow(2, new ListDataSource());
        madServerFixture.mockServerResult(createSelectResult());

        log.clear();

        RequestTable requestTable = (RequestTable)window.getTable().getJTable();
        requestTable.load();

        log.assertContent(
              "filter combo : data source loaded(), filter combo : data source loaded(), filter combo : data source loaded(), filters loaded()");
        madServerFixture.doTearDown();
    }


    public void test_dbFilters_selectorUpdate() throws Exception {
        ComboBox testUserCombo = new ComboBox(userCombo);
        testUserCombo.select("joe bloggs 2");
        assertTrue(testUserCombo.contentEquals(new String[]{"Tout", "joe bloggs", "joe bloggs 2"}));

        translationNotifier.setLanguage(Language.EN);

        assertTrue(testUserCombo.contentEquals(new String[]{"All", "joe bloggs", "joe bloggs 2"}));

        RequestTable requestTable = (RequestTable)window.getTable().getJTable();
        ListDataSource dataSource = requestTable.getDataSource();
        FieldsList fieldsList = dataSource.getSelector();

        assertEquals("joe bloggs 2", fieldsList.getFieldValue("user"));
        assertEquals("Tout", fieldsList.getFieldValue("source"));

        testUserCombo = new ComboBox(sourceCombo);
        testUserCombo.select("testRenderer 1");

        fieldsList = dataSource.getSelector();
        assertEquals("joe bloggs 2", fieldsList.getFieldValue("user"));
        assertEquals("source 1", fieldsList.getFieldValue("source"));
    }


    public void test_dbFilters_loadTableWhenValueChanged() throws Exception {
        clearLog();

        ComboBox testUserCombo = new ComboBox(userCombo);
        testUserCombo.select("joe bloggs 2");

        assertLog();
    }


    public void test_groupingByOkButton() throws Exception {
        assertGrouping(new Trigger() {
            public void run() throws Exception {
                window.getButton(QuarantineUtil.QUARANTINE_OK_BUTTON_NAME).click();
            }
        }, "0");
    }


    public void test_groupingByForceButton() throws Exception {
        assertGrouping(new Trigger() {
            public void run() throws Exception {
                window.getButton(QuarantineUtil.QUARANTINE_FORCE_BUTTON_NAME).click();
            }
        }, "-1001");
    }


    public void test_groupIdIsOptional() throws Exception {
        clearLog();

        RequestTable requestTable = (RequestTable)window.getTable().getJTable();
        ListDataSource dataSource = requestTable.getDataSource();
        Result loadResult = MadServerFixture
              .createResult(new String[]{
                    "quarantineId", "field1", "issuerCode", "user", "source", "errorType"},
                            new String[][]{{"0", "ligne1", "hello", "me", "myTest", "1001"},
                                           {"1", "ligne2", "hello", "me", "myTest", "1001"},
                            });
        loadResult.setPrimaryKey("field1");
        dataSource.setLoadResult(loadResult);
        window.getTable().selectRow(0);

        window.getButton(QuarantineUtil.QUARANTINE_OK_BUTTON_NAME).click();

        assertLog("<update request_id=\"1\">"
                  + "<id>updateQUserDecisivEntry</id>"
                  + "<primarykey><field name=\"field1\">ligne1</field></primarykey>"
                  + "<row><field name=\"errorType\">0</field></row>"
                  + "</update>");
    }


    private void initWindow(final int windowIndex) throws Exception {
        QuarantineGuiData guiData = (QuarantineGuiData)manager.getList().getDataList().toArray()[windowIndex];
        window = new Window(new DefaultQuarantineWindowMock(guiContext, guiData, userId));
        initDataSources(window);
    }


    private void initWindow(final int windowIndex, ListDataSource dataSource) throws Exception {
        QuarantineGuiData guiData = (QuarantineGuiData)manager.getList().getDataList().toArray()[windowIndex];
        window = new Window(new DefaultQuarantineWindowMock(guiContext, guiData, userId, dataSource));
        initDataSources(window);
    }


    private void clearLog() {
        log.clear();
        loadSemaphore = new Semaphore();
    }


    private void assertLog(String... requests) {
        sendRequestSemaphore.acquire(requests.length);
        loadSemaphore.acquire(1);

        StringBuilder expected = new StringBuilder();
        for (String request : requests) {
            expected.append("sendRequest(").append(request).append("), ");
        }
        expected.append("main table : data source loaded()");
        log.assertContent(expected.toString());
    }


    private void assertGrouping(Trigger trigger, String expectedError) throws Exception {
        clearLog();

        RequestTable requestTable = (RequestTable)window.getTable().getJTable();
        ListDataSource dataSource = requestTable.getDataSource();
        Result loadResult = createSelectResult();
        loadResult.setPrimaryKey("field1");
        dataSource.setLoadResult(loadResult);
        assertEquals(5, dataSource.getRowCount());
        window.getTable().selectRows(new int[]{1, 4});
        trigger.run();

        assertLog("<update request_id=\"1\">"
                  + "<id>updateQUserDecisivEntry</id>"
                  + "<primarykey><field name=\"field1\">ligne1</field></primarykey>"
                  + "<row><field name=\"errorType\">" + expectedError + "</field></row>"
                  + "</update>",
                  "<update request_id=\"2\">"
                  + "<id>updateQUserDecisivEntry</id>"
                  + "<primarykey><field name=\"field1\">ligne2</field></primarykey>"
                  + "<row><field name=\"errorType\">" + expectedError + "</field></row>"
                  + "</update>",
                  "<update request_id=\"3\">"
                  + "<id>updateQUserDecisivEntry</id>"
                  + "<primarykey><field name=\"field1\">ligne5</field></primarykey>"
                  + "<row><field name=\"errorType\">" + expectedError + "</field></row>"
                  + "</update>");

        clearLog();

        window.getTable().selectRow(3);
        trigger.run();

        assertLog("<update request_id=\"4\">"
                  + "<id>updateQUserDecisivEntry</id>"
                  + "<primarykey><field name=\"field1\">ligne4</field></primarykey>"
                  + "<row><field name=\"errorType\">" + expectedError + "</field></row>"
                  + "</update>");
    }


    private Result createSelectResult() {
        return MadServerFixture
              .createResult(new String[]{
                    "quarantineId", "field1", "issuerCode", "groupId", "user", "source", "errorType"},
                            new String[][]{{"0", "ligne1", "hello", "1", "me", "myTest", "1001"},
                                           {"1", "ligne2", "hello", "1", "me", "myTest", "1001"},
                                           {"2", "ligne3", "hello", "2", "me", "myTest", "1001"},
                                           {"3", "ligne4", "hello", "null", "me", "myTest", "1001"},
                                           {"4", "ligne5", "hello", "3", "me", "myTest", "1001"},
                            });
    }


    private void initDataSources(Window aWindow) {
        userCombo = (RequestComboBox)aWindow.getComboBox("DbFilter.user").getAwtComponent();
        ListDataSource userDataSource = userCombo.getDataSource();

        userDataSource.addRow(createRow("value", "joe bloggs"));
        userDataSource.addRow(createRow("value", "joe bloggs 2"));

        sourceCombo = (RequestComboBox)aWindow.getComboBox("DbFilter.source").getAwtComponent();
        ListDataSource sourceDataSource = sourceCombo.getDataSource();

        sourceDataSource.addRow(createRow("value", "source 1"));
        sourceDataSource.addRow(createRow("value", "source 2"));
    }


    private Row createRow(String fieldName, String value) {
        Row row = new Row();
        List<Field> fields = new ArrayList<Field>();
        fields.add(new Field(fieldName, value));
        row.setFields(fields);
        return row;
    }


    private class MadOperationsMock implements MadConnectionOperations {
        public Result sendRequest(Request request) throws RequestException {
            log.call("sendRequest", request.toXml());
            sendRequestSemaphore.release();
            return new Result();
        }


        public Result sendRequest(Request request, long timeout) throws RequestException {
            log.call("sendRequest", request.toXml());
            return new Result();
        }


        public ResultManager sendRequests(Request[] request) throws RequestException {
            StringBuilder requests = new StringBuilder();
            for (Request aRequest : request) {
                log.call("sendRequests", requests.append(aRequest.toXml()));
            }
            return null;
        }


        public ResultManager sendRequests(Request[] request, long timeout) throws RequestException {
            StringBuilder requests = new StringBuilder();
            for (Request aRequest : request) {
                log.call("sendRequests", requests.append(aRequest.toXml()));
            }
            return null;
        }
    }

    private class DefaultQuarantineWindowMock extends DefaultQuarantineWindow {

        DefaultQuarantineWindowMock(GuiContext rootCtxt, QuarantineGuiData gui,
                                    UserId userId, ListDataSource listDataSource) throws Exception {
            super(rootCtxt, gui, userId, listDataSource);
            allFieldsSelector.addField("issuerCode", "Tout");
            allFieldsSelector.addField("user", "Tout");
            allFieldsSelector.addField("source", "Tout");
            loadFilters();
        }


        DefaultQuarantineWindowMock(GuiContext rootCtxt, QuarantineGuiData gui, UserId userId) throws Exception {
            super(rootCtxt, gui, userId, new MockDataSource("main table", log));
            allFieldsSelector.addField("issuerCode", "Tout");
            allFieldsSelector.addField("user", "Tout");
            allFieldsSelector.addField("source", "Tout");
            loadFilters();
        }


        @Override
        void loadFilters() {
            super.loadFilters();
            log.call("filters loaded");
        }


        @Override
        protected ListDataSource getFilterDataSource() {
            return new MockDataSource("filter combo", log);
        }


        @Override
        protected StructureReader getStructureReader(GuiContext localGuiCtxt) {
            try {
                return new DefaultStructureReader(getClass().getResourceAsStream("StructureDef.xml"));
            }
            catch (Exception e) {
                fail("Exception inattendue");
            }
            return null;
        }
    }

    private class MockDataSource extends Mock.ListDataSource {
        private LogString log;
        private String prefix;


        MockDataSource(String prefix, LogString log) {
            this.prefix = prefix;
            this.log = log;
        }


        @Override
        public void load() throws RequestException {
            log.call(prefix + " : data source loaded");
            loadSemaphore.release();
        }
    }
}
