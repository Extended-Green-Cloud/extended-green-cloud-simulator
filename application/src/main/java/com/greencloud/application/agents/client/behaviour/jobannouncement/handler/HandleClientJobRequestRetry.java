package com.greencloud.application.agents.client.behaviour.jobannouncement.handler;

import static com.greencloud.application.agents.client.behaviour.jobannouncement.handler.logs.JobAnnouncementHandlerLog.JOB_FINISHES_AFTER_DEADLINE;
import static com.greencloud.application.agents.client.behaviour.jobannouncement.handler.logs.JobAnnouncementHandlerLog.RETRY_CLIENT_JOB_REQUEST_LOG;
import static com.greencloud.application.agents.client.domain.ClientAgentConstants.JOB_RETRY_MINUTES_ADJUSTMENT;
import static com.greencloud.application.mapper.JobMapper.mapToJobWithNewTime;
import static com.greencloud.application.utils.TimeUtils.convertToSimulationTime;

import java.time.temporal.ChronoUnit;

import com.greencloud.commons.job.JobStatusEnum;
import com.gui.agents.ClientAgentNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.client.ClientAgent;
import com.greencloud.application.agents.client.behaviour.df.FindCloudNetworkAgents;
import com.greencloud.application.agents.client.behaviour.jobannouncement.initiator.InitiateNewJobAnnouncement;
import com.greencloud.application.domain.job.ClientJob;

import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.WakerBehaviour;

/**
 * Behaviours scheduler the retry oj job announcement process
 */
public class HandleClientJobRequestRetry extends WakerBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(HandleClientJobRequestRetry.class);

	private final ClientAgent myClientAgent;
	private final ClientJob job;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent   agent executing the behaviour
	 * @param timeout time after which the retry will be triggered
	 * @param job     job for which the retry is triggered
	 */
	public HandleClientJobRequestRetry(Agent agent, long timeout, ClientJob job) {
		super(agent, timeout);
		this.job = job;
		this.myClientAgent = (ClientAgent) agent;
	}

	/**
	 * Method retries the process of job announcement
	 */
	@Override
	protected void onWake() {
		recalculateJobTimeInterval();
		logger.info(RETRY_CLIENT_JOB_REQUEST_LOG);
		if(!jobFinishesBeforeDeadline()) {
			logger.info(JOB_FINISHES_AFTER_DEADLINE);
			myClientAgent.getGuiController().updateClientsCountByValue(-1);
			((ClientAgentNode) myClientAgent.getAgentNode()).updateJobStatus(JobStatusEnum.FAILED);
			return;
		}
		myAgent.addBehaviour(prepareStartingBehaviour(job));
	}

	private SequentialBehaviour prepareStartingBehaviour(final ClientJob job) {
		final ClientJob jobForRetry = mapToJobWithNewTime(job, myClientAgent.getSimulatedJobStart(),
				myClientAgent.getSimulatedJobEnd());

		var startingBehaviour = new SequentialBehaviour(myAgent);
		startingBehaviour.addSubBehaviour(new FindCloudNetworkAgents());
		startingBehaviour.addSubBehaviour(new InitiateNewJobAnnouncement(myAgent, null, jobForRetry));
		return startingBehaviour;
	}

	private void recalculateJobTimeInterval() {
		final long simulationAdjustment = convertToSimulationTime((long) JOB_RETRY_MINUTES_ADJUSTMENT * 60);
		myClientAgent.setSimulatedJobStart(
				myClientAgent.getSimulatedJobStart().plus(simulationAdjustment, ChronoUnit.MILLIS));
		myClientAgent.setSimulatedJobEnd(
				myClientAgent.getSimulatedJobEnd().plus(simulationAdjustment, ChronoUnit.MILLIS));
	}

	private boolean jobFinishesBeforeDeadline() {
		return myClientAgent.getSimulatedJobEnd().isBefore(myClientAgent.getSimulatedDeadline());
	}
}
