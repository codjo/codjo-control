package net.codjo.control.server.handler;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import net.codjo.mad.server.handler.sql.HandlerSqlMock;
import org.junit.Test;
/**
 *
 */
public class DbFilterSelectorFactoryTest extends TestCase {
    private DbFilterSelectorFactory queryFactory = new DbFilterSelectorFactory();


    @Test
    public void test_sorter_empty() throws Exception {
        Map<String, String> args = createSorterParameters("");
        String query = queryFactory.buildQuery(args, new HandlerSqlMock("dbFilter"));

        assertEquals("select distinct NAV as VALUE from AP_FUND_PRICE", query);
    }


    @Test
    public void test_sorter_null() throws Exception {
        Map<String, String> args = createSorterParameters(null);
        String query = queryFactory.buildQuery(args, new HandlerSqlMock("dbFilter"));

        assertEquals("select distinct NAV as VALUE from AP_FUND_PRICE", query);
    }


    @Test
    public void test_sorter_descending() throws Exception {
        Map<String, String> args = createSorterParameters("descending");
        String query = queryFactory.buildQuery(args, new HandlerSqlMock("dbFilter"));

        assertEquals("select distinct NAV as VALUE from AP_FUND_PRICE order by NAV desc", query);
    }


    @Test
    public void test_sorter_ascending() throws Exception {
        Map<String, String> args = createSorterParameters("ascending");
        String query = queryFactory.buildQuery(args, new HandlerSqlMock("dbFilter"));

        assertEquals("select distinct NAV as VALUE from AP_FUND_PRICE order by NAV", query);
    }


    @Test
    public void test_sorter_pipo() throws Exception {
        Map<String, String> args = createSorterParameters("pipo");
        String query = queryFactory.buildQuery(args, new HandlerSqlMock("dbFilter"));

        assertEquals("select distinct NAV as VALUE from AP_FUND_PRICE order by NAV", query);
    }


    private Map<String, String> createSorterParameters(String sorter) {
        Map<String, String> args = new HashMap<String, String>();
        args.put("tableName", "AP_FUND_PRICE");
        args.put("columnName", "NAV");
        args.put("sorter", sorter);
        return args;
    }
}
