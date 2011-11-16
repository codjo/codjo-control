/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common;
import java.sql.Timestamp;
import junit.framework.TestCase;
/**
 * Classe de test de {@link Dictionary}.
 *
 * @author $Author: gonnot $
 * @version $Revision: 1.5 $
 */
public class DictionaryTest extends TestCase {
    private Dictionary dictionary;

    public void test_replaceVariables() {
        String pattern = "le $petit$";
        dictionary.addVariable("petit", "chiot");
        String result = dictionary.replaceVariables(pattern);
        assertEquals("le chiot", result);
    }


    public void test_replaceVariables_override() {
        String pattern = "le $petit$";

        dictionary.addVariable("petit", "chiot");
        String result = dictionary.replaceVariables(pattern);
        assertEquals("le chiot", result);

        dictionary.addVariable("petit", "bobo");
        result = dictionary.replaceVariables(pattern);
        assertEquals("le bobo", result);
    }


    public void test_replaceVariables_bug() {
        String pattern = "$control.table$";
        dictionary.addVariable("control.table", "chiot");
        String result = dictionary.replaceVariables(pattern);
        assertEquals("chiot", result);
    }


    public void test_replaceVariables_list() {
        String pattern = "le $petit$ $toto$";
        dictionary.addVariable(new Variable("toto", "chiot"));
        dictionary.addVariable(new Variable("petit", "tout petit"));
        String result = dictionary.replaceVariables(pattern);
        assertEquals("le tout petit chiot", result);
    }


    public void test_today() {
        String pattern = "aujourd'hui le $today$";
        dictionary.setNow(Timestamp.valueOf("2002-03-18 09:15:00"));
        String result = dictionary.replaceVariables(pattern);
        assertEquals("aujourd'hui le 2002-03-18", result);
    }


    public void test_today_twice() {
        String pattern = "aujourd'hui le $today$";
        dictionary.setNow(Timestamp.valueOf("2002-05-18 19:15:00"));
        dictionary.setNow(Timestamp.valueOf("2002-03-18 09:15:00"));
        String result = dictionary.replaceVariables(pattern);
        assertEquals("aujourd'hui le 2002-03-18", result);
    }


    @Override
    protected void setUp() {
        dictionary = new Dictionary();
    }
}
