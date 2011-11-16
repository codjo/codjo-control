/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.server.plugin;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import java.sql.SQLException;
/**
 *
 */
interface ControlerFactory {
    public void init(Agent agent, AclMessage message);


    public Controler createControler() throws SQLException;
}
