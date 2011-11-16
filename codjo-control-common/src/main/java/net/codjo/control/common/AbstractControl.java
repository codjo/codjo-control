/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
/**
 * Classe abstraite facilitant l'mplantation d'un control.
 *
 * @author $Author: gonnot $
 * @version $Revision: 1.2 $
 */
public abstract class AbstractControl implements Control {
    private int errorCode;
    private ControlContext context;

    protected AbstractControl() {}

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }


    public int getErrorCode() {
        return errorCode;
    }


    public ControlContext getContext() {
        return context;
    }


    public void setContext(ControlContext context) {
        this.context = context;
    }
}
