package net.codjo.control.server.plugin;
import net.codjo.aspect.AspectManager;
import net.codjo.aspect.util.TransactionalPoint;
import net.codjo.control.common.IntegrationPlan;
import net.codjo.control.common.loader.ApplicationIP;
import net.codjo.control.common.loader.IntegrationDefinition;
import net.codjo.control.common.loader.TransfertData;
import net.codjo.control.common.message.TransferJobRequest;
/**
 *
 */
class ControlPreference {
    private ApplicationIP applicationIP;
    private TransactionalPoint dispatchPoint;


    ControlPreference(ApplicationIP applicationIP, AspectManager aspectManager) {
        this.applicationIP = applicationIP;
        dispatchPoint = new TransactionalPoint(ControlServerPlugin.DISPATCH_ASPECT_POINT_ID, aspectManager);
    }


    public TransactionalPoint getDispatchPoint() {
        return dispatchPoint;
    }


    public IntegrationPlan getPlan(String quarantineTable) {
        return applicationIP.getPlan(quarantineTable);
    }


    public TransfertData getTransfertData(TransferJobRequest request) {
        for (IntegrationDefinition definition : applicationIP.getIntegrationDefinitions()) {
            TransfertData transfert = definition.getTransfert();
            if (transfert != null
                && transfert.getQuarantine().equals(request.getQuarantine())
                && transfert.getUser().equals(request.getUserQuarantine())) {
                return transfert;
            }
        }
        throw new IllegalArgumentException("Mécanisme de transfert entre les tables '"
                                           + request.getQuarantine()
                                           + "' et '"
                                           + request.getUserQuarantine()
                                           + "' est inconnu (absent du fichier META-INF/ApplicationIP.xml)");
    }
}
