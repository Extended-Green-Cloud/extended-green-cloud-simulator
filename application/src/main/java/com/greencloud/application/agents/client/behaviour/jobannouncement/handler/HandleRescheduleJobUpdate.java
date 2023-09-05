package com.greencloud.application.agents.client.behaviour.jobannouncement.handler;

import static com.greencloud.application.utils.MessagingUtils.readMessageContent;

import com.greencloud.application.agents.client.ClientAgent;
import com.greencloud.application.agents.client.domain.enums.ClientJobUpdateEnum;
import com.greencloud.application.domain.job.JobTimeFrames;

import jade.lang.acl.ACLMessage;

/**
 * Behaviour handles update regarding rescheduling the execution a given job
 */
public class HandleRescheduleJobUpdate extends AbstractJobUpdateHandler {

	public HandleRescheduleJobUpdate(final ACLMessage message, final ClientAgent myClient,
			final ClientJobUpdateEnum updateEnum) {
		super(message, myClient, updateEnum);
	}

	/**
	 * Method updates the execution time of the job (or job part) according to received time frames
	 */
	@Override
	public void action() {
		final JobTimeFrames newTimeFrames = readMessageContent(message, JobTimeFrames.class);
		readjustJobTimeFrames(newTimeFrames.getNewJobStart(), newTimeFrames.getNewJobEnd());
	}
}
