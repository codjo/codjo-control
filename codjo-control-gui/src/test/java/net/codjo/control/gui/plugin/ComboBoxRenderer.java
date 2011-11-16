package net.codjo.control.gui.plugin;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

class ComboBoxRenderer extends JLabel implements ListCellRenderer {
    private static final String NULL = "null";
    private static final String NULL_LABEL = " ";
    private String nullLabel = NULL_LABEL;


    ComboBoxRenderer() {
        setOpaque(true);
    }


    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        }
        else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        if (index != -1) {
            this.setText(getElementAt(index));
        }
        else {
            this.setText(nullLabel);
        }
        return this;
    }


    private String getElementAt(int viewIndex) {
        return rendererValue("testRenderer " + viewIndex);
    }


    private String rendererValue(String value) {
        if (NULL.equals(value)) {
            return nullLabel;
        }
        else {
            return value;
        }
    }
}
