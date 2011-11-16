/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.log4j.Logger;
/**
 * Classe responsable du transfert de données de la quarantaine vers la table de
 * controle.
 *
 * @author $Author: rivierv $
 * @version $Revision: 1.6 $
 */
public class Shipment {
    private static final Logger APP = Logger.getLogger(Shipment.class);
    private String from;
    private String fromPk;
    private String selectWhereClause;
    private String to;
    private ShipmentProcessor processor = new DefaultShipmentProcessor();
    private String processorClass = DefaultShipmentProcessor.class.getName();

    public Shipment() {}

    public void setFrom(String from) {
        this.from = from;
    }


    public void setFromPk(String fromPk) {
        this.fromPk = fromPk;
    }


    public void setSelectWhereClause(String selectWhereClause) {
        this.selectWhereClause = selectWhereClause;
    }


    public void setTo(String to) {
        this.to = to;
    }


    public String getFrom() {
        return from;
    }


    public String getFromPk() {
        return fromPk;
    }


    public String getSelectWhereClause() {
        return selectWhereClause;
    }


    public String getTo() {
        return to;
    }


    public void execute(Connection con, Dictionary dico)
            throws SQLException {
        APP.debug("Transfert des données de " + dico.replaceVariables(getFrom())
            + " vers " + dico.replaceVariables(getTo()));
        processor.execute(con, dico, this);
    }


    public String getProcessorClass() {
        return processorClass;
    }


    public void setProcessorClass(String processorClass)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        this.processorClass = processorClass;
        processor = (ShipmentProcessor)Class.forName(processorClass).newInstance();
    }


    ShipmentProcessor getProcessor() {
        return processor;
    }
}
