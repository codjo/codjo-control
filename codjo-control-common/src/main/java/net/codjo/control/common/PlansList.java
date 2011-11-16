/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
import java.util.ArrayList;
import java.util.Collection;
/**
 * Classe qui ecapsule plusieurs plans.
 *
 * @author $Author: gonnot $
 * @version $Revision: 1.3 $
 */
public class PlansList {
    private Collection<Plan> plans = new ArrayList<Plan>();

    public PlansList() {}

    public void setPlans(Collection<Plan> plans) {
        this.plans = plans;
    }


    public Collection<Plan> getPlans() {
        return plans;
    }


    public void addPlan(Plan plan) {
        plans.add(plan);
    }
}
