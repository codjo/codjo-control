/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
import java.sql.Connection;
/**
 * Interface de base d'un traitment java de type en masse.
 *
 * @author $Author: galaber $
 * @version $Revision: 1.3 $
 */
public interface MassControl {
    /**
     * Positionne le contexte d'execution du controle.
     *
     * @param context le contexte
     */
    public void setContext(ControlContext context);


    /**
     * Positionne le nom de la table temporaire
     *
     * @param tabName Le nom de la table de control.
     */
    public void setControlTable(String tabName);


    /**
     * Lance le controle sur l'objet.
     *
     * @param con La connexion JDBC
     * @param dico dictionnaire
     *
     * @throws ControlException Erreur de controle
     */
    public void control(Connection con, Dictionary dico)
            throws ControlException;
}
