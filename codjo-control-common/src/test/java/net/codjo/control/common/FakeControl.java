/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
/**
 * Description of the Class
 *
 * @author $Author: galaber $
 * @version $Revision: 1.3 $
 */
public class FakeControl extends AbstractControl {
    private ControlException controlError = null;
    private Object controledObject = null;


    public FakeControl() {
    }


    public Object getControledObject() {
        return controledObject;
    }


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
