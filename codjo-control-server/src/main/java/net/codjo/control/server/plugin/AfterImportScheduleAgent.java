package net.codjo.control.server.plugin;
import net.codjo.imports.common.message.ImportJobAuditArgument;
import net.codjo.imports.server.plugin.ImportServerPlugin;
import net.codjo.workflow.common.message.JobRequest;
import net.codjo.workflow.common.message.ScheduleContract;
import net.codjo.workflow.server.api.ScheduleAgent;
import net.codjo.control.common.message.ControlJobRequest;
import net.codjo.control.common.message.SourceOfData;
/**
 *
 */
class AfterImportScheduleAgent extends ScheduleAgent {
    AfterImportScheduleAgent() {
        super(new LaunchControlAfterImport());
    }


    private static class LaunchControlAfterImport extends ScheduleAgent.AbstractHandler
          implements ImportJobAuditArgument {
        public boolean acceptContract(ScheduleContract contract) {
            return ImportServerPlugin.IMPORT_JOB_TYPE.equals(contract.getRequest().getType())
                   && contract.getPostAudit().getArguments().get(FILLED_TABLE) != null;
        }


        public JobRequest createNextRequest(ScheduleContract contract) {
            String quarantine = contract.getPostAudit().getArguments().get(FILLED_TABLE);
            ControlJobRequest request = new ControlJobRequest(quarantine);
            request.addPath(SourceOfData.IMPORT);
            return request.toRequest();
        }
    }
}
