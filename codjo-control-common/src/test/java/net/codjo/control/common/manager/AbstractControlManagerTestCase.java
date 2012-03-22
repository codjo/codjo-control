/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.manager;
import net.codjo.control.common.ControlContext;
import net.codjo.control.common.ControlException;
import net.codjo.control.common.IntegrationPlan;
import net.codjo.control.common.i18n.InternationalizationFixture;
import net.codjo.control.common.loader.ApplicationIP;
import net.codjo.i18n.common.plugin.InternationalizationPlugin;
import net.codjo.test.common.LogString;
import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.NamingException;
import junit.framework.TestCase;
/**
 * TODO Classe dupliqué de codjo-control (car encore sous maven1) : net.codjo.control.common.manager.AbstractControlManagerTestCase
 */
public abstract class AbstractControlManagerTestCase extends TestCase {
    private InternationalizationFixture i18nFixture = new InternationalizationFixture();
    protected static final String USER_LOGIN = "smith";
    protected LogString log = new LogString();
    private ApplicationIPMock applicationIP;


    protected abstract String acquireConnectionLog();


    protected abstract String releaseConnectionLog();


    protected abstract AbstractControlManager createControlManager(ApplicationIP appIp);


    public void test_controlAdded() throws Exception {
        AbstractControlManager manager = createControlManager(applicationIP);

        manager.controlNewEntity("new Object");

        log.assertContent("applicationIp.getPlan(java.lang.String), "
                          + acquireConnectionLog()
                          + ", plan.proceedNewEntity(connection, new Object, context(smith)), "
                          + releaseConnectionLog());
    }


    public void test_controlAdded_controlFailure()
          throws Exception {
        net.codjo.control.common.ControlException controlFailure =
              new ControlException(0, "");
        applicationIP.mockControlFailure(controlFailure);

        AbstractControlManager manager = createControlManager(applicationIP);

        try {
            manager.controlNewEntity("new Object");
            fail();
        }
        catch (ControlException ex) {
            assertSame(controlFailure, ex);
        }

        assertTrue(log.getContent().contains(releaseConnectionLog()));
    }


    public void test_controlDeleted() throws Exception {
        AbstractControlManager manager = createControlManager(applicationIP);

        manager.controlDeletedEntity("deleted Object");

        log.assertContent("applicationIp.getPlan(java.lang.String), "
                          + acquireConnectionLog()
                          + ", plan.proceedDeletedEntity(connection, deleted Object, context(smith)), "
                          + releaseConnectionLog());
    }


    public void test_controlUpdated() throws Exception {
        AbstractControlManager manager = createControlManager(applicationIP);

        manager.controlUpdatedEntity("updated Object");

        log.assertContent("applicationIp.getPlan(java.lang.String), "
                          + acquireConnectionLog()
                          + ", plan.proceedUpdatedEntity(connection, updated Object, context(smith)), "
                          + releaseConnectionLog());
    }


    @Override
    protected final void setUp() throws Exception {
        i18nFixture.doSetUp();
        applicationIP = new ApplicationIPMock(new LogString("applicationIp", log));
        doSetup();
    }


    protected abstract void doSetup() throws NamingException, ClassNotFoundException;


    private static class ApplicationIPMock extends ApplicationIP {
        private LogString log;
        private ControlException controlFailure;


        ApplicationIPMock(LogString log) {
            this.log = log;
        }


        @Override
        public IntegrationPlan getPlan(Class beanClass) {
            log.call("getPlan", beanClass.getName());
            IntegrationPlanMock planMock =
                  new IntegrationPlanMock(new LogString("plan", log));
            planMock.mockControlFailure(controlFailure);
            return planMock;
        }


        public void mockControlFailure(ControlException failure) {
            controlFailure = failure;
        }
    }

    private static class IntegrationPlanMock extends IntegrationPlan {
        private final LogString log;
        private ControlException controlFailure;


        IntegrationPlanMock(LogString log) {
            this.log = log;
        }


        @Override
        public void proceedUpdatedEntity(final Connection con, final Object vo,
                                         final ControlContext ctxt) throws SQLException, ControlException {
            checkControlFailure();
            log.call("proceedUpdatedEntity", "connection", vo,
                     "context(" + ctxt.getUser() + ")");
        }


        @Override
        public void proceedNewEntity(final Connection con, final Object vo,
                                     final ControlContext ctxt) throws SQLException, ControlException {
            checkControlFailure();
            log.call("proceedNewEntity", "connection", vo,
                     "context(" + ctxt.getUser() + ")");
        }


        @Override
        public void proceedDeletedEntity(final Connection con, final Object vo,
                                         final ControlContext ctxt) throws SQLException, ControlException {
            checkControlFailure();
            log.call("proceedDeletedEntity", "connection", vo,
                     "context(" + ctxt.getUser() + ")");
        }


        private void checkControlFailure() throws ControlException {
            if (controlFailure != null) {
                throw controlFailure;
            }
        }


        public void mockControlFailure(ControlException failure) {
            this.controlFailure = failure;
        }
    }
}
