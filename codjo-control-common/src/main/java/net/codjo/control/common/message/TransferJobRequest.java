package net.codjo.control.common.message;
import net.codjo.workflow.common.message.JobRequest;
import net.codjo.workflow.common.message.JobRequestWrapper;
/**
 *
 */
public class TransferJobRequest extends JobRequestWrapper {
    private static final String TRANSFER_TYPE = "transfert";
    private static final String QUARANTINE = "quarantine";
    private static final String USER_QUARANTINE = "user-quarantine";
    public static final String QUARANTINE_TRANSFER_TYPE = "quarantine-transfer";



    public TransferJobRequest() {
        this(new JobRequest());
    }


    public TransferJobRequest(JobRequest jobRequest) {
        super(QUARANTINE_TRANSFER_TYPE, jobRequest);
    }


    public void setTransferType(Transfer transfer) {
        setArgument(TRANSFER_TYPE, transfer.toString());
    }


    public Transfer getTransferType() {
        String type = getArgument(TRANSFER_TYPE);
        if (Transfer.USER_TO_QUARANTINE.sameThat(type)) {
            return Transfer.USER_TO_QUARANTINE;
        }
        if (Transfer.QUARANTINE_TO_USER.sameThat(type)) {
            return Transfer.QUARANTINE_TO_USER;
        }
        return null;
    }


    public void setQuarantine(String quarantine) {
        setArgument(QUARANTINE, quarantine);
    }


    public String getQuarantine() {
        return getArgument(QUARANTINE);
    }


    public void setUserQuarantine(String userQuarantine) {
        setArgument(USER_QUARANTINE, userQuarantine);
    }


    public String getUserQuarantine() {
        return getArgument(USER_QUARANTINE);
    }


    public static class Transfer {
        private String transferType;
        public static final Transfer USER_TO_QUARANTINE = new Transfer("user-to-quarantine");
        public static final Transfer QUARANTINE_TO_USER = new Transfer("quarantine-to-user");


        Transfer(String value) {
            this.transferType = value;
        }


        @Override
        public String toString() {
            return transferType;
        }


        boolean sameThat(String type) {
            return transferType.equals(type);
        }
    }
}
