package net.codjo.control.server.handler;
import java.util.Map;
import net.codjo.mad.server.handler.HandlerException;
import net.codjo.mad.server.handler.sql.QueryBuilder;
import net.codjo.mad.server.handler.sql.SqlHandler;
import net.codjo.util.string.StringUtil;
/**
 *
 */
public class DbFilterSelectorFactory implements QueryBuilder {
    public String buildQuery(final Map args, SqlHandler sqlHandler) throws HandlerException {
        String tableName = (String)args.get("tableName");
        String columnName = StringUtil.javaToSqlName((String)args.get("columnName"));
        String sorter = (String)args.get("sorter");

        if ((sorter == null) || ("".equals(sorter))) {
            return "select distinct " + columnName + " as VALUE from " + tableName;
        }
        else {
            return "select distinct " + columnName + " as VALUE from " + tableName + " order by " +
                   buildOrderByClause(sorter, columnName);
        }
    }


    private String buildOrderByClause(String sorter, String columnName) {
        if ("ascending".equals(sorter)) {
            return columnName;
        }
        else if ("descending".equals(sorter)) {
            return columnName + " desc";
        }
        else {
            return columnName;
        }
    }
}
