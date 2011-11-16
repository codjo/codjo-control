/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.loader;
/**
 */
public class IntegrationDefinition {
    private TransfertData transfert;
    private String planURI;

    public TransfertData getTransfert() {
        return transfert;
    }


    public void setTransfert(TransfertData transfert) {
        this.transfert = transfert;
    }


    public String getPlanURI() {
        return planURI;
    }


    public void setPlanURI(String planURI) {
        this.planURI = planURI;
    }
}
