/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.gui.plugin;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import net.codjo.agent.UserId;
import net.codjo.control.gui.data.QuarantineGuiData;
import net.codjo.gui.toolkit.util.ErrorDialog;
import net.codjo.i18n.common.TranslationManager;
import net.codjo.i18n.gui.TranslationNotifier;
import net.codjo.mad.gui.framework.AbstractGuiAction;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.mad.gui.i18n.InternationalizationUtil;
import net.codjo.mad.gui.request.ListDataSource;

class DefaultQuarantineAction extends AbstractGuiAction {
    private CleanUpListener cleanUpListener = new CleanUpListener();
    private JInternalFrame frame;
    private QuarantineGuiData guiData;
    private final UserId userId;


    DefaultQuarantineAction(GuiContext ctxt, QuarantineGuiData guiData, UserId userId) {
        super(ctxt, guiData.getName(), guiData.getTooltip(), guiData.getIcon());
        this.guiData = guiData;
        this.userId = userId;
    }


    public void actionPerformed(ActionEvent event) {
        if (frame == null) {
            displayNewWindow();
        }
        else {
            try {
                frame.setSelected(true);
            }
            catch (PropertyVetoException ex) {
                ; // pas grave
            }
        }
    }


    private void displayNewWindow() {
        try {
            frame = new DefaultQuarantineWindow(getGuiContext(), guiData, userId, new ListDataSource());
            frame.addInternalFrameListener(cleanUpListener);
            getDesktopPane().add(frame);
            frame.pack();
            frame.setVisible(true);
            frame.setSelected(true);
        }
        catch (Exception ex) {
            ErrorDialog.show(getDesktopPane(), "Impossible d'afficher la liste", ex);
        }
    }


    private class CleanUpListener extends InternalFrameAdapter {
        @Override
        public void internalFrameActivated(InternalFrameEvent event) {
        }


        @Override
        public void internalFrameClosed(InternalFrameEvent event) {
            event.getInternalFrame().removeInternalFrameListener(this);
            frame = null;
        }


        @Override
        public void internalFrameClosing(InternalFrameEvent event) {
            event.getInternalFrame().removeInternalFrameListener(this);
            frame = null;
        }
    }
}
