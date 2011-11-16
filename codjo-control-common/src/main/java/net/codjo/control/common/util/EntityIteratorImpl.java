/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.util;
import net.codjo.control.common.UnexpectedControlException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
/**
 * Iterateur sur les entité définit dans la table des controles.
 *
 * <p> Exemple d'utilisation :
 * <pre>
 *    while (iterator.hasNext()) {
 *      Dividend div = (Dividend)iterator.next();
 *      div.setLabel("update");
 *      iterator.update(div);
 *    }
 *    iterator.close();
 * </pre>
 * </p>
 *
 * @author $Author: gonnot $
 * @version $Revision: 1.4 $
 */
class EntityIteratorImpl implements EntityIterator {
    private boolean hasNext = false;
    private boolean loaded = false;
    private Object currentObject;
    private EntityHelper entityHelper;
    private ResultSet rs;
    private Statement stmt;


    EntityIteratorImpl(EntityHelper entityHelper, ResultSet rs, Statement stmt) {
        if (entityHelper == null || rs == null) {
            throw new IllegalArgumentException("EntityIterator mal initialisé.");
        }
        this.entityHelper = entityHelper;
        this.rs = rs;
        this.stmt = stmt;
    }


    public void close() {
        try {
            stmt.close();
        }
        catch (SQLException ex) {
            throw new UnexpectedControlException(ex);
        }
    }


    public boolean hasNext() {
        if (!loaded) {
            loadNextObject();
        }
        return hasNext;
    }


    public Object next() {
        if (!hasNext) {
            throw new IllegalArgumentException("plus d'enregistrement");
        }
        Object obj = currentObject;
        loaded = false;
        return obj;
    }


    public void remove() {
        throw new java.lang.UnsupportedOperationException(
              "La méthode remove() n'est pas encore implémentée.");
    }


    public void update(Object obj, EntityResultState controlError) {
        if (obj != currentObject) {
            throw new IllegalStateException(
                  "Tentative de mise-à-jours de la mauvaise ligne");
        }
        try {

            entityHelper.updateResultSet(rs, obj, controlError);
            rs.updateRow();
        }
        catch (Exception ex) {
            throw new UnexpectedControlException(ex);
        }
    }


    private void loadNextObject() {
        loaded = true;
        try {
            if (rs.next()) {
                hasNext = true;
                currentObject = entityHelper.newObjectFrom(rs);
            }
            else {
                hasNext = false;
                currentObject = null;
            }
        }
        catch (Exception ex) {
            throw new UnexpectedControlException(ex);
        }
    }
}
