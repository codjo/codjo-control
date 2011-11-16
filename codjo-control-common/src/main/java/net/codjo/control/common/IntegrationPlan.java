/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
import net.codjo.control.common.util.EntityIterator;
import net.codjo.control.common.util.EntityResultState;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.log4j.Logger;
/**
 * Classe representant un plan d'intégration.
 *
 * @author $Author: gonnot $
 * @version $Revision: 1.11 $
 */
public class IntegrationPlan {
    private static final Logger APP = Logger.getLogger(IntegrationPlan.class);
    private static final ControlContext DEFAULT_CONTEXT =
          new ControlContext("auto", "auto", null);
    private Dictionary dictionary = new Dictionary();
    private String controlTableDef;
    private String description;
    private Plan dispatch;
    private String id;
    private PlansList planList;
    private PlansList planListForDelete;
    private Shipment shipment;
    private Entity entity;
    private StepsList abstractSteps = new StepsList();


    public IntegrationPlan() {
    }


    public void setControlTableDef(String controlTableDef) {
        this.controlTableDef = controlTableDef;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }


    public void setDispatch(Plan dispatch) {
        this.dispatch = dispatch;
    }


    public void setEntity(Entity entity) {
        this.entity = entity;
    }


    public void setId(String id) {
        this.id = id;
    }


    public void setPlanList(PlansList planList) {
        this.planList = planList;
    }


    public void setPlanListForDelete(PlansList planListForDelete) {
        this.planListForDelete = planListForDelete;
    }


    public void setShipment(net.codjo.control.common.Shipment shipment) {
        this.shipment = shipment;
    }


    public String getControlTableDef() {
        return controlTableDef;
    }


    public String getDescription() {
        return description;
    }


    public Dictionary getDictionary() {
        return dictionary;
    }


    public Plan getDispatch() {
        return dispatch;
    }


    public Entity getEntity() {
        return entity;
    }


    public String getId() {
        return id;
    }


    public PlansList getPlanList() {
        return planList;
    }


    public PlansList getPlanListForDelete() {
        return planListForDelete;
    }


    public StepsList getAbstractSteps() {
        return abstractSteps;
    }


    public void setAbstractSteps(StepsList abstractSteps) {
        this.abstractSteps.addStepsList(abstractSteps);
    }


    public String getQuarantineTable() {
        if (getShipment() != null && getShipment().getFrom() != null) {
            return getDictionary().replaceVariables(getShipment().getFrom());
        }
        else {
            return null;
        }
    }


    public Step getStep(String stepId) {
        for (Plan plan : getPlanList().getPlans()) {
            for (Step step : plan.getSteps()) {
                if (step.getId().equals(stepId)) {
                    return step;
                }
            }
        }
        throw new IllegalArgumentException("Step " + stepId + " is unknown");
    }


    public net.codjo.control.common.Shipment getShipment() {
        return shipment;
    }


    public void cleanUp(final Connection con) throws SQLException {
        dropControlTable(con);
    }


    /**
     * DEPRECATED.
     *
     * @param con con
     *
     * @throws SQLException     SQLException
     * @throws ControlException si un contrôle échoue
     * @deprecated utiliser la version avec contexte.
     */
    @Deprecated
    public void executeBatchControls(final Connection con)
          throws SQLException, ControlException {
        executeBatchControls(con, DEFAULT_CONTEXT);
    }


    public void executeBatchControls(final Connection con, final ControlContext context)
          throws SQLException, ControlException {
        APP.debug("-------- Lancement des controles " + getId());
        ImmutableControlContext immutableCtxt = new ImmutableControlContext(context);
        for (Plan plan : getPlanList().getPlans()) {
            if (plan.isJavaMode()) {
                EntityIterator iter =
                      getEntity().getEntityHelperForBatch().iterator(con, getControlTableName());
                try {
                    controlRows(plan, immutableCtxt, iter);
                }
                finally {
                    iter.close();
                }
            }
            else {
                String pathOfRequest = "batch";
                if (context != null) {
                    pathOfRequest = context.getPathOfRequest();
                }
                Map<Step, StepAudit> stepAudit = new HashMap<Step, StepAudit>();
                plan.executeMASS(con, getDictionary(), immutableCtxt, pathOfRequest,
                                 getControlTableName(), stepAudit);
                printStepAudit(stepAudit, pathOfRequest, Plan.MASS_TYPE);
            }
        }
    }


