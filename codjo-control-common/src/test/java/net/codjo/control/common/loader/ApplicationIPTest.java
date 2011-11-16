/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.loader;
import net.codjo.control.common.IntegrationPlan;
import java.io.File;
import java.util.Iterator;
import junit.framework.TestCase;
/**
 * Description of the Class
 *
 * @author $Author: gonnot $
 * @version $Revision: 1.5 $
 */
public class ApplicationIPTest extends TestCase {
    /**
     * Test le chargement d'un ApplicationIP a partir d'une Fichier sur le disque.
     *
     * @throws Exception Erreur durant le test
     */
    public void test_mapping_fromFile() throws Exception {
        File root = new File(".");
        if (root.getAbsolutePath().endsWith("codjo-control\\.")) {
            root = new File("codjo-control-common");
        }
        net.codjo.control.common.loader.XmlMapperHelper.initToLoadFromFile(root,
                                                                         new File(
                                                                               "src/test/resources/test-data/ApplicationIP.xml"));

        ApplicationIP appIp = XmlMapperHelper.getApplicationIP();

        assertEquals(1, appIp.getIntegrationDefinitions().size());

        IntegrationPlan ip = appIp.getPlan("Q_AP_ISSUER");
        assertEquals(ip, appIp.getPlanById("ap_issuer_integration_plan"));
        assertEquals("#ISSUER_CTRL", ip.getControlTableName());
    }


    /**
     * Test le chargement d'un ApplicationIP a partir d'une URI (e.g. dans un  jar).
     *
     * @throws Exception Erreur durant le test
     */
    public void test_mapping_fromURI() throws Exception {
        System.setProperty("javax.xml.parsers.SAXParserFactory",
                           "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
        XmlMapperHelper.initToLoadFromRessource(
              "/net/codjo/control/common/loader/ApplicationIPTest.xml");
        ApplicationIP appIp = XmlMapperHelper.getApplicationIP();

        assertEquals(3, appIp.getIntegrationDefinitions().size());
        Iterator iter = appIp.getIntegrationDefinitions().iterator();

        assertTrue(iter.hasNext());
        IntegrationDefinition def = (IntegrationDefinition)iter.next();
        assertEquals("/planA.xml", def.getPlanURI());
        assertEquals(null, def.getTransfert());
        IntegrationPlan planA = appIp.getPlanById("planA");
        assertNotNull(planA);
        assertEquals("#ISSUER_CTRL", planA.getControlTableName());
        assertEquals(10, planA.getStep("IsNull_IssuerCode").getErrorCode());

        assertTrue(iter.hasNext());
        def = (IntegrationDefinition)iter.next();
        assertEquals("/planB.xml", def.getPlanURI());
        assertEquals("Q_AP_BENCHMARK", def.getTransfert().getQuarantine());
        assertEquals("Q_AP_USER_BENCHMARK", def.getTransfert().getUser());
        assertEquals(false, def.getTransfert().isReplaceUserData());

        assertTrue(iter.hasNext());
        def = (IntegrationDefinition)iter.next();
        assertEquals("/planC.xml", def.getPlanURI());
        assertEquals("Q_AP_BENCH_C", def.getTransfert().getQuarantine());
        assertEquals("Q_AP_USER_BENCH_C", def.getTransfert().getUser());
        assertEquals("[COL_A, COL_B]", def.getTransfert().getMatchingCols().toString());
        assertEquals(true, def.getTransfert().isReplaceUserData());
    }
}
