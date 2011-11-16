package net.codjo.control.server.handler;
import net.codjo.mad.server.handler.HandlerException;
import net.codjo.mad.server.handler.sql.QueryBuilder;
import net.codjo.mad.server.handler.sql.SqlHandler;
import java.util.Map;
/**
 *
 */
public class DbFilterColumnSelectorFactory implements QueryBuilder {
    public String buildQuery(final Map args, SqlHandler sqlHandler) throws HandlerException {
        String tableName = (String)args.get("tableName");

        return "select name from syscolumns where id=object_id('" + tableName + "')";
    }
}
