/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.server.plugin;
import net.codjo.agent.AclMessage;
import net.codjo.agent.Agent;
import net.codjo.control.common.message.ControlJobRequest;
import net.codjo.sql.server.ConnectionPool;
import net.codjo.sql.server.JdbcServiceUtil;
import java.sql.Connection;
import java.sql.SQLException;
/**
 *
 */
class DefaultControlerFactory implements ControlerFactory {
    private final JdbcServiceUtil jdbcServiceUtil;
    private final QuarantineControlManager controlManager;
    private Agent agent;
    private AclMessage message;


    DefaultControlerFactory(JdbcServiceUtil jdbcServiceUtil, ControlPreference preference) {
        this.jdbcServiceUtil = jdbcServiceUtil;
        controlManager = new QuarantineControlManager(preference);
    }


    public void init(Agent anAgent, AclMessage aMessage) {
        this.agent = anAgent;
        this.message = aMessage;
    }


    public Controler createControler()
          throws SQLException {
        ConnectionPool connectionPool = jdbcServiceUtil.getConnectionPool(agent, message);
        return new DefaultControler(connectionPool, controlManager);
    }


    private static class DefaultControler implements Controler {
        private final ConnectionPool connectionPool;
        private final QuarantineControlManager controlManager;


        DefaultControler(ConnectionPool connectionPool, QuarantineControlManager controlManager) {
            this.connectionPool = connectionPool;
            this.controlManager = controlManager;
        }


        public PostControlAudit execute(ControlJobRequest jobRequest)
              throws SQLException, QuarantineControlException {
            Connection connection = connectionPool.getConnection();
            try {
                return controlManager.doQuarantineControls(connection, jobRequest);
            }
            finally {
                connectionPool.releaseConnection(connection);
            }
        }
    }
}
