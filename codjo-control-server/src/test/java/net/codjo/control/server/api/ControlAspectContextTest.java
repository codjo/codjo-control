package net.codjo.control.server.api;
import java.sql.Connection;
import net.codjo.aspect.AspectContext;
import net.codjo.aspect.util.TransactionalPoint;
import net.codjo.test.common.mock.ConnectionMock;
import junit.framework.TestCase;
/**
 * Classe de test de {@link ControlAspectContext}.
 */
public class ControlAspectContextTest extends TestCase {
    private Connection connectionMock = new ConnectionMock().getStub();


    public void test_toAspectContext() throws Exception {
        ControlAspectContext context = new ControlAspectContext();

        context.setControlTableName("#CONTROL_PORTFOLIO");
        context.setQuarantineTable("Q_AP_PORTFOLIO");
        context.setConnection(connectionMock);
        context.setUser("smith");
        context.setJobRequestId("control-327");
        context.setPathOfRequest("1/2/3");

        AspectContext aspectContext = context.toAspectContext();

        assertNotNull(aspectContext);
        assertEquals("#CONTROL_PORTFOLIO", aspectContext.get(ControlAspectContext.CONTROL_TABLE_KEY));
        assertEquals("Q_AP_PORTFOLIO", aspectContext.get(TransactionalPoint.ARGUMENT));
        assertSame(connectionMock, aspectContext.get(TransactionalPoint.CONNECTION));
        assertEquals("smith", aspectContext.get(ControlAspectContext.USER_KEY));
        assertEquals("control-327", aspectContext.get(ControlAspectContext.REQUEST_ID_KEY));
        assertEquals("1/2/3", aspectContext.get(ControlAspectContext.PATH_OF_REQUEST_KEY));
    }


    public void test_fromAspectContext() throws Exception {
        ControlAspectContext context = new ControlAspectContext(createAspectContext());

        assertEquals("#CONTROL_PORTFOLIO", context.getControlTableName());
        assertEquals("Q_AP_PORTFOLIO", context.getQuarantineTable());
        assertSame(connectionMock, context.getConnection());
        assertEquals("smith", context.getUser());
        assertEquals("control-327", context.getJobRequestId());
        assertEquals("1/2/3", context.getPathOfRequest());
    }


    private AspectContext createAspectContext() {
        ControlAspectContext context = new ControlAspectContext();

        context.setControlTableName("#CONTROL_PORTFOLIO");
        context.setQuarantineTable("Q_AP_PORTFOLIO");
        context.setConnection(connectionMock);
        context.setUser("smith");
        context.setJobRequestId("control-327");
        context.setPathOfRequest("1/2/3");
        return context.toAspectContext();
    }
}
