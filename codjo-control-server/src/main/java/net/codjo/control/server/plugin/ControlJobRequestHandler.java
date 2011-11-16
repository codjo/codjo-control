package net.codjo.control.server.plugin;
import net.codjo.agent.UserId;
import net.codjo.workflow.common.message.JobRequest;
import net.codjo.workflow.common.organiser.Job;
import net.codjo.workflow.server.organiser.AbstractJobBuilder;

class ControlJobRequestHandler extends AbstractJobBuilder {

    @Override
    public boolean accept(JobRequest jobRequest) {
        return "control".equals(jobRequest.getType());
    }


    @Override
    public Job createJob(JobRequest jobRequest, Job job, UserId userId) {
        job.setTable(jobRequest.getArguments().get("quarantineTable"));
        return job;
    }
}
