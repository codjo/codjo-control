/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.server.plugin;
import net.codjo.agent.AclMessage;
import net.codjo.control.common.message.ControlJobRequest;
import net.codjo.test.common.LogString;
import net.codjo.workflow.common.message.JobAudit;
import net.codjo.workflow.server.api.JobAgent;
import net.codjo.workflow.server.api.JobAgent.MODE;
import net.codjo.workflow.server.api.JobAgentTestCase;
/**
 *
 */
public class ControlJobAgentTest extends JobAgentTestCase {
    private ControlerFactoryMock controlFactory;


    public void test_control() throws Exception {
        record().startJobAgent(createJobAgent());

        PostControlAudit postControlAudit = new PostControlAudit();
        postControlAudit.setBadLineCount(2);
        mockAudit(postControlAudit);

        record().startTester("tester")
              .sendMessage(createControlRequest("oggi", "MY_QUARANTINE", "control-321"))
              .then()
              .receiveMessage(hasAuditType(JobAudit.Type.PRE))
              .then()
              .receiveMessage(hasAuditType(JobAudit.Type.POST))
              .assertReceivedMessage(hasAuditWarningMessage("Il y a 2 ligne(s) placées en quarantaine."));

        executeStory();

        log.assertContent(
              "controlFactory.init(agent:job-agent, message:REQUEST)"
              + ", controlFactory.createControler()"
              + ", controler.execute(MY_QUARANTINE, oggi, control-321)");
    }


    @Override
    protected JobAgent createJobAgent() throws Exception {
        controlFactory = new ControlerFactoryMock(new LogString("controlFactory", log));
        return new ControlJobAgent(controlFactory, MODE.NOT_DELEGATE);
    }


    @Override
    protected String getServiceType() throws Exception {
        return ControlServerPlugin.CONTROL_REQUEST_TYPE;
    }


    private void mockAudit(PostControlAudit postControlAudit) {
        ControlerMock controlerMock = new ControlerMock(new LogString("controler", log));
        controlFactory.mockCreateControler(controlerMock);
        controlerMock.mockExecuteResult(postControlAudit);
    }


    private AclMessage createControlRequest(String initiator, String quarantineTable, String requestId) {
        ControlJobRequest request = new ControlJobRequest();
        request.setId(requestId);
        request.setInitiatorLogin(initiator);
        request.setQuarantineTable(quarantineTable);
        return createJobRequestMessage(request.toRequest());
    }
}
