package net.codjo.control.common.message;
import net.codjo.workflow.common.message.JobRequest;
import net.codjo.workflow.common.message.JobRequestWrapper;
/**
 *
 */
public class ControlJobRequest extends JobRequestWrapper {
    public static final String QUARANTINE_TABLE = "quarantineTable";
    public static final String CONTROL_REQUEST_TYPE = "control";
    public static final String APPLICATION_IP_PATH = "/META-INF/ApplicationIP.xml";
    public static final String PATH_OF_DATA_KEY = "path.of.data";


    public ControlJobRequest() {
        this(new JobRequest());
    }


    public ControlJobRequest(String quarantineTable) {
        this();
        setQuarantineTable(quarantineTable);
    }


    public ControlJobRequest(JobRequest request) {
        super(CONTROL_REQUEST_TYPE, request);
    }


    public void setQuarantineTable(String quarantineTable) {
        setArgument(QUARANTINE_TABLE, quarantineTable);
    }


    public String getQuarantineTable() {
        return getArgument(QUARANTINE_TABLE);
    }


    public void addPath(String path) {
        String currentPath = getPath();
        if (currentPath == null || currentPath.trim().length() == 0) {
            setArgument(PATH_OF_DATA_KEY, path);
        }
        else {
            setArgument(PATH_OF_DATA_KEY, currentPath + "/" + path);
        }
    }


    public String getPath() {
        return getArgument(PATH_OF_DATA_KEY);
    }
}
