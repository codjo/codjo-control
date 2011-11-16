package net.codjo.control.common.message;
import net.codjo.workflow.common.message.JobRequest;
import net.codjo.workflow.common.message.JobRequestWrapper;
import net.codjo.workflow.common.message.JobRequestWrapperTestCase;
/**
 * Classe de test de {@link net.codjo.control.common.message.ControlJobRequest}.
 */
public class ControlJobRequestTest extends JobRequestWrapperTestCase {
    private ControlJobRequest request = new ControlJobRequest();


    public void test_arguments() throws Exception {
        request.setQuarantineTable("Q_AP_TABLE");
        assertEquals("Q_AP_TABLE", request.getQuarantineTable());
    }


    public void test_path() throws Exception {
        assertNull(request.getPath());
        request.addPath("p1");
        assertEquals("p1",request.getPath());
        request.addPath("p2");
        assertEquals("p1/p2",request.getPath());
        request.addPath("p3");
        assertEquals("p1/p2/p3",request.getPath());
    }


    public void test_constructor() throws Exception {
        request = new ControlJobRequest("Q_AP_TABLE");
        assertEquals("Q_AP_TABLE", request.getQuarantineTable());
    }


    @Override
    protected String getJobRequestType() {
        return ControlJobRequest.CONTROL_REQUEST_TYPE;
    }


    @Override
    protected JobRequestWrapper createWrapper(JobRequest jobRequest) {
        return new ControlJobRequest(jobRequest);
    }
}
