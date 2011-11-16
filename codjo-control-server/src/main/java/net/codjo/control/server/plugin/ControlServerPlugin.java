package net.codjo.control.server.plugin;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.ContainerConfiguration;
import net.codjo.agent.ContainerFailureException;
import net.codjo.agent.DFService;
import net.codjo.aspect.AspectConfigException;
import net.codjo.aspect.AspectManager;
import net.codjo.control.common.loader.ApplicationIP;
import net.codjo.control.common.loader.XmlMapperHelper;
import net.codjo.control.common.manager.DefaultControlManager;
import net.codjo.control.common.message.ControlJobRequest;
import net.codjo.control.common.message.TransferJobRequest;
import net.codjo.control.server.audit.ControlStringifier;
import net.codjo.control.server.audit.TransferStringifier;
import net.codjo.mad.server.plugin.MadServerPlugin;
import net.codjo.plugin.common.ApplicationCore;
import net.codjo.plugin.server.ServerPlugin;
import net.codjo.sql.server.JdbcServiceUtil;
import net.codjo.workflow.server.api.JobAgent;
import net.codjo.workflow.server.api.JobAgent.MODE;
import net.codjo.workflow.server.api.ResourcesManagerAgent;
import net.codjo.workflow.server.api.ResourcesManagerAgent.AgentFactory;
import net.codjo.workflow.server.plugin.WorkflowServerPlugin;
import net.codjo.xml.XmlException;
import java.io.IOException;

public final class ControlServerPlugin implements ServerPlugin {
    private final MadServerPlugin madServerPlugin;
    private final ApplicationCore applicationCore;
    public static final String DISPATCH_ASPECT_POINT_ID = "control.dispatch";
    public static final String APPLICATION_IP_PATH = ControlJobRequest.APPLICATION_IP_PATH;
    public static final String CONTROL_REQUEST_TYPE = ControlJobRequest.CONTROL_REQUEST_TYPE;
    public static final String QUARANTINE_TRANSFER_TYPE = TransferJobRequest.QUARANTINE_TRANSFER_TYPE;
    public static final String QUARANTINE_TABLE = ControlJobRequest.QUARANTINE_TABLE;


    public ControlServerPlugin(WorkflowServerPlugin workflowServerPlugin) {
        this(workflowServerPlugin, null, null);
    }


    public ControlServerPlugin(WorkflowServerPlugin workflowServerPlugin,
                               MadServerPlugin madServerPlugin,
                               ApplicationCore core) {
        this.madServerPlugin = madServerPlugin;
        this.applicationCore = core;

        workflowServerPlugin.getConfiguration().registerJobBuilder(new ControlJobRequestHandler());
        new ControlStringifier().install(workflowServerPlugin);
        new TransferStringifier().install(workflowServerPlugin);
    }


    public void initContainer(ContainerConfiguration configuration) {
    }


    public void start(AgentContainer agentContainer) throws IOException, XmlException,
                                                            ContainerFailureException,
                                                            AspectConfigException {

        XmlMapperHelper.initToLoadFromRessource(APPLICATION_IP_PATH);

        if (madServerPlugin != null && applicationCore != null) {
            applicationCore.addGlobalComponent(XmlMapperHelper.getApplicationIP());
            madServerPlugin.getConfiguration().addSessionComponent(DefaultControlManager.class);
        }

        ControlPreference preference =
              new ControlPreference(XmlMapperHelper.getApplicationIP(), createAspectManager());

        createTransferService(agentContainer, preference);
        createControlService(agentContainer, preference);
    }


    private void createControlService(AgentContainer agentContainer, final ControlPreference preference)
          throws ContainerFailureException {
        agentContainer
              .acceptNewAgent("control-scheduler", new AfterImportScheduleAgent())
              .start();
        agentContainer
              .acceptNewAgent("control-drh-agent",
                              new ResourcesManagerAgent(new ControlAgentFactory(preference),
                                                        DFService.createAgentDescription(CONTROL_REQUEST_TYPE)))
              .start();
        agentContainer
              .acceptNewAgent("control-job-agent", createControlJobAgent(preference, MODE.NOT_DELEGATE))
              .start();
    }


    private void createTransferService(AgentContainer agentContainer, ControlPreference preference)
          throws ContainerFailureException {
        agentContainer
              .acceptNewAgent("quarantine-transfer-scheduler", new TransferScheduleAgent())
              .start();
        agentContainer.acceptNewAgent("quarantine-transfer-drh-agent",
                                      new ResourcesManagerAgent(new TransferAgentFactory(preference),
                                                                DFService.createAgentDescription(
                                                                      QUARANTINE_TRANSFER_TYPE)))
              .start();
        agentContainer
              .acceptNewAgent("quarantine-transfer-job-agent",
                              createTransferJobAgent(preference, MODE.NOT_DELEGATE))
              .start();
    }


    private TransferJobAgent createTransferJobAgent(ControlPreference preference, MODE mode) {
        return new TransferJobAgent(new JdbcServiceUtil(), preference, mode);
    }


    private ControlJobAgent createControlJobAgent(ControlPreference preference, MODE mode) {
        return new ControlJobAgent(new DefaultControlerFactory(new JdbcServiceUtil(), preference), mode);
    }


    public void stop() {
        if (madServerPlugin != null && applicationCore != null) {
            applicationCore.removeGlobalComponent(ApplicationIP.class);
            madServerPlugin.getConfiguration().removeSessionComponent(DefaultControlManager.class);
        }
    }


    private AspectManager createAspectManager() throws AspectConfigException {
        // TODO : A transformer sous forme de plugin
        AspectManager aspectManager = new AspectManager();
        aspectManager.load();
        return aspectManager;
    }


    private class TransferAgentFactory implements AgentFactory {
        private final ControlPreference preference;


        TransferAgentFactory(ControlPreference preference) {
            this.preference = preference;
        }


        public JobAgent create() throws Exception {
            return createTransferJobAgent(preference, MODE.DELEGATE);
        }
    }
    private class ControlAgentFactory implements AgentFactory {
        private final ControlPreference preference;


        ControlAgentFactory(ControlPreference preference) {
            this.preference = preference;
        }


        public JobAgent create() throws Exception {
            return createControlJobAgent(preference, MODE.DELEGATE);
        }
    }
}
