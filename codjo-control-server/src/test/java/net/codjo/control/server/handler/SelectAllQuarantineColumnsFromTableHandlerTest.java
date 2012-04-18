package net.codjo.control.server.handler;
import java.sql.SQLException;
import net.codjo.mad.server.handler.HandlerCommand;
import net.codjo.mad.server.handler.HandlerCommand.CommandResult;
import net.codjo.mad.server.handler.HandlerCommandTestCase;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static net.codjo.control.common.util.SqlNameCodec.decodeList;
import static net.codjo.database.common.api.structure.SqlTable.table;
import static net.codjo.test.common.matcher.JUnitMatchers.*;
/**
 *
 */
public class SelectAllQuarantineColumnsFromTableHandlerTest extends HandlerCommandTestCase {
    @Test
    public void test_nominal() throws Exception {
        getJdbcFixture().create(table("TEST_CONTROL"), "COL_2 int, COL_1 int");

        CommandResult result = assertExecuteQuery(singletonMap("tableName", "TEST_CONTROL"));

        assertThat(decodeList(result.toString()), is(asList("COL_1", "COL_2")));
    }


    @Test
    public void test_unkownTable() throws Exception {
        try {
            assertExecuteQuery(singletonMap("tableName", "UNKOWN_TABLE"));
            fail();
        }
        catch (SQLException e) {
            ;
        }
    }


    @Override
    protected HandlerCommand createHandlerCommand() {
        return new SelectAllQuarantineColumnsFromTableHandler();
    }


    @Override
    protected String getHandlerId() {
        return "selectAllQuarantineColumnsFromTable";
    }
}
