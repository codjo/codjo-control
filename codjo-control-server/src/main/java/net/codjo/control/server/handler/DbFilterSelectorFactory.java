package net.codjo.control.server.handler;
import net.codjo.mad.server.handler.HandlerException;
import net.codjo.mad.server.handler.sql.QueryBuilder;
import net.codjo.mad.server.handler.sql.SqlHandler;
import net.codjo.util.string.StringUtil;
import java.util.Map;
/**
 *
 */
public class DbFilterSelectorFactory implements QueryBuilder {
    public String buildQuery(final Map args, SqlHandler sqlHandler) throws HandlerException {
        String tableName = (String)args.get("tableName");
        String columnName = StringUtil.javaToSqlName((String)args.get("columnName"));

        return "select distinct " + columnName + " as VALUE from " + tableName;
    }
}
