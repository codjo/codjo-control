/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
import java.io.PrintStream;
import java.io.PrintWriter;
import net.codjo.control.common.util.EntityResultState;

import static net.codjo.i18n.common.plugin.InternationalizationUtil.translate;
/**
 * Control en erreur.
 *
 * @author $Author: gonnot $
 * @version $Revision: 1.4 $
 */
public class ControlException extends Exception implements EntityResultState {
    private Exception cause = null;
    private String errorLog;
    private int errorType;


    public ControlException(int errorType, String errorLog) {
        this(errorType, errorLog, null);
    }


    public ControlException(EntityResultState error) {
        this(error.getErrorType(), error.getErrorLog(), null);
    }


    public ControlException(int errorType, Exception cause) {
        this(errorType, cause.getLocalizedMessage(), cause);
    }


    public ControlException(int errorType, String errorLog, Exception cause) {
        super(translate("ControlException.error") + " " + errorType + " - " + errorLog);
        this.errorLog = errorLog;
        this.errorType = errorType;
        this.cause = cause;
    }


    @Override
    public Exception getCause() {
        return cause;
    }


    public String getErrorLog() {
        return errorLog;
    }


    public int getErrorType() {
        return errorType;
    }


    @Override
    public void printStackTrace(PrintWriter writer) {
        super.printStackTrace(writer);
        if (getCause() != null) {
            writer.println(" ---- cause ---- ");
            getCause().printStackTrace(writer);
        }
    }


    @Override
    public void printStackTrace() {
        super.printStackTrace();
        if (getCause() != null) {
            System.err.println(" ---- cause ---- ");
            getCause().printStackTrace();
        }
    }


    @Override
    public void printStackTrace(PrintStream stream) {
        super.printStackTrace(stream);
        if (getCause() != null) {
            stream.println(" ---- cause ---- ");
            getCause().printStackTrace(stream);
        }
    }
}
