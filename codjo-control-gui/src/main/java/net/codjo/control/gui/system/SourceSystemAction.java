/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.gui.system;
import net.codjo.gui.toolkit.util.ErrorDialog;
import net.codjo.mad.gui.framework.AbstractGuiAction;
import net.codjo.mad.gui.framework.GuiContext;
import java.awt.event.ActionEvent;
import javax.swing.UIManager;
/**
 * Action GUI pour afficher les données de la table PM_SOURCE_SYSTEM.
 */
public class SourceSystemAction extends AbstractGuiAction {
    public SourceSystemAction(GuiContext ctxt) {
        super(ctxt, "Système source", "Affiche et édite les systèmes sources.");
    }

    public void actionPerformed(ActionEvent evt) {
        displayNewWindow();
    }


    private void displayNewWindow() {
        try {
            SourceSystemWindow frame = new SourceSystemWindow(getGuiContext());
            getDesktopPane().add(frame);
            frame.setFrameIcon(UIManager.getIcon("icon"));
            frame.pack();
            frame.setVisible(true);
            frame.setSelected(true);
        }
        catch (Exception ex) {
            ErrorDialog.show(getDesktopPane(), "Impossible d'afficher l'IHM", ex);
        }
    }
}
