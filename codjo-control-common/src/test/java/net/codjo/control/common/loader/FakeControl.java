/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.loader;
import net.codjo.control.common.AbstractControl;
import net.codjo.control.common.ControlException;
import net.codjo.control.common.Dictionary;
/**
 * DOCUMENT ME!
 *
 * @version $Revision: 1.1 $
 */
public class FakeControl extends AbstractControl {
    private ControlException controlError = null;
    private Object controledObject = null;

    public FakeControl() {}


    public FakeControl(ControlException controlError) {
        this.controlError = controlError;
    }

    public void control(Object obj, Dictionary dico)
            throws ControlException {
        controledObject = obj;
        if (controlError != null) {
            throw controlError;
        }
    }


    public boolean hasBeenCalled() {
        return controledObject != null;
    }
}
