/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
/**
 * Interface de base d'un contrôle java de type ligne à ligne.
 *
 * @author $Author: gonnot $
 * @version $Revision: 1.2 $
 */
public interface Control {
    /**
     * Positionne le code erreur de se control.
     *
     * @param errorCode La nouvelle valeur de errorCode
     */
    public void setErrorCode(int errorCode);


    /**
     * Positionne le contexte d'execution du controle.
     *
     * @param context le contexte
     */
    public void setContext(ControlContext context);


    /**
     * Lance le controle sur l'objet.
     *
     * @param obj l'objet à controler
     * @param dico dictionnaire
     *
     * @throws ControlException Erreur de controle
     */
    public void control(Object obj, Dictionary dico)
            throws ControlException;
}
