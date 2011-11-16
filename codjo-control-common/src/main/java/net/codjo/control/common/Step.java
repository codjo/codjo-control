/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.log4j.Logger;
/**
 * Etape dans un plan d'intégration.
 *
 * @author $Author: nadaud $
 * @version $Revision: 1.12 $
 */
public class Step implements Cloneable {
    public static final String ERROR_CODE = "error_code";
    public static final String ERROR_MESSAGE = "error_message";
    public static final String FOR_ALL = "all";
    public static final String FOR_BATCH = "batch";
    public static final String FOR_USER = "user";
    public static final String FOR_USER_ADD = "user.add";
    public static final String FOR_USER_UPDATE = "user.update";
    private static final Logger APP = Logger.getLogger(Step.class);
    private SqlStepHelper sqlControler = new SqlStepHelper();
    private String controlClass;
    private String description;
    private int errorCode;
    private String errorMessage;
    private String id;
    private Control javaControler;
    private MassControl javaMassControler;
    private Set<Parameter> parameters;
    private int priority;
    private Query query;
    private String stepFor;
    private String test;
    private String type;
    private List<String> tableToSpooled;
    /**
     * Dictionnaire attaché à ce Step.
     */
    private Dictionary dictionary = new Dictionary();


    public Step() {
    }


    @Override
    public Object clone() {
        Step newStep = new Step();
        if (this.controlClass != null) {
            try {
                newStep.setControlClass(this.getControlClass());
            }
            catch (Exception ex) {
                throw new RuntimeException(
                      "An exception occured while trying to clone a Step: "
                      + ex.getClass().getName() + ": " + ex.getMessage());
            }
        }
        newStep.description = this.description;
        newStep.errorCode = this.errorCode;
        newStep.errorMessage = this.errorMessage;
        newStep.id = this.id;
        if (this.parameters != null) {
            newStep.setParameters(this.parameters);
        }
        newStep.priority = this.priority;
        if (this.query != null) {
            newStep.setQuery((Query)this.getQuery().clone());
        }
        newStep.stepFor = this.stepFor;
        newStep.test = this.test;
        newStep.type = this.type;
        if (this.tableToSpooled != null) {
            newStep.tableToSpooled = new ArrayList<String>(tableToSpooled);
        }
        if (this.getDictionary() != null) {
            newStep.setDictionary((Dictionary)this.getDictionary().clone());
        }
        return newStep;
    }


    public void addTableToSpooledForDebug(String tableName) {
        if (tableToSpooled == null) {
            tableToSpooled = new ArrayList<String>();
        }
        tableToSpooled.add(tableName);
    }


    public void setControlClass(String controlClass)
          throws Exception {
        this.controlClass = controlClass;

        Object obj = Class.forName(controlClass).newInstance();
        if (obj instanceof Control) {
            javaControler = (Control)Class.forName(controlClass).newInstance();
        }
        else {
            javaMassControler = (MassControl)Class.forName(controlClass).newInstance();
        }
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }


    public Dictionary getDictionary() {
        return dictionary;
    }


    public void setDictionary(Dictionary dictionary) {
        this.dictionary.addDictionary(dictionary);
    }


