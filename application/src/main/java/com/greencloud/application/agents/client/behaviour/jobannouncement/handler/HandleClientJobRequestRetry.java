package com.greencloud.application.agents.client.behaviour.jobannouncement.handler;

import static com.greencloud.application.agents.client.behaviour.jobannouncement.handler.logs.JobAnnouncementHandlerLog.RETRY_CLIENT_JOB_REQUEST_LOG;
import static com.greencloud.application.agents.client.domain.ClientAgentConstants.JOB_RETRY_MINUTES_ADJUSTMENT;
import static com.greencloud.application.utils.TimeUtils.convertToSimulationTime;

import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.client.ClientAgent;
import com.greencloud.application.agents.client.behaviour.df.FindCloudNetworkAgents;
import com.greencloud.application.agents.client.behaviour.jobannouncement.initiator.InitiateNewJobAnnouncement;
import com.greencloud.application.domain.job.Job;
import com.greencloud.application.mapper.JobMapper;

import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.WakerBehaviour;

/**
 * Behaviours scheduler the retry oj job announcement process
 */
public class HandleClientJobRequestRetry extends WakerBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(HandleClientJobRequestRetry.class);

	private final ClientAgent myClientAgent;
	private final Job job;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent   agent executing the behaviour
	 * @param timeout time after which the retry will be triggered
	 * @param job     job for which the retry is triggered
	 */
	public HandleClientJobRequestRetry(Agent agent, long timeout, Job job) {
		super(agent, timeout);
		this.job = job;
		this.myClientAgent = (ClientAgent) agent;
	}

	/**
	 * Method retries the process of job announcement
	 */
	@Override
	protected void onWake() {
		myAgent.addBehaviour(prepareStartingBehaviour(job));
		logger.info(RETRY_CLIENT_JOB_REQUEST_LOG);
	}

	private SequentialBehaviour prepareStartingBehaviour(final Job job) {
		recalculateJobTimeInterval();
		final Job jobForRetry = JobMapper.mapToJobWithNewTime(job, myClientAgent.getSimulatedJobStart(),
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
}
