package net.codjo.control.server.plugin;
import net.codjo.agent.UserId;
import net.codjo.workflow.common.message.Arguments;
import net.codjo.workflow.common.message.JobRequest;
import net.codjo.workflow.common.organiser.Job;
import net.codjo.workflow.common.organiser.JobMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ControlJobRequestHandlerTest {
    private ControlJobRequestHandler handler = new ControlJobRequestHandler();
    private static final UserId USER_ID = UserId.createId("loginTest", "passwdTest");


    @Test
    public void test_createJob() throws Exception {
        JobRequest request = new JobRequest("control", new Arguments("quarantineTable", "AP_TEZT"));
        Job job = handler.createJob(request, new JobMock(), USER_ID);

        assertEquals("AP_TEZT", job.getTable());
    }


    @Test
    public void test_accept() throws Exception {
        assertTrue(handler.accept(new JobRequest("control")));
        assertFalse(handler.accept(new JobRequest("import")));
    }
}
