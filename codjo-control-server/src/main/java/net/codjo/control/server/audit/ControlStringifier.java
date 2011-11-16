package net.codjo.control.server.audit;
import net.codjo.control.common.message.ControlJobRequest;
import net.codjo.workflow.common.message.JobRequest;
import net.codjo.workflow.server.plugin.StringifierImpl;
/**
 *
 */
public class ControlStringifier extends StringifierImpl {

    public ControlStringifier() {
        super(ControlJobRequest.CONTROL_REQUEST_TYPE);
    }


    public String toString(JobRequest jobRequest) {
        return new ControlJobRequest(jobRequest).getQuarantineTable();
    }
}
