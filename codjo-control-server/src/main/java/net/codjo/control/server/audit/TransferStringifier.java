package net.codjo.control.server.audit;
import net.codjo.control.common.message.TransferJobRequest;
import net.codjo.workflow.common.message.JobRequest;
import net.codjo.workflow.server.plugin.StringifierImpl;
/**
 *
 */
public class TransferStringifier extends StringifierImpl {

    public TransferStringifier() {
        super(TransferJobRequest.QUARANTINE_TRANSFER_TYPE);
    }


    public String toString(JobRequest jobRequest) {
        return new TransferJobRequest(jobRequest).getQuarantine();
    }
}
