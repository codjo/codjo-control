/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.gui.util;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.codjo.gui.toolkit.util.ErrorDialog;
import net.codjo.mad.client.request.FieldsList;
import net.codjo.mad.client.request.RequestException;
import net.codjo.mad.client.request.Row;
import net.codjo.mad.gui.framework.AbstractGuiAction;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.mad.gui.framework.SwingRunnable;
import net.codjo.mad.gui.request.RequestTable;
import net.codjo.mad.gui.request.factory.RequestFactory;

import static net.codjo.mad.gui.i18n.InternationalizationUtil.translate;
/**
 * Action permettant un cours ou un Ordre d'eviter ou de bypasser un controle.
 *
 * @author $Author: galaber $
 * @version $Revision: 1.5 $
 */
abstract class AbstractValidateAction extends AbstractGuiAction implements QuarantineUtil {
    protected static final String ERROR_TYPE = "errorType";
    private RequestTable table = null;
    protected Collection<String> groupingColumn = null;


    /**
     * Constructeur de AbstractValidateAction
     *
     * @param ctxt             contexte graphique
     * @param name             Le nom de l'action
     * @param shortDescription tooltips
     * @param icon             icon de l'action
     * @param table            La table a manager
     */
    protected AbstractValidateAction(GuiContext ctxt, String name, String shortDescription,
                                     String icon, RequestTable table) {
        super(ctxt, name, shortDescription, icon);
        setEnabled(false);
        this.table = table;
        table.getSelectionModel().addListSelectionListener(new EnableStateUpdater());
    }


    public void actionPerformed(ActionEvent parm1) {
        table.setEnabled(false);
        this.setEnabled(false);
        getGuiContext().executeTask(new ValidorWorker());
    }


    protected long getErrorType(Row row) {
        return Long.valueOf(row.getFieldValue(ERROR_TYPE));
    }


    protected boolean isOverridableError(long errorNumber) {
        return (errorNumber >= FIRST_OVERRIDABLE_CONTROL);
    }


    protected boolean isRecyclableError(long errorNumber) {
        return (errorNumber > FLAG_OK && errorNumber < FIRST_BLOCKING_CONTROL);
    }


    /**
     * Indique si la ligne est en erreur bloquante, sans erreurs ou si l'erreur est overridable..
     *
     * @param row Description of the Parameter
     *
     * @return <code>true</code> si l'erreur de la ligne peut etre modifie
     */
    protected abstract boolean isUpdatableError(Row row);


    /**
     * Retourne le code erreur a modifier.
     *
     * @param row Description of the Parameter
     *
     * @return la valeur de la colonne errorType
     */
    protected abstract String getUpdateValue(Row row);


    private void reloadTableData() {
        try {
            table.load();
        }
        catch (Exception e) {
            e.printStackTrace();
            ErrorDialog.show(table, "Impossible de recharger la table ", e);
        }
    }


    /**
     * Validation de la ligne <code>row</code>.
     *
     * @param row La ligne a mettre à jours
     *
     * @throws RequestException erreur SQL
     */
    private void validRow(Row row) throws RequestException {
        RequestFactory factory = table.getPreference().getUpdate();

        factory.init(new FieldsList(table.getLoadResult().buildPrimaryKeyListForRow(row)));

        Map<String, String> vals = new HashMap<String, String>();
        vals.put(ERROR_TYPE, getUpdateValue(row));
        getGuiContext().getSender().send(factory.buildRequest(vals));
    }


    private class ValidorWorker extends SwingRunnable {
        private ValidorWorker() {
            super(translate("DefaultQuarantineWindow.lineProcessing", getGuiContext()));
        }


        public void run() {
            Row[] rows = findRowsToValidate();
            for (int i = 0; i < rows.length; i++) {
                Row row = rows[i];
                if (isUpdatableError(row)) {
                    try {
                        validRow(row);
                    }
                    catch (Exception e) {
                        ErrorDialog.show(table, "Impossible de valider la ligne " + i, e);
                    }
                }
            }
        }


        private Row[] findRowsToValidate() {
            Row[] rowsSelected = table.getAllSelectedDataRows();
            Set<Row> rowsGrouping = new HashSet<Row>();
            for (Row rowSelected : rowsSelected) {
                rowsGrouping.add(rowSelected);
                if (!rowSelected.contains("groupId")) {
                    continue;
                }
                String groupIdValue = rowSelected.getFieldValue("groupId");
                if (!"null".equals(groupIdValue)) {
                    for (int index = 0; index < table.getDataSource().getRowCount(); index++) {
                        Row row = table.getDataSource().getRow(index);
                        if (row.getFieldValue("groupId").equals(groupIdValue) && !row.equals(rowSelected)) {
                            rowsGrouping.add(row);
                        }
                    }
                }
            }
            Row[] rows = rowsGrouping.toArray(new Row[rowsGrouping.size()]);
            //noinspection InnerClassTooDeeplyNested
            Arrays.sort(rows, new Comparator<Row>() {
                public int compare(Row o1, Row o2) {
                    return o1.getFieldValue("quarantineId").compareTo(o2.getFieldValue("quarantineId"));
                }
            });
            return rows;
        }


        @Override
        public void updateGui() {
            reloadTableData();
            table.setEnabled(true);
        }
    }

    /**
     * Listener de Mise-a-jours de l'etat Enable de l'action.
     */
    private class EnableStateUpdater implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent event) {
            if (event.getValueIsAdjusting()) {
                return;
            }
            updateEnableState();
        }


        private void updateEnableState() {
            ListSelectionModel selection = table.getSelectionModel();
            if (selection.isSelectionEmpty()) {
                setEnabled(false);
                return;
            }

            Row[] rows = table.getAllSelectedDataRows();
            for (Row row : rows) {
                if (!isUpdatableError(row)) {
                    setEnabled(false);
                    return;
                }
            }

            setEnabled(true);
        }
    }
}