    public void executeDispatch(final Connection con, final ControlContext context)
          throws SQLException, ControlException {
        APP.debug("-------- Lancement du dispatch " + getId());
        ImmutableControlContext immutableCtxt = new ImmutableControlContext(context);
        Map<Step, StepAudit> stepAudit = new HashMap<Step, StepAudit>();
        getDispatch().executeMASS(con, getDictionary(), immutableCtxt, Step.FOR_ALL,
                                  getControlTableName(), stepAudit);
        printStepAudit(stepAudit, Step.FOR_ALL, Plan.MASS_TYPE);
    }


    public void executeShipmemt(final Connection con)
          throws SQLException {
        long start = 0;
        if (APP.isDebugEnabled()) {
            start = System.currentTimeMillis();
            APP.debug("-------- Lancement du Shipment " + getId());
        }

        getShipment().execute(con, getDictionary());

        if (APP.isDebugEnabled()) {
            long end = System.currentTimeMillis();
            APP.debug("-------- Fin du Shipment " + getId() + " en "
                      + ((end - start) / 1000) + " s");
        }
    }


    public void init(final Connection con) throws SQLException {
        getDictionary().setNow(new Timestamp(System.currentTimeMillis()));
        createControlTable(con);
    }


    /**
     * DEPRECATED.
     *
     * @param con la connection
     * @param vo  Objet a controler
     *
     * @throws SQLException     Erreur SQL
     * @throws ControlException Control en erreur
     * @deprecated utiliser la version avec contexte.
     */
    @Deprecated
    public void proceedDeletedEntity(Connection con, Object vo)
          throws SQLException, ControlException {
        proceedDeletedEntity(con, vo, DEFAULT_CONTEXT);
    }


    public void proceedDeletedEntity(Connection con, Object vo, final ControlContext ctxt)
          throws SQLException, ControlException {
        if (getPlanListForDelete() == null) {
            return;
        }
        ctxt.setConnection(con);
        proceedEntity(Step.FOR_USER, con, vo, new ImmutableControlContext(ctxt),
                      getPlanListForDelete());
    }


    /**
     * @deprecated utilisé la version {@link #proceedUpdatedEntity(java.sql.Connection,Object,
     *             ControlContext)}
     */
    @Deprecated
    public void proceedEntity(final Connection con, final Object vo,
                              final ControlContext ctxt) throws SQLException, ControlException {
        proceedUpdatedEntity(con, vo, ctxt);
    }


    public void proceedUpdatedEntity(final Connection con, final Object vo,
                                     final ControlContext ctxt) throws SQLException, ControlException {
        APP.debug("-------- Lancement des controles pour une modification " + getId());
        ctxt.setConnection(con);
        proceedEntity(Step.FOR_USER_UPDATE, con, vo, new ImmutableControlContext(ctxt),
                      getPlanList());
    }


    /**
     * DEPRECATED.
     *
     * @param con la connection
     * @param vo  Objet a controler
     *
     * @throws SQLException     Erreur SQL
     * @throws ControlException Control en erreur
     * @deprecated utiliser la version avec contexte.
     */
    @Deprecated
    public void proceedNewEntity(Connection con, Object vo)
          throws SQLException, ControlException {
        proceedNewEntity(con, vo, DEFAULT_CONTEXT);
    }


    public void proceedNewEntity(final Connection con, final Object vo,
                                 final ControlContext ctxt) throws SQLException, ControlException {
        APP.debug("-------- Lancement des controles pour un ajout " + getId());
        ctxt.setConnection(con);
        proceedEntity(Step.FOR_USER_ADD, con, vo, new ImmutableControlContext(ctxt),
                      getPlanList());
    }


    /**
     * DEPRECATED.
     *
     * @param con la connection
     *
     * @throws SQLException     Erreur SQL
     * @throws ControlException Erreur dans MassControl
     * @deprecated utiliser la version avec contexte.
     */
    @Deprecated
    public void proceedQuarantine(Connection con)
          throws SQLException, ControlException {
        proceedQuarantine(con, DEFAULT_CONTEXT);
    }


    public void proceedQuarantine(final Connection con, final ControlContext context)
          throws SQLException, ControlException {
        APP.debug("---------------------- Integration " + getId());
        context.setConnection(con);
        init(con);
        try {
            executeShipmemt(con);
            executeBatchControls(con, context);
            executeDispatch(con, context);
        }
        finally {
            dropControlTable(con);
        }
    }


