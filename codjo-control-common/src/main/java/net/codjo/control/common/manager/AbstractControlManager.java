package net.codjo.control.common.manager;
import net.codjo.control.common.IntegrationPlan;
import net.codjo.control.common.ControlContext;
import net.codjo.control.common.ControlException;
import net.codjo.control.common.UnexpectedControlException;
import net.codjo.control.common.loader.ApplicationIP;
import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.NamingException;
/**
 *
 */
public abstract class AbstractControlManager
    implements ControlManager {
    private static final ControlType NEW_CONTROL_TYPE =
        new ControlType() {
            public void proceed(final IntegrationPlan integrationPlan,
                                final Connection con, final Object vo, final ControlContext ctxt)
                    throws SQLException, ControlException {
                integrationPlan.proceedNewEntity(con, vo, ctxt);
            }
        };
    private static final ControlType DELETE_CONTROL_TYPE =
        new ControlType() {
            public void proceed(final IntegrationPlan integrationPlan,
                                final Connection con, final Object vo, final ControlContext ctxt)
                    throws SQLException, ControlException {
                integrationPlan.proceedDeletedEntity(con, vo, ctxt);
            }
        };
    private static final ControlType UPDATE_CONTROL_TYPE =
        new ControlType() {
            public void proceed(final IntegrationPlan integrationPlan,
                                final Connection con, final Object vo, final ControlContext ctxt)
                    throws SQLException, ControlException {
                integrationPlan.proceedUpdatedEntity(con, vo, ctxt);
            }
        };
    private ApplicationIP applicationIP = null;

    protected AbstractControlManager(ApplicationIP applicationIP) {
        this.applicationIP = applicationIP;
    }

    public void controlNewEntity(Object entity) throws ControlException {
        controlEntity(entity, AbstractControlManager.NEW_CONTROL_TYPE);
    }


    public void controlUpdatedEntity(Object entity)
            throws ControlException {
        controlEntity(entity, AbstractControlManager.UPDATE_CONTROL_TYPE);
    }


    public void controlDeletedEntity(Object entity)
            throws ControlException {
        controlEntity(entity, AbstractControlManager.DELETE_CONTROL_TYPE);
    }


    public ApplicationIP getApplicationIP() {
        return applicationIP;
    }


    protected abstract Connection getConnection()
            throws SQLException, NamingException;


    protected abstract void releaseConnection(Connection con)
            throws SQLException;


    protected ControlContext newGuiContext() {
        return new ControlContext(getCurrentUser(), "IHM",null);
    }


    protected abstract String getCurrentUser();


    private void controlEntity(Object vo, ControlType controlType)
            throws ControlException {
        Connection con;
        try {
            IntegrationPlan ctrl = applicationIP.getPlan(vo.getClass());
            con = getConnection();
            try {
                controlType.proceed(ctrl, con, vo, newGuiContext());
            }
            finally {
                releaseConnection(con);
            }
        }
        catch (ControlException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new UnexpectedControlException(ex);
        }
    }

    private static interface ControlType {
        public void proceed(final IntegrationPlan integrationPlan, final Connection con,
                            final Object vo, final ControlContext ctxt)
                throws SQLException, ControlException;
    }
}
