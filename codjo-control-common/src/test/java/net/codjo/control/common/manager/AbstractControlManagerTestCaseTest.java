/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.manager;
import net.codjo.control.common.loader.ApplicationIP;
import net.codjo.test.common.mock.ConnectionMock;
import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.NamingException;
/**
 *
 */
public class AbstractControlManagerTestCaseTest extends AbstractControlManagerTestCase {
    @Override
    protected String acquireConnectionLog() {
        return "getConnection()";
    }


    @Override
    protected String releaseConnectionLog() {
        return "releaseConnection()";
    }


    @Override
    protected AbstractControlManager createControlManager(ApplicationIP appIp) {
        return new AbstractControlManagerImpl(appIp);
    }


    @Override
    protected void doSetup() throws NamingException {
    }


    private class AbstractControlManagerImpl extends AbstractControlManager {
        AbstractControlManagerImpl(ApplicationIP applicationIP) {
            super(applicationIP);
        }


        @Override
        protected Connection getConnection() throws SQLException, NamingException {
            log.call("getConnection");
            return new ConnectionMock();
        }


        @Override
        protected void releaseConnection(Connection con)
              throws SQLException {
            log.call("releaseConnection");
        }


        @Override
        protected String getCurrentUser() {
            return USER_LOGIN;
        }
    }
}
