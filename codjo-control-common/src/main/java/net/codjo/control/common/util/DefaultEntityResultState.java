/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.util;
/**
 * Implantation par défaut d'une etat d'une entité (erreur ou non) .
 *
 * @version $Revision: 1.2 $
 */
class DefaultEntityResultState implements EntityResultState {
    private String errorLog;
    private int errorType;

    DefaultEntityResultState() {
        this.errorType = NO_ERROR;
    }


    DefaultEntityResultState(int errorType, String errorLog) {
        this.errorLog = errorLog;
        this.errorType = errorType;
    }

    public String getErrorLog() {
        return errorLog;
    }


    public int getErrorType() {
        return errorType;
    }
}
