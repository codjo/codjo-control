/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.server.plugin;
import net.codjo.agent.AclMessage;
import net.codjo.agent.test.DummyAgent;
import net.codjo.aspect.AspectManager;
import net.codjo.control.common.loader.XmlMapperHelper;
import net.codjo.control.common.message.ControlJobRequest;
import net.codjo.control.common.message.SourceOfData;
import net.codjo.database.common.api.DatabaseFactory;
import net.codjo.database.common.api.structure.SqlTable;
import net.codjo.sql.server.JdbcServiceUtil;
import net.codjo.sql.server.JdbcServiceUtilMock;
import net.codjo.test.common.LogString;
import net.codjo.tokio.TokioFixture;
import junit.framework.TestCase;
/**
 * Classe de test de {@link DefaultControlerFactory}.
 */
public class DefaultControlerFactoryTest extends TestCase {
    private static final String MY_QUARANTINE = "MY_QUARANTINE";
    private DefaultControlerFactory factory;
    private TokioFixture fixture = new TokioFixture(DefaultControlerFactoryTest.class);
    private LogString log = new LogString();
    private static final DatabaseFactory DATABASE_FACTORY = new DatabaseFactory();


    public void test_executeControl() throws Exception {
        fixture.getJdbcFixture().create(SqlTable.table(MY_QUARANTINE), "MY_FIELD_1 numeric null, "
                                                                       + "ERROR_TYPE numeric null, "
                                                                       + "ERROR_LOG varchar(255) null");
        fixture.insertInputInDb("ImportDecisiv");

        ControlJobRequest jobRequest = new ControlJobRequest();
        jobRequest.setQuarantineTable(MY_QUARANTINE);
        jobRequest.setInitiatorLogin("gonnot");
        jobRequest.setId("control-237");
        jobRequest.addPath(SourceOfData.IMPORT);

        Controler controler = factory.createControler();
        controler.execute(jobRequest);

        fixture.assertAllOutputs("ImportDecisiv");
    }


    @Override
    protected void setUp() throws Exception {
        fixture.doSetUp(DATABASE_FACTORY.createJdbcFixture());
        JdbcServiceUtil mock = new JdbcServiceUtilMock(log, fixture.getJdbcFixture());

        XmlMapperHelper.initToLoadFromRessource("/META-INF/ApplicationIP.xml");

        factory = new DefaultControlerFactory(mock, new ControlPreference(XmlMapperHelper.getApplicationIP(),
                                                                          new AspectManager()));
        factory.init(new DummyAgent(), new AclMessage(AclMessage.Performative.AGREE));
    }


    @Override
    protected void tearDown() throws Exception {
        fixture.doTearDown();
    }
}
