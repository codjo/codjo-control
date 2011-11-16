/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
import java.sql.Connection;
/**
 * Contexte d'execution d'un control.
 *
 * @version $Revision: 1.1.1.1 $
 */
public class ControlContext {
    private String user;
    private String currentRequestId;
    private String pathOfRequest;
    private Connection connection;


    public ControlContext(String user, String currentRequestId, String pathOfRequest) {
        this.user = user;
        this.currentRequestId = currentRequestId;
        this.pathOfRequest = pathOfRequest;
    }


    public ControlContext() {
    }


    public String getUser() {
        return user;
    }


    public void setUser(String user) {
        this.user = user;
    }


    public String getCurrentRequestId() {
        return currentRequestId;
    }


    public void setCurrentRequestId(String currentRequestId) {
        this.currentRequestId = currentRequestId;
    }


    public String getPathOfRequest() {
        return pathOfRequest;
    }


    public void setPathOfRequest(String pathOfRequest) {
        this.pathOfRequest = pathOfRequest;
    }


    public Connection getConnection() {
        return connection;
    }


    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
