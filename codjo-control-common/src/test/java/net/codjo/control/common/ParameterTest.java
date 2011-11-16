/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import junit.framework.TestCase;
import org.easymock.MockControl;
/**
 * Description of the Class
 *
 * @author $Author: gonnot $
 * @version $Revision: 1.2 $
 */
public class ParameterTest extends TestCase {
    private Dictionary dico;
    private PreparedStatement mockStatement;
    private MockControl statementControl;


    public ParameterTest(String testCaseName) {
        super(testCaseName);
    }


    public void test_initStatement_int() throws Exception {
        Parameter parameter = new Parameter(1, "int", "5");

        mockStatement.setInt(1, 5);
        statementControl.replay();

        parameter.initStatement(mockStatement, dico);

        statementControl.verify();
    }


    public void test_initStatement_int_var() throws Exception {
        Parameter parameter = new Parameter(1, "int", "$error_code$");
        dico.addVariable("error_code", "5");

        mockStatement.setInt(1, 5);
        statementControl.replay();

        parameter.initStatement(mockStatement, dico);

        statementControl.verify();
    }


    public void test_initStatement_string() throws Exception {
        Parameter parameter = new Parameter(1, "string", "str");

        mockStatement.setString(1, "str");
        statementControl.replay();

        parameter.initStatement(mockStatement, dico);

        statementControl.verify();
    }


    public void test_initStatement_string_var() throws Exception {
        Parameter parameter = new Parameter(1, "string", "$var$");

        mockStatement.setString(1, "value");
        statementControl.replay();

        parameter.initStatement(mockStatement, dico);

        statementControl.verify();
    }


    public void test_initStatement_today() throws Exception {
        Timestamp ima = new Timestamp(System.currentTimeMillis());
        dico.setNow(ima);
        Parameter parameter = new Parameter(1, "now", "");

        mockStatement.setTimestamp(1, ima);
        statementControl.replay();

        parameter.initStatement(mockStatement, dico);

        statementControl.verify();
    }


    @Override
    protected void setUp() {
        initMockStuff();
        dico = new Dictionary();
        dico.addVariable("var", "value");
    }


    private void initMockStuff() {
        statementControl = MockControl.createControl(PreparedStatement.class);
        mockStatement = (PreparedStatement)statementControl.getMock();
    }
}