    protected void createControlTable(Connection con)
          throws SQLException {
        if (getControlTableDef() == null) {
            return;
        }

        // Desactive le drop pour eviter d'avoir des traces d'erreur dans weblo
        //  du style: table '#tmp' ne peut etre drop
        //        dropControlTable(con);
        Statement stmt = con.createStatement();
        try {
            stmt.executeUpdate(getDictionary().replaceVariables(getControlTableDef()));
            if (stmt.getWarnings() != null) {
                throw stmt.getWarnings();
            }
        }
        finally {
            stmt.close();
        }
    }


    public String getControlTableName() {
        return getDictionary().replaceVariables(getShipment().getTo());
    }


    private void controlRows(final Plan plan, final ImmutableControlContext context,
                             final EntityIterator iter) {
        Map<Step, StepAudit> stepAudit = new HashMap<Step, StepAudit>();
        while (iter.hasNext()) {
            Object item = iter.next();

            try {
                plan.executeJAVA(item, getDictionary(), context, Step.FOR_BATCH, stepAudit);
                iter.update(item, null);
            }
            catch (ControlException ex) {
                iter.update(item, ex);
            }
        }
        printStepAudit(stepAudit, Step.FOR_BATCH, Plan.JAVA_TYPE);
    }


    private void printStepAudit(Map<Step, StepAudit> stepAuditMap, String pathOfRequest, String stepType) {
        Set<Entry<Step, StepAudit>> entries = stepAuditMap.entrySet();
        for (Entry<Step, StepAudit> entry : entries) {
            Step step = entry.getKey();
            StepAudit stepAudit = entry.getValue();
            if (stepAudit.getOkRunningCount() > 0) {
                APP.info("For " + stepType + " step " + step.getId() + " match stepFor:" + step.getStepFor()
                         + ", receive:"
                         + pathOfRequest + " --> " + "step called " +
                         stepAudit.getOkRunningCount() + " time(s) in "
                         + stepAudit.getOkRunningDuration() + ".");
            }
            else {
                APP.info("For " + stepType + " step " + step.getId() + " match stepFor:" + step.getStepFor()
                         + ", receive:"
                         + pathOfRequest + " --> " + "step skipped " +
                         stepAudit.getNotOkRunningCount() + " time(s).");
            }
        }
    }


    private void dropControlTable(Connection con)
          throws SQLException {
        if (getControlTableDef() == null) {
            return;
        }
        Statement stmt = con.createStatement();
        try {
            stmt.executeUpdate("drop table " + getControlTableName());
        }
        catch (SQLException e) {
            APP.debug("Drop de la table " + getControlTableName(), e);
        }
        finally {
            stmt.close();
        }
    }


    private void proceedEntity(String stepType, Connection con, Object vo,
                               final ImmutableControlContext context, PlansList plansToProceed)
          throws SQLException, ControlException {
        init(con);
        try {
            boolean firstSql = true;
            for (Plan plan : plansToProceed.getPlans()) {
                if (plan.hasStepFor(stepType)) {
                    if (plan.isJavaMode()) {
                        Map<Step, StepAudit> stepAudit = new HashMap<Step, StepAudit>();
                        plan.executeJAVA(vo, getDictionary(), context, stepType, stepAudit);
                        printStepAudit(stepAudit, stepType, Plan.JAVA_TYPE);
                    }
                    else {
                        String tableName = getControlTableName();
                        if (firstSql) {
                            getEntity().getEntityHelper().insertIntoTable(con, vo, tableName);
                            firstSql = false;
                        }
                        else {
                            getEntity().getEntityHelper().updateTable(con, vo, tableName);
                        }
                        Map<Step, StepAudit> stepAudit = new HashMap<Step, StepAudit>();
                        plan.executeMASS(con, getDictionary(), context, stepType, getControlTableName(),
                                         stepAudit);
                        printStepAudit(stepAudit, stepType, Plan.MASS_TYPE);
                        EntityResultState entityState =
                              getEntity().getEntityHelper().updateObject(con, vo, tableName);
                        throwIfError(entityState);
                    }
                }
            }
        }
        finally {
            cleanUp(con);
        }
    }


    private void throwIfError(EntityResultState entityState)
          throws ControlException {
        if (entityState.getErrorType() != EntityResultState.NO_ERROR) {
            throw new ControlException(entityState);
        }
    }
}
