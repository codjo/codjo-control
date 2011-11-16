/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.util;
/**
 * Description d'une erreur sur une entité.
 *
 * @version $Revision: 1.1.1.1 $
 */
public interface EntityResultState {
    public static final int NO_ERROR = 0;

    public String getErrorLog();


    public int getErrorType();
}
