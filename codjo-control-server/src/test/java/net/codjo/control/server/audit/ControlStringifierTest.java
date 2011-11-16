package net.codjo.control.server.audit;
import net.codjo.workflow.common.message.Arguments;
import net.codjo.workflow.common.message.JobRequest;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
/**
 *
 */
public class ControlStringifierTest {
    private ControlStringifier stringifier = new ControlStringifier();


    @Test
    public void test_toString() throws Exception {
        Arguments arguments = new Arguments("quarantineTable", "Q_AP_YOUR_TABLE");
        arguments.put("/META-INF/ApplicationIP.xml", "???");
        arguments.put("path.of.data", "my-path");

        assertEquals("Q_AP_YOUR_TABLE", stringifier.toString(new JobRequest("control", arguments)));
    }
}
