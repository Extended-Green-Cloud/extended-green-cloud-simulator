package agents.client.behaviour.jobannouncement.handler;

import static agents.client.behaviour.jobannouncement.handler.logs.JobAnnouncementHandlerLog.RETRY_CLIENT_JOB_REQUEST_LOG;
import static agents.client.domain.ClientAgentConstants.JOB_RETRY_MINUTES_ADJUSTMENT;
import static mapper.JobMapper.mapToJobWithNewTime;
import static utils.TimeUtils.convertToSimulationTime;

import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agents.client.ClientAgent;
import agents.client.behaviour.df.FindCloudNetworkAgents;
import agents.client.behaviour.jobannouncement.initiator.InitiateNewJobAnnouncement;
import domain.job.ClientJob;
import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.WakerBehaviour;

/**
 * Behaviours scheduler the retry oj job announcement process
 */
public class HandleClientJobRequestRetry extends WakerBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(HandleClientJobRequestRetry.class);

	private final ClientAgent myClientAgent;
	private final String guid;
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
		this.guid = myClientAgent.getName();
	}

	/**
	 * Method retries the process of job announcement
	 */
	@Override
	protected void onWake() {
		myAgent.addBehaviour(prepareStartingBehaviour(job));
		logger.info(RETRY_CLIENT_JOB_REQUEST_LOG, guid, job.getJobId());
	}

	private SequentialBehaviour prepareStartingBehaviour(final ClientJob job) {
		recalculateJobTimeInterval();
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
}
