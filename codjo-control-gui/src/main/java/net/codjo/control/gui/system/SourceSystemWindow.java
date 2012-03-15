/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.gui.system;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import net.codjo.gui.toolkit.swing.GenericRenderer;
import net.codjo.i18n.common.TranslationManager;
import net.codjo.i18n.gui.InternationalizableContainer;
import net.codjo.i18n.gui.TranslationNotifier;
import net.codjo.mad.client.request.RequestException;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.mad.gui.i18n.InternationalizableRequestTable;
import net.codjo.mad.gui.i18n.InternationalizationUtil;
import net.codjo.mad.gui.request.PreferenceFactory;
import net.codjo.mad.gui.request.RequestTable;
import net.codjo.mad.gui.request.RequestToolBar;

import static net.codjo.mad.gui.i18n.InternationalizationUtil.*;

class SourceSystemWindow extends JInternalFrame implements InternationalizableContainer {
    private JScrollPane scrollPane = new JScrollPane();
    private RequestTable requestTable = new RequestTable();
    private RequestToolBar toolBar = new RequestToolBar();
    private TranslationManager translationManager;
    private TranslationNotifier notifier;
    private GuiContext guiContext;


    SourceSystemWindow(GuiContext ctxt) throws RequestException {
        super("Système", true, true, true, true);
        guiContext = ctxt;
        jbInit();
        initInternationalization(ctxt);
        requestTable.setEditable(true);
        requestTable.setPreference("SourceSystemWindow");
        initDecimalSeparatorStuff();
        initDateFormatStuff();
        toolBar.setHasExcelButton(true);
        toolBar.setHasValidationButton(true);
        toolBar.setHasUndoRedoButtons(true);
        toolBar.init(ctxt, requestTable);
        requestTable.load();
    }


    private void initInternationalization(GuiContext context) {
        notifier = retrieveTranslationNotifier(context);
        translationManager = retrieveTranslationManager(context);
        notifier.addInternationalizableContainer(this);
    }


    public void addInternationalizableComponents(TranslationNotifier translationNotifier) {
        translationNotifier.addInternationalizableComponent(this, "SourceSystemWindow.title");
        translationNotifier.addInternationalizableComponent(
              new InternationalizableRequestTable(PreferenceFactory.getPreference("SourceSystemWindow"), requestTable));
    }


    private void initDateFormatStuff() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("yyyyMM", translate("SourceSystemWindow.period", guiContext) + " (197303)");
        map.put("dd-MM-yy", "jj-mm-aa (18-03-73)");
        map.put("dd-MM-yyyy", "jj-mm-aaaa (18-03-1973)");
        map.put("yyyyMMdd", "aaaammjj (19661010)");
        map.put("dd/MM/yy", "jj/mm/aa (18/03/73)");
        map.put("dd/MM/yyyy", "jj/mm/aaaa (18/03/1973)");
        map.put("ddMMyyyy", "jjmmaaaa (05091977)");
        SourceSystemRenderer renderer = new SourceSystemRenderer(map);

        JComboBox combo = new JComboBox(new String[]{
              "dd-MM-yy", "dd-MM-yyyy", "yyyyMMdd", "dd/MM/yy", "dd/MM/yyyy",
              "ddMMyyyy", "yyyyMM"
        });
        combo.setEditable(true);
        combo.setName("SourceSystemWindow.dateFormat");
        combo.setRenderer(renderer);

        requestTable.setCellEditor("dateFormat", new DefaultCellEditor(combo));
        requestTable.setCellRenderer("dateFormat", renderer);
    }


    private void initDecimalSeparatorStuff() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(".", translate("SourceSystemWindow.dot", guiContext));
        map.put(",", translate("SourceSystemWindow.comma", guiContext));
        GenericRenderer renderer = new GenericRenderer(map);

        JComboBox combo = new JComboBox(new String[]{".", ","});
        combo.setRenderer(renderer);

        requestTable.setCellEditor("decimalSeparator", new DefaultCellEditor(combo));
        requestTable.setCellRenderer("decimalSeparator", renderer);
    }


    private void jbInit() {
        this.getContentPane().add(scrollPane, BorderLayout.CENTER);
        this.getContentPane().add(toolBar, BorderLayout.SOUTH);
        scrollPane.getViewport().add(requestTable, null);
    }


    private class SourceSystemRenderer extends GenericRenderer {
        SourceSystemRenderer(Map traductTable) {
            super(traductTable);
        }


        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
            JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            return setLabelColor(label, value, isSelected);
        }


        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            return setLabelColor(label, value, isSelected);
        }


        private Component setLabelColor(JLabel label, Object value, boolean isSelected) {
            if (label.getForeground().equals(Color.red)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(value.toString());
                Date currentDate = new Date();
                try {
                    dateFormat.format(currentDate);
                    if (isSelected) {
                        label.setForeground(UIManager.getColor("Table.selectionForeground"));
                    }
                    else {
                        label.setForeground(UIManager.getColor("Table.foreground"));
                    }
                }
                catch (IllegalArgumentException e) {
                    label.setForeground(Color.red);
                }
            }
            return label;
        }
    }
}
