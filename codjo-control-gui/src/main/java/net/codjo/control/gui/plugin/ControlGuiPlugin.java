package net.codjo.control.gui.plugin;
import net.codjo.control.common.message.ControlJobRequest;
import net.codjo.control.common.message.TransferJobRequest;
import net.codjo.i18n.common.Language;
import net.codjo.i18n.common.TranslationManager;
import net.codjo.mad.client.plugin.MadConnectionPlugin;
import net.codjo.mad.gui.base.GuiConfiguration;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.mad.gui.i18n.AbstractInternationalizableGuiPlugin;
import net.codjo.mad.gui.request.RequestToolBar;
import net.codjo.workflow.common.schedule.WorkflowConfiguration;
import net.codjo.workflow.gui.plugin.WorkflowGuiPlugin;
import javax.swing.ImageIcon;

public final class ControlGuiPlugin extends AbstractInternationalizableGuiPlugin {
    private final MadConnectionPlugin madConnectionPlugin;
    private final Class mainClass;

    private final ControlGuiPluginConfiguration configuration = new ControlGuiPluginConfiguration();


    public ControlGuiPlugin(MadConnectionPlugin madConnectionPlugin, WorkflowGuiPlugin workflowGuiPlugin) {
        this.madConnectionPlugin = madConnectionPlugin;
        this.mainClass = ControlGuiPlugin.class;

        workflowGuiPlugin.getConfiguration()
              .setTaskManagerJobIcon(ControlJobRequest.CONTROL_REQUEST_TYPE,
                                     new ImageIcon(getClass().getResource("/images/job.control.png")));
        workflowGuiPlugin.getConfiguration()
              .setTaskManagerJobIcon(TransferJobRequest.QUARANTINE_TRANSFER_TYPE,
                                     new ImageIcon(getClass().getResource(
                                           "/images/job.quarantine-transfer.png")));
    }


    @Override
    protected void registerLanguageBundles(TranslationManager translationManager) {
        translationManager.addBundle("net.codjo.control.common.i18n", Language.FR);
        translationManager.addBundle("net.codjo.control.common.i18n", Language.EN);
        translationManager.addBundle("net.codjo.control.gui.i18n", Language.FR);
        translationManager.addBundle("net.codjo.control.gui.i18n", Language.EN);
    }


    @Override
    public void initGui(GuiConfiguration guiConfiguration) throws Exception {
        super.initGui(guiConfiguration);
        QuarantineManager quarantineManager =
              new QuarantineManager(mainClass.getResource(configuration.getConfigurationFilePath()),
                                    madConnectionPlugin.getUserId(),
                                    configuration.getQuarantineWorkflowConfiguration());

        quarantineManager.addMenuTo(this, guiConfiguration, guiConfiguration.getGuiContext());
    }


    public ControlGuiPluginConfiguration getConfiguration() {
        return configuration;
    }


    public class ControlGuiPluginConfiguration {
        private String configurationFilePath = "/conf/quarantine.xml";
        private WorkflowConfiguration quarantineWorkflowConfiguration = new WorkflowConfiguration();


        public String getConfigurationFilePath() {
            return configurationFilePath;
        }


        public void setConfigurationFilePath(String configurationFilePath) {
            this.configurationFilePath = configurationFilePath;
        }


        public WorkflowConfiguration getQuarantineWorkflowConfiguration() {
            return quarantineWorkflowConfiguration;
        }
    }

    public static interface QuarantineToolbarCustomizer {
        public void customize(GuiContext ctxt, RequestToolBar requestToolBar);
    }
}
