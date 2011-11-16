/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.common.util;
import junit.framework.TestCase;
/**
 * Description of the Class
 *
 * @author $Author: gonnot $
 * @version $Revision: 1.2 $
 */
public class PropertyConverterTest extends TestCase {
    PropertyConverter converter;


    public PropertyConverterTest(String testCaseName) {
        super(testCaseName);
    }


    public void test_buildQuery() {
        assertEquals("SELECT p FROM "
                     + "net.codjo.control.common.util.PropertyConverterTest" + " p WHERE id = $1 ",
                     converter.buildSelectByPk(PropertyConverterTest.class));
    }


    public void test_convertLoadWithException() {
        try {
            converter.load(PropertyConverterTest.class, "pk");
            fail("convert failed !");
        }
        catch (Exception ex) {
            assertEquals("La mécanique de conversion a été désactivée.", ex.getMessage());
        }
    }


    @Override
    protected void setUp() {
        converter = new PropertyConverter("subEntity", "id");
    }
}
