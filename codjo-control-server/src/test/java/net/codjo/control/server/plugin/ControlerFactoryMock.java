package net.codjo.control.server.plugin;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.test.common.LogString;
import java.sql.SQLException;
/**
 * Mock de la classe {@link ControlerFactory}.
 */
class ControlerFactoryMock implements ControlerFactory {
    private LogString log;
    private Controler controler;


    ControlerFactoryMock(LogString log) {
        this.log = log;
        controler = new ControlerMock(new LogString("controler", log));
    }


    public void init(Agent agent, AclMessage message) {
        log.call("init", "agent:" + agent.getAID().getLocalName(),
                 "message:" + AclMessage.performativeToString(message.getPerformative()));
    }


    public Controler createControler()
          throws SQLException {
        log.call("createControler");
        return controler;
    }


    public void mockCreateControler(Controler controlerMock) {
        this.controler = controlerMock;
    }
}
