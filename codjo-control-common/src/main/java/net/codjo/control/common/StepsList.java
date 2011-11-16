/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
import java.util.ArrayList;
import java.util.Collection;
/**
 * DOCUMENT ME!
 */
public class StepsList {
    private Collection<Step> steps = new ArrayList<Step>();


    public StepsList() {
    }


    public void addStep(Step step) {
        this.steps.add(step);
    }


    public void addStepsList(StepsList stepsList) {
        this.steps.addAll(stepsList.steps);
    }


    public Step getStep(String stepId) {
        for (Step step : this.steps) {
            if (stepId.equals(step.getId())) {
                return step;
            }
        }
        return null;
    }


    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("StepList: ");
        for (Step step : steps) {
            buff.append(step.getId());
            buff.append(" ");
        }
        return buff.toString();
    }
}
