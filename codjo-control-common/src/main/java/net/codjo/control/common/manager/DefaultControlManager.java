/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.manager;
import net.codjo.agent.UserId;
import net.codjo.control.common.loader.ApplicationIP;
import net.codjo.sql.server.ConnectionPool;
import java.sql.Connection;
import java.sql.SQLException;
/**
 *
 */
public class DefaultControlManager extends AbstractControlManager {
    private final UserId userId;
    private final ConnectionPool connectionPool;


    public DefaultControlManager(ApplicationIP applicationIP,
                                 UserId userId,
                                 ConnectionPool connectionPool) {
        super(applicationIP);
        this.userId = userId;
        this.connectionPool = connectionPool;
    }


    @Override
    protected Connection getConnection() throws SQLException {
        return connectionPool.getConnection();
    }


    @Override
    protected void releaseConnection(Connection connection) throws SQLException {
        connectionPool.releaseConnection(connection);
    }


    @Override
    protected String getCurrentUser() {
        return userId.getLogin();
    }
}
