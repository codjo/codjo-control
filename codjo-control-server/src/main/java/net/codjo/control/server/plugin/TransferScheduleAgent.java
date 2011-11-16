package net.codjo.control.server.plugin;
import net.codjo.control.common.message.ControlJobRequest;
import net.codjo.control.common.message.SourceOfData;
import net.codjo.control.common.message.TransferJobRequest;
import net.codjo.workflow.common.message.JobRequest;
import net.codjo.workflow.common.message.ScheduleContract;
import net.codjo.workflow.server.api.ScheduleAgent;
/**
 *
 */
class TransferScheduleAgent extends ScheduleAgent {
    TransferScheduleAgent() {
        super(new TransferHandler());
    }


    private static class TransferHandler extends AbstractHandler {
        public boolean acceptContract(ScheduleContract contract) {
            JobRequest request = contract.getRequest();
            return isUserToQuarantineRequest(request)
                   || (ControlServerPlugin.CONTROL_REQUEST_TYPE.equals(request.getType())
                       && contract.getPreviousContract() != null
                       && isUserToQuarantineRequest(contract.getPreviousContract().getRequest()));
        }


        private boolean isUserToQuarantineRequest(JobRequest request) {
            if (!ControlServerPlugin.QUARANTINE_TRANSFER_TYPE.equals(request.getType())) {
                return false;
            }
            TransferJobRequest transfer = new TransferJobRequest(request);
            return transfer.getTransferType() == TransferJobRequest.Transfer.USER_TO_QUARANTINE;
        }


        public JobRequest createNextRequest(ScheduleContract contract) {
            if (ControlServerPlugin.QUARANTINE_TRANSFER_TYPE.equals(contract.getRequest().getType())) {
                TransferJobRequest transfer = new TransferJobRequest(contract.getRequest());
                ControlJobRequest jobRequest = new ControlJobRequest(transfer.getQuarantine());
                jobRequest.addPath(SourceOfData.TRANSFERT_FROM_QUARANTINE);
                return jobRequest.toRequest();
            }
            else {
                TransferJobRequest transfer =
                      new TransferJobRequest(contract.getPreviousContract().getRequest());
                transfer.setTransferType(TransferJobRequest.Transfer.QUARANTINE_TO_USER);
                transfer.setId(null);
                return transfer.toRequest();
            }
        }
    }
}
