/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.server.plugin;
import net.codjo.aspect.AspectException;
import net.codjo.aspect.util.PointRunner;
import net.codjo.aspect.util.PointRunnerException;
import net.codjo.aspect.util.TransactionException;
import net.codjo.control.common.ControlContext;
import net.codjo.control.common.ControlException;
import net.codjo.control.common.IntegrationPlan;
import net.codjo.control.common.message.ControlJobRequest;
import net.codjo.control.server.api.ControlAspectContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
/**
 *
 */
class QuarantineControlManager {
    private static final String TECHNICAL_ERROR_TYPE = "499";
    private static final Logger APP = Logger.getLogger(QuarantineControlManager.class);
    private static final String TECHNICAL_ERROR_PREFIX = "ERREUR TECHNIQUE : ";
    private ControlPreference preference;


    QuarantineControlManager(ControlPreference preference) {
        this.preference = preference;
    }


    public PostControlAudit doQuarantineControls(Connection connection, ControlJobRequest jobRequest)
          throws QuarantineControlException, SQLException {

        String quarantineTable = jobRequest.getQuarantineTable();
        APP.info("Controle de la quarantaine : " + quarantineTable);
        NDC.push("Controle " + quarantineTable);
        try {

            IntegrationPlan plan = preference.getPlan(quarantineTable);

            ControlAspectContext aspectContext = createContext(plan, connection, jobRequest);
            return proceedQuarantine(plan, aspectContext);
        }
        catch (Exception cause) {
            APP.error("Controle de la quarantaine " + quarantineTable + " en echec", cause);
            PostControlAudit audit = tagTechnicalError(connection, quarantineTable, cause);
            throw new QuarantineControlException(audit, cause);
        }
        finally {
            NDC.pop();
        }
    }


    private PostControlAudit proceedQuarantine(IntegrationPlan plan, ControlAspectContext context)
          throws SQLException, AspectException, PointRunnerException, TransactionException, ControlException {
        APP.info("Initialisation...");
        plan.init(context.getConnection());
        try {
            APP.info("Transfert Quarantaine '" + context.getQuarantineTable() + "' vers Table de controle");
            plan.executeShipmemt(context.getConnection());

            APP.info("Tag les lignes en cours de controle");
            int movedRows = tagMovedRows(context.getConnection(), context.getQuarantineTable());

            APP.info("Execution des contrôles");
            plan.executeBatchControls(context.getConnection(), createControlContext(context));

            APP.info("Execution des controles");
            executeDispatch(plan, context);

            APP.info("Construction de l'audit");
            return createPostAudit(context, movedRows);
        }
        finally {
            APP.info("Nettoyage");
            plan.cleanUp(context.getConnection());
        }
    }


    void executeDispatch(final IntegrationPlan plan, final ControlAspectContext context)
          throws PointRunnerException, AspectException, TransactionException {

        PointRunner runner =
              new PointRunner() {
                  public void run() throws PointRunnerException {
                      try {
                          plan.executeDispatch(context.getConnection(), createControlContext(context));
                      }
                      catch (Exception e) {
                          throw new PointRunnerException(e);
                      }
                  }
              };

        preference.getDispatchPoint().run(context.toAspectContext(), runner);
    }


    private PostControlAudit createPostAudit(ControlAspectContext context, int movedRows)
          throws SQLException {
        PostControlAudit audit = new PostControlAudit();
        audit.setBadLineCount(getRowCount(context, "ERROR_TYPE <> 0"));
        audit.setValidLineCount(movedRows - audit.getBadLineCount());
        return audit;
    }


    private int getRowCount(ControlAspectContext context, String whereClause) throws SQLException {
        Statement stmt = context.getConnection().createStatement();
        try {
            ResultSet rs = stmt.executeQuery("select count(1) from " + context.getQuarantineTable()
                                             + " where " + whereClause);
            rs.next();
            return rs.getInt(1);
        }
        finally {
            stmt.close();
        }
    }


    private int tagMovedRows(Connection connection, String quarantineTable) throws SQLException {
        Statement stmt = connection.createStatement();
        try {
            return stmt.executeUpdate("update " + quarantineTable
                                      + " set ERROR_TYPE = null where ERROR_TYPE = 0");
        }
        finally {
            stmt.close();
        }
    }


    private PostControlAudit tagTechnicalError(Connection connection, String quarantineTable, Throwable cause)
          throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("update " + quarantineTable
                                                             + " set ERROR_TYPE = " + TECHNICAL_ERROR_TYPE
                                                             + ", ERROR_LOG = ?"
                                                             + " where ERROR_TYPE is null");
        try {
            stmt.setString(1, getTechnicalErrorMessage(cause));
            int badLine = stmt.executeUpdate();
            return new PostControlAudit(0, badLine);
        }
        finally {
            stmt.close();
        }
    }


    String getTechnicalErrorMessage(Throwable ex) {
        if (ex.getLocalizedMessage() == null) {
            return TECHNICAL_ERROR_PREFIX + ex.getClass().getName();
        }
        return TECHNICAL_ERROR_PREFIX
               + ex.getLocalizedMessage().substring(0, Math.min(ex.getLocalizedMessage().length(),
                                                                254 - TECHNICAL_ERROR_PREFIX.length()));
    }


    private ControlContext createControlContext(ControlAspectContext context) {
        ControlContext controlContext = new ControlContext(context.getUser(),
                                                           context.getJobRequestId(),
                                                           context.getPathOfRequest());
        controlContext.setConnection(context.getConnection());
        return controlContext;
    }


    private static ControlAspectContext createContext(IntegrationPlan ctrl,
                                                      Connection connection,
                                                      ControlJobRequest jobRequest) {
        ControlAspectContext context = new ControlAspectContext();
        context.setControlTableName(ctrl.getControlTableName());
        context.setQuarantineTable(ctrl.getQuarantineTable());
        context.setConnection(connection);
        context.setUser(jobRequest.getInitiatorLogin());
        context.setJobRequestId(jobRequest.getId());
        context.setPathOfRequest(jobRequest.getPath());
        return context;
    }
}
