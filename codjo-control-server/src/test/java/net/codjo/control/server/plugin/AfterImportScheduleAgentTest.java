package net.codjo.control.server.plugin;
import net.codjo.agent.MessageTemplate;
import net.codjo.imports.common.message.ImportJobAuditArgument;
import net.codjo.imports.common.message.ImportJobRequest;
import net.codjo.workflow.common.message.Arguments;
import net.codjo.workflow.common.message.JobAudit;
import net.codjo.workflow.common.message.JobRequest;
import net.codjo.workflow.common.message.ScheduleContract;
import net.codjo.workflow.server.api.ScheduleAgentTestCase;
import net.codjo.control.common.message.ControlJobRequest;
import net.codjo.control.common.message.SourceOfData;
/**
 * Classe de test de {@link AfterImportScheduleAgent}.
 */
public class AfterImportScheduleAgentTest extends ScheduleAgentTestCase {
    public static final String CONTROL = ControlServerPlugin.CONTROL_REQUEST_TYPE;
    public static final String QUARANTINE_TRANSFER = ControlServerPlugin.QUARANTINE_TRANSFER_TYPE;


    public void test_nominal() throws Exception {
        record().startScheduleAgent(new AfterImportScheduleAgent());

        record().startTester("schedule-leader-mock")
              .sendMessage(createScheduleContractMessage(createImportContract("AP_Q")))
              .then()
              .play(receiveAndAcceptProposal())
              .then()
              .play(receiveResult(CONTROL, matchControlQuarantine("AP_Q:"+ SourceOfData.IMPORT)));

        executeStory();
    }


    public void test_refuseContract_notAnImport() throws Exception {
        record().startScheduleAgent(new AfterImportScheduleAgent());

        JobRequest jobRequest = new JobRequest("notImport");
        jobRequest.setInitiatorLogin("user_tu");
        record().startTester("schedule-leader-mock")
              .sendMessage(createScheduleContractMessage(createContract(jobRequest)))
              .then()
              .play(receiveRefuseContract());

        executeStory();
    }


    public void test_notUnderstoodContract_badFormattedImport() throws Exception {
        record().startScheduleAgent(new AfterImportScheduleAgent());

        ScheduleContract badFormattedContract = createImportContract("AP_Q");
        badFormattedContract.getPostAudit().setArguments(null);

        record().startTester("schedule-leader-mock")
              .sendMessage(createScheduleContractMessage(badFormattedContract))
              .then()
              .play(receiveNotUnderstoodContract());

        executeStory();
    }


    private ScheduleContract createImportContract(String quarantine) {
        JobAudit postAudit = new JobAudit();
        postAudit.setArguments(new Arguments(ImportJobAuditArgument.FILLED_TABLE, quarantine));
        JobRequest jobRequest = new ImportJobRequest().toRequest();
        jobRequest.setInitiatorLogin("user_tu");
        return new ScheduleContract(jobRequest, postAudit);
    }


    private MessageTemplate matchControlQuarantine(String expected) {
        return match(new AssertJobRequest("quarantine", expected) {
            @Override
            protected String extractActual(JobRequest request) {
                ControlJobRequest jobRequest = new ControlJobRequest(request);
                return jobRequest.getQuarantineTable()+":"+jobRequest.getPath();
            }
        });
    }
}