    public String getErrorMessage() {
        return errorMessage;
    }


    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }


    public void setId(String id) {
        this.id = id;
    }


    public void setParameters(Collection<Parameter> parameters) {
        this.parameters = new TreeSet<Parameter>(parameters);
    }


    public void setPriority(int priority) {
        this.priority = priority;
    }


    public void setQuery(Query query) {
        this.query = query;
    }


    public void setQuery(String temporaryTable, String ignoreWarningCode, String sql) {
        Query tmp = new Query(sql);
        tmp.setIgnoreWarningCode(ignoreWarningCode);
        tmp.setTemporaryTable(temporaryTable);
        this.setQuery(tmp);
    }


    public void setStepFor(String stepFor) {
        if (stepFor != null) {
            this.stepFor = stepFor.toLowerCase();
        }
        else {
            this.stepFor = null;
        }
    }


    public void setTest(String test) {
        this.test = test;
    }


    public void setType(String type) {
        this.type = type;
    }


    public String getControlClass() {
        return controlClass;
    }


    public String getDescription() {
        return description;
    }


    public int getErrorCode() {
        return errorCode;
    }


    public String getId() {
        return id;
    }


    public Collection<Parameter> getParameters() {
        return parameters;
    }


    public int getPriority() {
        return priority;
    }


    public Query getQuery() {
        return query;
    }


    public String getStepFor() {
        return stepFor;
    }


    public boolean isStepFor(String stepForId) {
        if (stepFor == null || stepForId == null || FOR_ALL.equals(stepFor)) {
            return true;
        }
        if (FOR_USER.equals(stepFor)) {
            return isPathOfRequestContains(stepForId, FOR_USER) ||
                   isPathOfRequestContains(stepForId, FOR_USER_ADD) ||
                   isPathOfRequestContains(stepForId, FOR_USER_UPDATE);
        }
        if (FOR_USER_ADD.equals(stepFor)) {
            return isPathOfRequestContains(stepForId, FOR_USER) ||
                   isPathOfRequestContains(stepForId, FOR_USER_ADD);
        }
        if (FOR_USER_UPDATE.equals(stepFor)) {
            return isPathOfRequestContains(stepForId, FOR_USER) ||
                   isPathOfRequestContains(stepForId, FOR_USER_UPDATE);
        }
        return isPathOfRequestContains(stepForId, stepFor);
    }


    private boolean isPathOfRequestContains(String pathOfRequest, String toLookFor) {
        if (!pathOfRequest.startsWith("/")) {
            pathOfRequest = "/" + pathOfRequest;
        }
        if (!pathOfRequest.endsWith("/")) {
            pathOfRequest = pathOfRequest + "/";
        }
        return pathOfRequest.contains("/" + toLookFor + "/");
    }


    public String getTest() {
        return test;
    }


    public String getType() {
        return type;
    }


    public void addParameter(Parameter param) {
        if (parameters == null) {
            parameters = new TreeSet<Parameter>();
        }
        parameters.add(param);
    }


    public void addParameter(Integer index, String parameterType, String parameterValue) {
        if (parameters == null) {
            parameters = new TreeSet<Parameter>();
        }
        parameters.add(new Parameter(index, parameterType, parameterValue));
    }


    public void execute(Object obj, Dictionary dico, ControlContext context)
          throws ControlException {
        dico.addVariable(ERROR_CODE, Integer.toString(getErrorCode()));
        dico.addVariable(ERROR_MESSAGE, getErrorMessage());
        this.dictionary.setParent(dico);
        javaControler.setErrorCode(getErrorCode());
        javaControler.setContext(context);

        long start = System.currentTimeMillis();

        javaControler.control(obj, dictionary);

        if (APP.isDebugEnabled()) {
            long end = System.currentTimeMillis();
            APP.debug("\nEtape java (" + getPriority() + ")" + getId() + " - "
                      + getType() + "\n" + getControlClass() + "\nen " + ((end - start) / 1000)
                      + " s");
        }
    }


    public void execute(Connection con, Dictionary dico, String controlTableName,
                        boolean mass, ControlContext context) throws SQLException, ControlException {
        dico.addVariable(ERROR_CODE, Integer.toString(getErrorCode()));
        dico.addVariable(ERROR_MESSAGE, getErrorMessage());
        this.dictionary.setParent(dico);
        if (getQuery() != null && getControlClass() == null) {
            // Step SQL
            sqlControler.execute(con, dictionary);

            if (tableToSpooled != null && APP.isDebugEnabled()) {
                for (String tableName : tableToSpooled) {
                    spoolTable(con, tableName);
                }
            }
        }
        else if (mass) {
            javaMassControler.setContext(context);
            javaMassControler.setControlTable(controlTableName);
            javaMassControler.control(con, dictionary);
        }
    }


    @Override
    public String toString() {
        return "control( \n" + getId() + "\n" + getParameters() + "\n" + getQuery() + ")";
    }


    /**
     * Cette methode est ajoute pour faciliter le DEBUG des tests release.
     *
     * @param con       connection SQL
     * @param tableName nom de la table
     *
     * @throws SQLException Erreur de lecture de la BD
     */
    private static void spoolTable(Connection con, String tableName)
          throws SQLException {
        Statement stmt = con.createStatement();
        APP.debug("******************** SPOOL de " + tableName + "********************");
        try {
            StringBuffer reportBuffer = new StringBuffer();
            ResultSet rs = stmt.executeQuery("select * from " + tableName);
            ResultSetMetaData rsmd = rs.getMetaData();

            // Spool Header
            int colmumnCount = rsmd.getColumnCount();
            for (int i = 1; i <= colmumnCount; i++) {
                reportBuffer.append("\t").append(rsmd.getColumnName(i));
            }
            reportBuffer.append("\n");
            for (int i = 1; i <= colmumnCount; i++) {
                reportBuffer.append("\t").append(rsmd.getColumnTypeName(i));
            }
            reportBuffer.append("\n");
            // Spool Content
            while (rs.next()) {
                for (int i = 1; i <= colmumnCount; i++) {
                    reportBuffer.append("\t").append(rs.getObject(i));
                }
                reportBuffer.append("\n");
            }

            APP.debug(reportBuffer);
        }
        finally {
            stmt.close();
        }
    }


    /**
     * Classe Helper pour une execution d'une Step en mode SQL.
     *
     * @author $Author: nadaud $
     * @version $Revision: 1.12 $
     */
    private class SqlStepHelper {
        public void execute(Connection con, Dictionary dico)
              throws SQLException {
            long start = System.currentTimeMillis();

            PreparedStatement stmt =
                  con.prepareStatement(dico.replaceVariables(getQuery().getSql()));
            int result = -1;
            try {
                if (hasParameter()) {
                    setStatementParameters(stmt, dico);
                }

                result = stmt.executeUpdate();

                SQLWarning warnings = stmt.getWarnings();
                String ignoreWarning = getQuery().getIgnoreWarningCode();
                if (warnings != null) {
                    if (ignoreWarning == null
                        || !ignoreWarning.contains(warnings.getSQLState())) {
                        throw warnings;
                    }
                }
            }
            finally {
                if (getQuery().useTemporaryTable()) {
                    dropTable(con, getQuery().getTemporaryTable());
                }
                if (APP.isDebugEnabled()) {
                    long end = System.currentTimeMillis();
                    APP.debug("\nEtape SQL (" + getPriority() + ")" + getId() + " - "
                              + getType() + "\n" + dico.replaceVariables(getQuery().getSql())
                              + "\n" + "nb lignes maj = " + result + "\nen "
                              + ((end - start) / 1000) + " s");
                }
                stmt.close();
            }
        }


        private void setStatementParameters(PreparedStatement stmt, Dictionary dico)
              throws SQLException {
            for (Parameter parameter : getParameters()) {
                parameter.initStatement(stmt, dico);
            }
        }


        private void dropTable(Connection con, String tableName) {
            try {
                Statement stmt = con.createStatement();
                try {
                    stmt.executeUpdate("drop table " + tableName);
                }
                finally {
                    stmt.close();
                }
            }
            catch (Exception error) {
                APP.debug("Step (" + getPriority() + ") drop table " + tableName, error);
            }
        }


        private boolean hasParameter() {
            return parameters != null && parameters.size() > 0;
        }
    }
}
