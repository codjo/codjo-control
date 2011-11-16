/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.log4j.Logger;
/**
 * Classe representant un plan (un ensemble) d'etapes à executer.
 *
 * @author $Author: palmont $
 * @version $Revision: 1.3 $
 */
public class Plan {
    public static final String JAVA_TYPE = "java";
    public static final String MASS_TYPE = "mass";
    private Set<Step> steps;
    private String type;
    private static final Logger APP = Logger.getLogger(IntegrationPlan.class);


    public Plan() {
    }


    public void setSteps(Collection<Step> steps) {
        this.steps = new TreeSet<Step>(new ByAscendigPriority());
        this.steps.addAll(steps);
    }


    public void setType(String type) {
        this.type = type;
    }


    public boolean isJavaMode() {
        return JAVA_TYPE.equals(getType());
    }


    public Step getStep(String stepId) {
        for (Step step : steps) {
            if (step.getId().equals(stepId)) {
                return step;
            }
        }
        throw new IllegalArgumentException("Step inconnue dans le plan " + stepId);
    }


    public Collection<Step> getSteps() {
        return steps;
    }


    public String getType() {
        return type;
    }


    public void addStep(Step step) {
        if (steps == null) {
            steps = new TreeSet<Step>(new ByAscendigPriority());
        }
        steps.add(step);
    }


    public void executeJAVA(Object obj, Dictionary dico, ControlContext context,
                            String pathOfRequest, Map<Step, StepAudit> stepAuditMap)
          throws ControlException {
        if (!isJavaMode()) {
            throw new IllegalArgumentException("Les plans qui ne sont pas de type "
                                               + "Java ne peuvent être appele en mode JAVA");
        }
        for (Step step : getSteps()) {
            StepAudit stepAudit = getStepAudit(step, stepAuditMap);
            boolean isOkToRunStep = step.isStepFor(pathOfRequest);

            if (isOkToRunStep) {
                step.execute(obj, dico, context);
                stepAudit.incrementOkRunningCount();
            }
            else {
                stepAudit.incrementNotOkRunningCount();
            }
        }
    }


    public void executeMASS(Connection con, Dictionary dico, ControlContext context,
                            String pathOfRequest, String controlTableName, Map<Step, StepAudit> stepAuditMap)
          throws SQLException, ControlException {
        if (isJavaMode()) {
            throw new IllegalArgumentException("Les plans de type Java ne peuvent "
                                               + "être appele en mode SQL");
        }
        for (Step step : getSteps()) {
            StepAudit stepAudit = getStepAudit(step, stepAuditMap);
            boolean mass = MASS_TYPE.equals(getType()) || getType() == null;
            boolean isOkToRunStep = step.isStepFor(pathOfRequest);
            if (isOkToRunStep) {
                step.execute(con, dico, controlTableName, mass, context);
                stepAudit.incrementOkRunningCount();
            }
            else {
                stepAudit.incrementNotOkRunningCount();
            }
        }
    }


    public boolean hasStepFor(String stepFor) {
        if (getSteps() == null) {
            return false;
        }
        for (Step step : getSteps()) {
            if (step.isStepFor(stepFor)) {
                return true;
            }
        }
        return false;
    }


    private StepAudit getStepAudit(Step step, Map<Step, StepAudit> stepAudit) {
        if (!stepAudit.containsKey(step)) {
            stepAudit.put(step, new StepAudit());
        }

        return stepAudit.get(step);
    }


    private static class ByAscendigPriority implements Comparator<Step> {
        public int compare(Step step1, Step step2) {
            int valA = step1.getPriority();
            int valB = step2.getPriority();
            if (valA < valB) {
                return -1;
            }
            else if (valA == valB && step1 != step2) {
                throw new IllegalArgumentException("La priorité: " + valA
                                                   + " est utilisé plus d'une fois !");
            }
            else if (valA == valB) {
                return 0;
            }
            else {
                return 1;
            }
        }
    }
}
