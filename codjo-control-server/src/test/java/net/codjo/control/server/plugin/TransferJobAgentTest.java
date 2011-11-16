package net.codjo.control.server.plugin;
import net.codjo.aspect.AspectManager;
import net.codjo.control.common.loader.ApplicationIP;
import net.codjo.control.common.loader.IntegrationDefinition;
import net.codjo.control.common.loader.TransfertData;
import net.codjo.control.common.message.TransferJobRequest;
import net.codjo.database.common.api.DatabaseFactory;
import net.codjo.database.common.api.JdbcFixture;
import net.codjo.sql.server.JdbcServiceUtilMock;
import net.codjo.test.common.LogString;
import net.codjo.workflow.common.message.JobAudit;
import net.codjo.workflow.server.api.JobAgent;
import net.codjo.workflow.server.api.JobAgentTestCase;
import java.sql.SQLException;
public class TransferJobAgentTest extends JobAgentTestCase {
    private static final String QUARANTINE = "AP_QUARANTINE";
    private static final String USER_QUARANTINE = "AP_USER_QUARANTINE";
    private ApplicationIP applicationIP = new ApplicationIP();
    private JdbcFixture jdbc;


    @Override
    protected void doSetUp() throws Exception {
        jdbc = new DatabaseFactory().createJdbcFixture();
        jdbc.doSetUp();
    }


    @Override
    protected void doTearDown() throws Exception {
        jdbc.doTearDown();
    }


    public void test_executeQuarantineToUserTransfer() throws Exception {
        createTable(QUARANTINE);
        insertInto(QUARANTINE, "with Error", "5", "1");
        insertInto(QUARANTINE, "no Error", "0", "2");
        createTable(USER_QUARANTINE);

        applicationIP.addIntegrationDefinition(
              createDefinition(new TransfertData(QUARANTINE, USER_QUARANTINE)));

        TransferJobRequest transferRequest = new TransferJobRequest();
        transferRequest.setTransferType(TransferJobRequest.Transfer.QUARANTINE_TO_USER);
        transferRequest.setQuarantine(QUARANTINE);
        transferRequest.setUserQuarantine(USER_QUARANTINE);

        record().startJobAgent(createJobAgent());

        record().startTester("tester")
              .sendMessage(createJobRequestMessage(transferRequest.toRequest()))
              .then()
              .receiveMessage(hasAuditType(JobAudit.Type.PRE))
              .then()
              .receiveMessage(hasAuditType(JobAudit.Type.POST))
              .assertReceivedMessage(hasAuditStatus(JobAudit.Status.OK));

        executeStory();

        jdbc.assertContent(net.codjo.database.common.api.structure.SqlTable.table(QUARANTINE), new String[][]{
              {"no Error", "0", "2"}
        });
        jdbc.assertContent(net.codjo.database.common.api.structure.SqlTable.table(USER_QUARANTINE),
                           new String[][]{
                                 {"with Error", "5", "1"}
                           });
    }


    public void test_executeUserToQuarantineTransfer() throws Exception {
        createTable(QUARANTINE);
        createTable(USER_QUARANTINE);
        insertInto(USER_QUARANTINE, "non traité", "5", "1");
        insertInto(USER_QUARANTINE, "corrigé", "0", "2");

        applicationIP.addIntegrationDefinition(
              createDefinition(new TransfertData(QUARANTINE, USER_QUARANTINE)));

        TransferJobRequest transferRequest = new TransferJobRequest();
        transferRequest.setTransferType(TransferJobRequest.Transfer.USER_TO_QUARANTINE);
        transferRequest.setQuarantine(QUARANTINE);
        transferRequest.setUserQuarantine(USER_QUARANTINE);

        record().startJobAgent(createJobAgent());

        record().startTester("tester")
              .sendMessage(createJobRequestMessage(transferRequest.toRequest()))
              .then()
              .receiveMessage(hasAuditType(JobAudit.Type.PRE))
              .then()
              .receiveMessage(hasAuditType(JobAudit.Type.POST))
              .assertReceivedMessage(hasAuditStatus(JobAudit.Status.OK));

        executeStory();

        jdbc.assertContent(net.codjo.database.common.api.structure.SqlTable.table(USER_QUARANTINE),
                           new String[][]{
                                 {"non traité", "5", "1"}
                           });
        jdbc.assertContent(net.codjo.database.common.api.structure.SqlTable.table(QUARANTINE), new String[][]{
              {"corrigé", "0", null}
        });
    }


    private void insertInto(String tableName, String content, String errorType, String id)
          throws SQLException {
        jdbc.executeUpdate("insert into " + tableName
                           + " values ("
                           + "'" + content + "'"
                           + ", " + errorType + ""
                           + ", " + id + ""
                           + ")");
    }


    private void createTable(String tableName) throws SQLException {
        jdbc.create(net.codjo.database.common.api.structure.SqlTable.table(tableName),
                    "CONTENT varchar(255) null"
                    + ", ERROR_TYPE int null, QUARANTINE_ID int null");
    }


    @Override
    protected JobAgent createJobAgent() throws Exception {
        return new TransferJobAgent(new JdbcServiceUtilMock(new LogString(), jdbc),
                                    new ControlPreference(applicationIP, new AspectManager()));
    }


    @Override
    protected String getServiceType() throws Exception {
        return ControlServerPlugin.QUARANTINE_TRANSFER_TYPE;
    }


    private IntegrationDefinition createDefinition(TransfertData transfert) {
        IntegrationDefinition integrationDefinition = new IntegrationDefinition();
        integrationDefinition.setTransfert(transfert);
        return integrationDefinition;
    }
}
