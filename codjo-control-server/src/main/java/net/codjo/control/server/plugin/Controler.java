/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.control.server.plugin;
import net.codjo.control.common.message.ControlJobRequest;
import java.sql.SQLException;
/**
 *
 */
interface Controler {
    PostControlAudit execute(ControlJobRequest controlJobRequest)
          throws SQLException, QuarantineControlException;
}
