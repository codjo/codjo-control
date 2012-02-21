/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
import java.sql.Connection;
import junit.framework.TestCase;
import net.codjo.test.common.mock.ConnectionMock;
/**
 */
public class ControlContextTest extends TestCase {

    public void test_global() throws Exception {
        ControlContext context = new ControlContext();
        assertNull(context.getUser());
        assertNull(context.getCurrentRequestId());

        context.setCurrentRequestId("id");
        context.setUser("bobo");
        assertEquals("id", context.getCurrentRequestId());
        assertEquals("bobo", context.getUser());
        assertNull(context.getPathOfRequest());

        context = new ControlContext("bobo", "id", "p1");
        assertEquals("id", context.getCurrentRequestId());
        assertEquals("bobo", context.getUser());
        assertEquals("p1", context.getPathOfRequest());
    }


    public void test_connection() throws Exception {
        ControlContext controlContext = new ControlContext();
        Connection connectionMock = new ConnectionMock().getStub();
        controlContext.setConnection(connectionMock);
        assertSame(connectionMock, controlContext.getConnection());
    }
}
