package net.codjo.control.server.plugin;
import net.codjo.aspect.AspectManager;
import net.codjo.control.common.loader.ApplicationIP;
import net.codjo.control.common.loader.IntegrationDefinition;
import net.codjo.control.common.loader.TransfertData;
import net.codjo.control.common.message.TransferJobRequest;
import java.util.Collections;
import junit.framework.TestCase;
/**
 *
 */
public class ControlPreferenceTest extends TestCase {
    public void test_constructor() throws Exception {
        AspectManager aspectManager = new AspectManager();
        ControlPreference preference = new ControlPreference(new ApplicationIP(), aspectManager);

        assertNotNull(preference.getDispatchPoint());
        assertEquals(ControlServerPlugin.DISPATCH_ASPECT_POINT_ID,
                     preference.getDispatchPoint().getPointId());
        assertSame(aspectManager, preference.getDispatchPoint().getManager());
    }


    public void test_getTransfertData() throws Exception {
        TransfertData transfertData = new TransfertData("AP_QUARANTINE", "AP_USER_QUARANTINE");

        TransferJobRequest request = createTransfert("AP_QUARANTINE", "AP_USER_QUARANTINE");

        ControlPreference preference = createPreferenceWith(transfertData);
        assertSame(transfertData, preference.getTransfertData(request));
    }


    public void test_getTransfertData_forUnknownTables() throws Exception {

        TransferJobRequest request = createTransfert("AP_QUARANTINE", "AP_USER_QUARANTINE");

        ControlPreference preference = createPreferenceWith(new TransfertData("AP_QUARANTINE", "B"));

        try {
            preference.getTransfertData(request);
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("Mécanisme de transfert entre les tables 'AP_QUARANTINE' et 'AP_USER_QUARANTINE' "
                         + "est inconnu (absent du fichier META-INF/ApplicationIP.xml)",
                         ex.getLocalizedMessage());
        }
    }


    private TransferJobRequest createTransfert(String quarantine, String userQuarantine) {
        TransferJobRequest transferRequest = new TransferJobRequest();
        transferRequest.setQuarantine(quarantine);
        transferRequest.setUserQuarantine(userQuarantine);
        return transferRequest;
    }


    private ControlPreference createPreferenceWith(TransfertData transfertData) {
        IntegrationDefinition integrationDefinition = new IntegrationDefinition();
        integrationDefinition.setTransfert(transfertData);

        ApplicationIP applicationIP = new ApplicationIP();
        applicationIP.setIntegrationDefinitions(Collections.singleton(integrationDefinition));

        return new ControlPreference(applicationIP, new AspectManager());
    }
}
