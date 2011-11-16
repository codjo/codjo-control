/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.gui.util;
import net.codjo.mad.client.request.Row;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.mad.gui.request.RequestTable;
/**
 * Action permettant le forçage d'un controle.
 *
 * @author $Author: GONNOT $
 * @version $Revision: 1.2 $
 */
public class ForceAction extends AbstractValidateAction {
    public ForceAction(GuiContext ctxt, RequestTable table) {
        super(ctxt, "Forcer", "Force les contrôles des lignes selectionnées", "/images/control.force.gif",
              table);
    }


    @Override
    protected boolean isUpdatableError(Row row) {
        return (getErrorType(row) >= FIRST_OVERRIDABLE_CONTROL);
    }


    @Override
    protected String getUpdateValue(Row row) {
        return Long.toString(-getErrorType(row));
    }
}
