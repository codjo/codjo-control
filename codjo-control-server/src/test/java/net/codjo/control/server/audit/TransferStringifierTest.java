package net.codjo.control.server.audit;
import net.codjo.workflow.common.message.Arguments;
import net.codjo.workflow.common.message.JobRequest;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
/**
 *
 */
public class TransferStringifierTest {
    private TransferStringifier stringifier = new TransferStringifier();


    @Test
    public void test_toString() throws Exception {
        Arguments arguments = new Arguments("transfert", "user-to-quarantine");
        arguments.put("quarantine", "Q_AP_YOUR_TABLE");
        arguments.put("user-quarantine", "Q_AP_MY_TABLE");

        assertEquals("Q_AP_YOUR_TABLE",
                     stringifier.toString(new JobRequest("quarantine-transfer", arguments)));
    }
}
