package net.codjo.control.common.message;
import net.codjo.workflow.common.message.JobRequest;
import net.codjo.workflow.common.message.JobRequestWrapper;
import net.codjo.workflow.common.message.JobRequestWrapperTestCase;
/**
 * Classe de test de {@link TransferJobRequest}.
 */
public class TransferJobRequestTest extends JobRequestWrapperTestCase {

    public void test_arguments() throws Exception {
        TransferJobRequest request = new TransferJobRequest();

        request.setQuarantine("AP_SOURCE");
        assertEquals("AP_SOURCE", request.getQuarantine());

        request.setUserQuarantine("AP_DEST");
        assertEquals("AP_DEST", request.getUserQuarantine());
    }


    public void test_transferType() throws Exception {
        TransferJobRequest request = new TransferJobRequest();

        request.setTransferType(TransferJobRequest.Transfer.QUARANTINE_TO_USER);
        assertEquals(TransferJobRequest.Transfer.QUARANTINE_TO_USER, request.getTransferType());

        request.setTransferType(TransferJobRequest.Transfer.USER_TO_QUARANTINE);
        assertEquals(TransferJobRequest.Transfer.USER_TO_QUARANTINE, request.getTransferType());
    }


    public void test_transferType_undefined() throws Exception {
        TransferJobRequest request = new TransferJobRequest();
        assertNull(request.getTransferType());
    }


    @Override
    protected String getJobRequestType() {
        return TransferJobRequest.QUARANTINE_TRANSFER_TYPE;
    }


    @Override
    protected JobRequestWrapper createWrapper(JobRequest jobRequest) {
        return new TransferJobRequest(jobRequest);
    }
}
