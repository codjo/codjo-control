/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.loader;
import net.codjo.control.common.IntegrationPlan;
import net.codjo.control.common.Step;
import net.codjo.control.common.StepsList;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ObjectCreationFactory;
import org.xml.sax.Attributes;
/**
 * Factory appelée par le digester quand un tag <code>&lt;step&gt;</code> est rencontré,
 * pour instancier un objet de type <code>Step</code>. Cette factory gère l'attribut
 * <code>inheritId</code> : quand il est présent, on recherche le step abstrait
 * correspondant et on réplique ses champs dans le nouveau Step (opération de clonage).
 *
 * @author $Author: gonnot $
 * @version $Revision: 1.6 $
 */
public class StepFactory implements ObjectCreationFactory {
    private Digester digester;

    public Object createObject(Attributes attributes)
            throws Exception {
        // Création du nouveau Step
        Step step;
        String inheritId = attributes.getValue("inheritId");
        if (inheritId == null) {
            // Si pas d'attribut inheritId, alors on instancie simplement un nouveau Step
            step = new Step();
        }
        else {
            // Si un attribut inheritId est présent, on doit hériter du step abstrait correspondant.
            // On récupère l'objet IntegrationPlan, qui est le premier objet qui a été
            // empilé dans le digester. La liste des steps abstraits est attaché à
            // cet objet.
            IntegrationPlan ip = (IntegrationPlan)digester.peek(digester.getCount() - 1);
            StepsList abstractSteps = ip.getAbstractSteps();
            if (abstractSteps == null) {
                throw new IllegalArgumentException("A step inherits from abstract step "
                    + inheritId + ", but no abstract step has been declared!");
            }
            Step abstractStep = abstractSteps.getStep(inheritId);
            if (abstractStep == null) {
                throw new IllegalArgumentException("A step inherits from abstract step "
                    + inheritId + ", but this abstract step has not been declared!");
            }
            step = (Step)abstractStep.clone();
        }

        return step;
    }


    public Digester getDigester() {
        return digester;
    }


    public void setDigester(Digester digester) {
        this.digester = digester;
    }
}
