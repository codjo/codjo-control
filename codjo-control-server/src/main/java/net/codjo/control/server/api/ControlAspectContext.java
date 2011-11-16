package net.codjo.control.server.api;
import net.codjo.aspect.AspectContext;
import net.codjo.aspect.util.TransactionalPoint;
import java.sql.Connection;
/**
 * Wrapper autour d'un {@link AspectContext} pour simplifier son utilisation.
 */
public class ControlAspectContext {
    static final String CONTROL_TABLE_KEY = "control.table";
    static final String USER_KEY = "user";
    static final String REQUEST_ID_KEY = "requestId";
    static final String PATH_OF_REQUEST_KEY = "pathOfRequest";
    private final AspectContext aspectContext;


    public ControlAspectContext() {
        this(new AspectContext());
    }


    public ControlAspectContext(AspectContext aspectContext) {
        this.aspectContext = aspectContext;
    }


    public void setControlTableName(String controlTableName) {
        aspectContext.put(CONTROL_TABLE_KEY, controlTableName);
    }


    public String getControlTableName() {
        return (String)aspectContext.get(CONTROL_TABLE_KEY);
    }


    public void setQuarantineTable(String quarantineTable) {
        aspectContext.put(TransactionalPoint.ARGUMENT, quarantineTable);
    }


    public String getQuarantineTable() {
        return (String)aspectContext.get(TransactionalPoint.ARGUMENT);
    }


    public void setConnection(Connection connection) {
        aspectContext.put(TransactionalPoint.CONNECTION, connection);
    }


    public Connection getConnection() {
        return (Connection)aspectContext.get(TransactionalPoint.CONNECTION);
    }


    public void setUser(String user) {
        aspectContext.put(USER_KEY, user);
    }


    public String getUser() {
        return (String)aspectContext.get(USER_KEY);
    }


    public void setJobRequestId(String jobRequestId) {
        aspectContext.put(REQUEST_ID_KEY, jobRequestId);
    }


    public String getJobRequestId() {
        return (String)aspectContext.get(REQUEST_ID_KEY);
    }


    public AspectContext toAspectContext() {
        return aspectContext;
    }

    public void setPathOfRequest(String pathOfRequest) {
        aspectContext.put(PATH_OF_REQUEST_KEY, pathOfRequest);
    }

    public String getPathOfRequest() {
        return (String)aspectContext.get(PATH_OF_REQUEST_KEY);
    }
}
