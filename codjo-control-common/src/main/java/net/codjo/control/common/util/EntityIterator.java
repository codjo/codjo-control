/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.util;
import java.util.Iterator;
/**
 * Iterateur sur les entité définit dans la table des controles.
 * 
 * <p>
 * Exemple d'utilisation :
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
 * @version $Revision: 1.1.1.1 $
 */
public interface EntityIterator extends Iterator<Object> {
    public void close();


    public void update(Object obj, EntityResultState error);

}
