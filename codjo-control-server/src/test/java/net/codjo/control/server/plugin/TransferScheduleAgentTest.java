/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.server.plugin;
import net.codjo.agent.MessageTemplate;
import net.codjo.control.common.message.ControlJobRequest;
import net.codjo.control.common.message.SourceOfData;
import net.codjo.control.common.message.TransferJobRequest;
import net.codjo.workflow.common.message.JobRequest;
import net.codjo.workflow.common.message.ScheduleContract;
import net.codjo.workflow.server.api.ScheduleAgentTestCase;
/**
 * Classe de test de {@link TransferScheduleAgent}.
 */
public class TransferScheduleAgentTest extends ScheduleAgentTestCase {
    public static final String CONTROL = ControlServerPlugin.CONTROL_REQUEST_TYPE;
    public static final String QUARANTINE_TRANSFER = ControlServerPlugin.QUARANTINE_TRANSFER_TYPE;


    public void test_userToQuarantineWorkflow_step1Control() throws Exception {
        record().startScheduleAgent(new TransferScheduleAgent());

        record().startTester("schedule-leader-mock")
              .sendMessage(createScheduleContractMessage(userToQuarantineContract("AP_Q_USER", "AP_Q")))
              .then()
              .play(receiveAndAcceptProposal())
              .then()
              .play(receiveResult(CONTROL,
                                  matchControlQuarantine("AP_Q:" + SourceOfData.TRANSFERT_FROM_QUARANTINE)));

        executeStory();
    }


    public void test_userToQuarantineWorkflow_step2BackToUser() throws Exception {
        record().startScheduleAgent(new TransferScheduleAgent());

        final ScheduleContract userToQuarantine = userToQuarantineContract("AP_Q_USER", "AP_Q");

        record().startTester("schedule-leader-mock")
              .sendMessage(createScheduleContractMessage(createContract("control", userToQuarantine)))
              .then()
              .play(receiveAndAcceptProposal())
              .then()
              .play(receiveResult(QUARANTINE_TRANSFER,
                                  matchTransferRequest("quarantine-to-user, AP_Q, AP_Q_USER")));

        executeStory();
    }


    public void test_userToQuarantineWorkflow() throws Exception {
        record().startScheduleAgent(new TransferScheduleAgent());

        final ScheduleContract userToQuarantine = userToQuarantineContract("AP_Q_USER", "AP_Q");
        userToQuarantine.getRequest().setId("ID");

        record().startTester("schedule-leader-mock")
              .sendMessage(createScheduleContractMessage(userToQuarantine))
              .then()
              .play(receiveAndAcceptProposal())
              .then()
              .play(receiveResult(CONTROL,
                                  matchControlQuarantine("AP_Q:" + SourceOfData.TRANSFERT_FROM_QUARANTINE)))
              .then()
              .sendMessage(createScheduleContractMessage(createContract("control", userToQuarantine)))
              .then()
              .play(receiveAndAcceptProposal())
              .then()
              .play(receiveResult(QUARANTINE_TRANSFER,
                                  and(matchTransferRequest("quarantine-to-user, AP_Q, AP_Q_USER"),
                                      matchId(null))));

        executeStory();
    }


    private MessageTemplate matchId(String expectedId) {
        return match(new AssertJobRequest("id", expectedId) {
            @Override
            protected String extractActual(JobRequest request) {
                return request.getId();
            }
        });
    }


    private MessageTemplate matchTransferRequest(String expected) {
        return match(new AssertJobRequest("transfert", expected) {
            @Override
            protected String extractActual(JobRequest request) {
                TransferJobRequest transfert = new TransferJobRequest(request);
                return transfert.getTransferType()
                       + ", " + transfert.getQuarantine()
                       + ", " + transfert.getUserQuarantine();
            }
        });
    }


    private MessageTemplate matchControlQuarantine(String expected) {
        return match(new AssertJobRequest("quarantine", expected) {
            @Override
            protected String extractActual(JobRequest request) {
                ControlJobRequest jobRequest = new ControlJobRequest(request);
                return jobRequest.getQuarantineTable() + ":" + jobRequest.getPath();
            }
        });
    }


    private ScheduleContract userToQuarantineContract(String userQuarantine,
                                                      String quarantine) {
        TransferJobRequest transferJobRequest = new TransferJobRequest();
        transferJobRequest.setInitiatorLogin("user_tu");
        transferJobRequest.setTransferType(TransferJobRequest.Transfer.USER_TO_QUARANTINE);
        transferJobRequest.setQuarantine(quarantine);
        transferJobRequest.setUserQuarantine(userQuarantine);

        return createContract(transferJobRequest.toRequest());
    }
}
