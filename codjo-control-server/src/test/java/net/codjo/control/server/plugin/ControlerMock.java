/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.server.plugin;
import net.codjo.control.common.message.ControlJobRequest;
import net.codjo.test.common.LogString;
import java.sql.SQLException;
/**
 *
 */
class ControlerMock implements Controler {
    private final LogString log;
    private PostControlAudit controlAudit = new PostControlAudit();


    ControlerMock(LogString log) {
        this.log = log;
    }


    ControlerMock() {
        this(new LogString());
    }


    public PostControlAudit execute(ControlJobRequest jobRequest) throws SQLException {
        log.call("execute", jobRequest.getQuarantineTable(), jobRequest.getInitiatorLogin(),
                 jobRequest.getId());
        return controlAudit;
    }


    public void mockExecuteResult(PostControlAudit audit) {
        this.controlAudit = audit;
    }
}
