package net.codjo.control.server.handler;
import net.codjo.control.common.util.FilterConstants;
import net.codjo.datagen.DatagenFixture;
import net.codjo.datagen.DatagenFixture.DatagenInput;
import net.codjo.util.file.FileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static net.codjo.test.common.matcher.JUnitMatchers.*;
/**
 *
 */
public class DatagenConsistencyTest {
    private DatagenFixture datagen = new DatagenFixture(DatagenConsistencyTest.class,
                                                        DatagenInput.resource("entityFile.xml"));


    @Test
    public void test_generation() throws Exception {
        datagen.generate();

        String handlerContent =
              FileUtil.loadContent(datagen.getHandlerFile("com/mycompany/SelectAllQUserBookWithParametersHandler.java"));

        assertThat(FilterConstants.ALL,
                   describedAs(
                         "This constant must be 'Tout' because the value is defined in datagen and control (cf. query built for user-quarantine and FilterValueConverter)",
                         is("Tout")));

        assertContains(handlerContent,
                       "select QUARANTINE_ID,TITLE from Q_AP_USER_BOOK where (TITLE = ? or ? = 'Tout')",
                       "query.setString(1,  net.codjo.control.server.handler.FilterValueConverter.convertFromStringValue(String.class, (String) pks.get(\"title\")));",
                       "query.setString(2,  net.codjo.control.server.handler.FilterValueConverter.convertFromStringValue(String.class, (String) pks.get(\"title\")));");

//        TODO faire un test avec
//        <field name="creationDatetime" type="java.sql.Timestamp">
//                <description>Import date</description>
//                <sql type="timestamp"/>
//            </field>
//        car ca plante...
    }


    private static void assertContains(String handlerContent, String... expecteds) {
        for (String expected : expecteds) {
            assertThat(handlerContent,
                       containsString(expected));
        }
    }


    @Before
    public void setUp() throws Exception {
        datagen.doSetUp();
    }


    @After
    public void tearDown() throws Exception {
        datagen.doTearDown();
    }
}
