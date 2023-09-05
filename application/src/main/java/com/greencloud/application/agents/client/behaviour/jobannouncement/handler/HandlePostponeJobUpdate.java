package com.greencloud.application.agents.client.behaviour.jobannouncement.handler;

import static com.greencloud.application.agents.scheduler.constants.SchedulerAgentConstants.JOB_RETRY_MINUTES_ADJUSTMENT;
import static com.greencloud.application.utils.TimeUtils.postponeTime;

import java.time.Instant;

import com.greencloud.application.agents.client.ClientAgent;
import com.greencloud.application.agents.client.domain.enums.ClientJobUpdateEnum;

import jade.lang.acl.ACLMessage;

/**
 * Behaviour handles update regarding postponing a given job
 */
public class HandlePostponeJobUpdate extends AbstractJobUpdateHandler {

	public HandlePostponeJobUpdate(final ACLMessage message, final ClientAgent myClient,
			final ClientJobUpdateEnum updateEnum) {
		super(message, myClient, updateEnum);
	}

	/**
	 * Method updates the time frames of the job (or job part)
	 */
	@Override
	public void action() {
		final Instant postponedStart = postponeTime(myClient.getJobExecution().getJobSimulatedStart(),
				JOB_RETRY_MINUTES_ADJUSTMENT);
		final Instant postponedEnd = postponeTime(myClient.getJobExecution().getJobSimulatedEnd(),
				JOB_RETRY_MINUTES_ADJUSTMENT);
		readjustJobTimeFrames(postponedStart, postponedEnd);
	}
}
