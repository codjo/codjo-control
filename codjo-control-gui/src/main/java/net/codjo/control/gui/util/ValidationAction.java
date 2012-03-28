/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.gui.util;
import net.codjo.gui.toolkit.waiting.WaitingPanel;
import net.codjo.mad.client.request.Row;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.mad.gui.request.RequestTable;
/**
 * Action permettant la validation d'un controle.
 *
 * @author $Author: GONNOT $
 * @version $Revision: 1.2 $
 */
public class ValidationAction extends AbstractValidateAction {
    public ValidationAction(GuiContext ctxt, RequestTable table, WaitingPanel waitingPanel) {
        super(ctxt, "Valider", "Valide les contrôles des lignes selectionnées",
              "/images/control.validation.gif", table, waitingPanel);
    }


    @Override
    protected boolean isUpdatableError(Row row) {
        long error = getErrorType(row);
        return (isRecyclableError(error) || isOverridableError(error));
    }


    @Override
    protected String getUpdateValue(Row row) {
        return Long.toString(FLAG_OK);
    }
}
