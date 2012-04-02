package net.codjo.control.server.handler;
import java.sql.SQLException;
import java.util.List;
import net.codjo.mad.server.handler.HandlerCommand;
import net.codjo.mad.server.handler.HandlerException;

import static net.codjo.control.common.util.SQLUtil.determineDbFieldList;
import static net.codjo.control.common.util.SqlNameCodec.encodeList;
/**
 *
 */
public class SelectAllQuarantineColumnsFromTableHandler extends HandlerCommand {

    @Override
    public CommandResult executeQuery(CommandQuery query) throws HandlerException, SQLException {
        List<String> columnsName = determineDbFieldList(getContext().getConnection(),
                                                        query.getArgumentString("tableName"));
        return createResult(encodeList(columnsName));
    }
}
