/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.manager;
import net.codjo.agent.UserId;
import net.codjo.control.common.loader.ApplicationIP;
import net.codjo.sql.server.ConnectionPoolMock;
import net.codjo.test.common.LogString;
import net.codjo.test.common.mock.ConnectionMock;
import java.sql.Connection;
/**
 *
 */
public class DefaultControlManagerTest extends AbstractControlManagerTestCase {
    private UserId userId;
    private ConnectionPoolMock connectionPool;
    private Connection connection;


    @Override
    protected String acquireConnectionLog() {
        return "connectionPool.getConnection()";
    }


    @Override
    protected String releaseConnectionLog() {
        return "connectionPool.releaseConnection(" + connection + ")";
    }


    @Override
    protected AbstractControlManager createControlManager(ApplicationIP appIp) {
        return new DefaultControlManager(appIp, userId, connectionPool);
    }


    @Override
    protected void doSetup() throws ClassNotFoundException {
        userId = UserId.createId(USER_LOGIN, USER_LOGIN);

        connectionPool = new ConnectionPoolMock(new LogString("connectionPool", log));
        connection = new ConnectionMock();
        connectionPool.mockGetConnection(connection);
    }
}
