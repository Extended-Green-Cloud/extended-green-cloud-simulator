package com.greencloud.application.agents.client.behaviour.jobannouncement.handler;

import static com.greencloud.application.messages.constants.MessageConversationConstants.BACK_UP_POWER_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.GREEN_POWER_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.ON_HOLD_JOB_ID;
import static com.greencloud.application.utils.MessagingUtils.readMessageContent;

import java.util.List;

import com.greencloud.application.agents.client.ClientAgent;
import com.greencloud.application.agents.client.domain.enums.ClientJobUpdateEnum;
import com.greencloud.application.domain.job.JobStatusUpdate;

import jade.lang.acl.ACLMessage;

/**
 * Behaviour handles in generic way information about job status update
 */
public class HandleGenericJobStatusUpdate extends AbstractJobUpdateHandler {

	private static final List<String> serverUpdateStatuses = List.of(BACK_UP_POWER_JOB_ID, GREEN_POWER_JOB_ID,
			ON_HOLD_JOB_ID);

	public HandleGenericJobStatusUpdate(final ACLMessage message, final ClientAgent myClient,
			final ClientJobUpdateEnum updateEnum) {
		super(message, myClient, updateEnum);
	}

	/**
	 * Method updates information about job status and duration of job execution at the previous status
	 */
	@Override
	public void action() {
		final JobStatusUpdate jobUpdate = readMessageContent(message, JobStatusUpdate.class);

		if (serverUpdateStatuses.contains(message.getConversationId())) {
			measureTimeToRetrieveTheMessage(jobUpdate);
		}
		updateInformationOfJobStatusUpdate(jobUpdate);
	}
}
