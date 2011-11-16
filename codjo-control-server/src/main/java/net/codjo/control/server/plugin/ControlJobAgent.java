/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.server.plugin;
import net.codjo.agent.DFService;
import net.codjo.control.common.message.ControlJobRequest;
import net.codjo.workflow.common.message.JobAudit;
import net.codjo.workflow.common.message.JobException;
import net.codjo.workflow.common.message.JobRequest;
import net.codjo.workflow.common.protocol.JobProtocolParticipant;
import net.codjo.workflow.server.api.JobAgent;
/**
 *
 */
class ControlJobAgent extends JobAgent {

    ControlJobAgent(ControlerFactory controlerFactory) {
        this(controlerFactory, MODE.NOT_DELEGATE);
    }


    ControlJobAgent(ControlerFactory controlerFactory, MODE mode) {
        super(new ControlParticipant(controlerFactory),
              new DFService.AgentDescription(
                    new DFService.ServiceDescription(ControlServerPlugin.CONTROL_REQUEST_TYPE,
                                                     "control-service")), mode);
    }


    private static class ControlParticipant extends JobProtocolParticipant {
        private final ControlerFactory controlerFactory;
        private PostControlAudit postAudit;


        ControlParticipant(ControlerFactory controlerFactory) {
            this.controlerFactory = controlerFactory;
        }


        @Override
        protected void executeJob(JobRequest request) throws JobException {
            ControlJobRequest controlRequest = new ControlJobRequest(request);

            controlerFactory.init(getAgent(), getRequestMessage());
            try {
                Controler controler = controlerFactory.createControler();
                postAudit = controler.execute(controlRequest);
            }
            catch (QuarantineControlException exception) {
                postAudit = exception.getPostControlAudit();
                throwJobException(exception);
            }
            catch (Exception exception) {
                throwJobException(exception);
            }
        }


        @Override
        protected void handlePOST(JobRequest request, JobException failure) {
            JobAudit audit = new JobAudit(JobAudit.Type.POST);
            if (failure != null) {
                audit.setError(new JobAudit.Anomaly(failure.getMessage(), failure));
            }
            if (postAudit != null) {
                postAudit.fill(audit);
            }
            else {
                new PostControlAudit().fill(audit);
            }
            sendAudit(audit);
        }


        private void throwJobException(Exception cause) throws JobException {
            throw new JobException("Error lors des controls ' : " + cause.getLocalizedMessage(), cause);
        }
    }
}
