/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.manager;
import net.codjo.control.common.ControlException;
/**
 *
 */
public interface ControlManager {
    public void controlNewEntity(Object entity) throws ControlException;


    public void controlUpdatedEntity(Object entity)
            throws ControlException;


    public void controlDeletedEntity(Object entity)
            throws ControlException;
}
