/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.loader;
import net.codjo.control.common.IntegrationPlan;
import net.codjo.xml.XmlException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
/**
 * Ensemble des Plan d'integration pour une application.
 *
 * @version $Revision: 1.1 $
 */
public class ApplicationIP {
    private String name;
    private Collection<IntegrationDefinition> integrationDefinitions;
    private Map<String, IntegrationPlan> plansByQuarantine = null;
    private Map<Class, IntegrationPlan> plansByVoClass = null;
    private Map<String, IntegrationPlan> plansById = null;


    public ApplicationIP() {
    }


    public String getName() {
        return name;
    }


    void loadAllPlans() throws IOException, XmlException {
        if (plansByQuarantine != null) {
            return;
        }
        plansByQuarantine = new HashMap<String, IntegrationPlan>();
        plansByVoClass = new HashMap<Class, IntegrationPlan>();
        plansById = new HashMap<String, IntegrationPlan>();

        for (IntegrationDefinition integrationDefinition : getIntegrationDefinitions()) {
            String plan = integrationDefinition.getPlanURI();
            loadPlan(plan);
        }
    }


    public void addIntegrationDefinition(IntegrationDefinition integrationDefinition) {
        if (integrationDefinitions == null) {
            integrationDefinitions = new ArrayList<IntegrationDefinition>();
        }
        integrationDefinitions.add(integrationDefinition);
    }


    public void loadPlan(String planFileName) throws IOException, XmlException {
        IntegrationPlan plan = XmlMapperHelper.loadPlan(planFileName);

        if (plan.getQuarantineTable() != null) {
            plansByQuarantine.put(plan.getQuarantineTable(), plan);
        }

        if (plan.getEntity() != null) {
            plansByVoClass.put(plan.getEntity().getEntityHelper().getBeanClass(), plan);
        }

        if (plan.getId() != null) {
            plansById.put(plan.getId(), plan);
        }
    }


    public IntegrationPlan getPlan(String quarantine) {
        IntegrationPlan plan = plansByQuarantine.get(quarantine);
        if (plan == null) {
            throw new IllegalArgumentException("Aucun plan d'intégration " + "pour "
                                               + quarantine);
        }
        return plan;
    }


    public IntegrationPlan getPlanById(String planId) {
        IntegrationPlan plan = plansById.get(planId);

        if (plan == null) {
            throw new IllegalArgumentException("Aucun plan d'intégration pour l'ID="
                                               + planId);
        }

        return plan;
    }


    public IntegrationPlan getPlan(Class beanClass) {
        IntegrationPlan plan = plansByVoClass.get(beanClass);
        if (plan == null) {
            throw new IllegalArgumentException("Aucun plan d'intégration "
                                               + "pour l'entité " + beanClass);
        }
        return plan;
    }


    public void setName(String name) {
        this.name = name;
    }


    public Collection<IntegrationDefinition> getIntegrationDefinitions() {
        return integrationDefinitions;
    }


    public void setIntegrationDefinitions(Collection<IntegrationDefinition> integrationDefinitions) {
        this.integrationDefinitions = integrationDefinitions;
    }
}
