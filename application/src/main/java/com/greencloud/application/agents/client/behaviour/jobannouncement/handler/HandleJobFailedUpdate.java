package com.greencloud.application.agents.client.behaviour.jobannouncement.handler;

import static com.greencloud.application.utils.MessagingUtils.readMessageContent;

import com.greencloud.application.agents.client.ClientAgent;
import com.greencloud.application.agents.client.domain.enums.ClientJobUpdateEnum;
import com.greencloud.application.domain.job.JobStatusUpdate;
import com.greencloud.commons.domain.job.enums.JobClientStatusEnum;
import com.gui.agents.ClientAgentNode;

import jade.lang.acl.ACLMessage;

/**
 * Behaviour handles information regarding job failure
 */
public class HandleJobFailedUpdate extends AbstractJobUpdateHandler {
	public HandleJobFailedUpdate(final ACLMessage message, final ClientAgent myClient,
			final ClientJobUpdateEnum updateEnum) {
		super(message, myClient, updateEnum);
	}

	/**
	 * Method updates the job status on GUI, logs failure event to the database and terminates the agent.
	 */
	@Override
	public void action() {
		final JobClientStatusEnum jobStatus = updateEnum.getJobStatus();
		final JobStatusUpdate jobUpdate = readMessageContent(message, JobStatusUpdate.class);

		((ClientAgentNode) myClient.getAgentNode()).updateJobStatus(jobStatus);
		myClient.getJobExecution().updateJobStatusDuration(jobStatus, jobUpdate.getChangeTime());
		myClient.getGuiController().updateClientsCountByValue(-1);
		myClient.getGuiController().updateFailedJobsCountByValue(1);
		myClient.manage().writeClientData(true);
		myClient.doDelete();
	}
}
