package net.codjo.control.server.plugin;
import net.codjo.agent.AgentContainerMock;
import net.codjo.agent.ContainerConfigurationMock;
import net.codjo.agent.test.AgentContainerFixture;
import net.codjo.agent.test.Story.ConnectionType;
import net.codjo.i18n.common.plugin.InternationalizationPlugin;
import net.codjo.mad.server.plugin.MadServerPluginMock;
import net.codjo.plugin.server.ServerCoreMock;
import net.codjo.test.common.LogString;
import net.codjo.workflow.server.organiser.JobBuilder;
import net.codjo.workflow.server.plugin.WorkflowServerPlugin;
import net.codjo.workflow.server.plugin.WorkflowServerPlugin.WorkflowServerPluginConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ControlServerPluginTest {
    private ControlServerPlugin plugin;
    private LogString log = new LogString();
    private AgentContainerFixture fixture = new AgentContainerFixture();


    @Before
    public void setUp() throws Exception {
        fixture.doSetUp();
        WorkflowServerPlugin workflowServerPluginMock = mock(WorkflowServerPlugin.class);
        WorkflowServerPluginConfiguration pluginConfigurationMock
              = mock(WorkflowServerPluginConfiguration.class);
        when(workflowServerPluginMock.getConfiguration()).thenReturn(pluginConfigurationMock);

        plugin = new ControlServerPlugin(workflowServerPluginMock,
                                         new InternationalizationPlugin(),
                                         new MadServerPluginMock(new LogString("madServerPlugin", log)),
                                         new ServerCoreMock(new LogString("core", log), fixture));

        verify(pluginConfigurationMock).registerJobBuilder(Mockito.<JobBuilder>anyObject());
    }


    @After
    public void tearDown() throws Exception {
        fixture.doTearDown();
    }


    @Test
    public void test_other() throws Exception {
        plugin.initContainer(new ContainerConfigurationMock(log));
        log.assertContent("");
    }


    @Test
    public void test_madPluginUnavailable() throws Exception {
        WorkflowServerPlugin workflowServerPlugin = mock(WorkflowServerPlugin.class);
        WorkflowServerPluginConfiguration configuration = mock(WorkflowServerPluginConfiguration.class);
        when(workflowServerPlugin.getConfiguration()).thenReturn(configuration);

        plugin = new ControlServerPlugin(workflowServerPlugin, new InternationalizationPlugin());

        plugin.initContainer(new ContainerConfigurationMock(log));
        plugin.start(new AgentContainerMock(new LogString()));
        plugin.stop();
        log.assertContent("");
    }


    @Test
    public void test_start() throws Exception {
        fixture.startContainer(ConnectionType.NO_CONNECTION);
        plugin.start(fixture.getContainer());

        assertAgentWithService(ControlServerPlugin.CONTROL_REQUEST_TYPE,
                               new String[]{"control-job-agent",
                                            "control-drh-agent"});

        assertAgentWithService(ControlServerPlugin.QUARANTINE_TRANSFER_TYPE,
                               new String[]{"quarantine-transfer-job-agent",
                                            "quarantine-transfer-drh-agent"});

        assertAgentWithService(WorkflowServerPlugin.WORKFLOW_SCHEDULE_SERVICE,
                               new String[]{"control-scheduler",
                                            "quarantine-transfer-scheduler"});
    }


    @Test
    public void test_componentsManagement() throws Exception {
        plugin.start(new AgentContainerMock(new LogString()));

        log.assertAndClear("core.addGlobalComponent(ApplicationIP)"
                           + ", madServerPluginConfiguration.addSessionComponent(DefaultControlManager)");

        plugin.stop();

        log.assertAndClear("core.removeGlobalComponent(ApplicationIP)"
                           + ", madServerPluginConfiguration.removeSessionComponent(DefaultControlManager)");
    }


    private void assertAgentWithService(String service, String[] expectedLocalNames) {
        fixture.assertNumberOfAgentWithService(expectedLocalNames.length, service);
        fixture.assertAgentWithService(expectedLocalNames, service);
    }
}
