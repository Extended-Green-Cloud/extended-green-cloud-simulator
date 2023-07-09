package com.greencloud.application.agents.scheduler.behaviour.job.cancellation;

import static com.greencloud.application.agents.scheduler.behaviour.job.cancellation.logs.JobCancellationLogs.JOB_CANCELLATION_COMPLETED_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.job.cancellation.logs.JobCancellationLogs.JOB_CANCELLING_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.job.cancellation.logs.JobCancellationLogs.NOT_ALL_JOB_PARTS_CANCELLED_LOG;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.utils.MessagingUtils.readMessageContent;
import static com.greencloud.application.messages.factory.JobStatusMessageFactory.prepareJobCancellationMessage;
import static com.greencloud.application.utils.JobUtils.getJobById;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Objects;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.scheduler.SchedulerAgent;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Behaviour responsible for announcing cancellation of a job to Cloud Networks
 */
public class InitiateJobCancellation extends AchieveREInitiator {

	private static final Logger logger = getLogger(InitiateJobCancellation.class);

	private final SchedulerAgent mySchedulerAgent;
	private final String originalJobId;

	private InitiateJobCancellation(final SchedulerAgent agent, final ACLMessage cancel, final String jobId) {
		super(agent, cancel);

		this.mySchedulerAgent = agent;
		this.originalJobId = jobId;
	}

	/**
	 * Method creates the behaviour
	 *
	 * @param agent         agent executing the behaviour
	 * @param originalJobId id of the original job - as it was before the split
	 * @return InitiateJobCancellation
	 */
	public static InitiateJobCancellation create(final SchedulerAgent agent, final String originalJobId) {
		final ACLMessage cancel = prepareJobCancellationMessage(originalJobId,
				agent.getAvailableCloudNetworks().toArray(new AID[0]));
		return new InitiateJobCancellation(agent, cancel, originalJobId);
	}

	/**
	 * Method handles information about successful job cancellation by removing all associated job parts from
	 * the scheduler job part list.
	 *
	 * @param inform retrieved message containing list of all cancelled job parts (i.e. list of IDs)
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void handleInform(ACLMessage inform) {
		final List<String> cancelledParts = readMessageContent(inform, (Class<List<String>>) ((Class) List.class));

		cancelledParts.stream()
				.map(jobPart -> getJobById(jobPart, mySchedulerAgent.getClientJobs()))
				.filter(Objects::nonNull)
				.forEach(job -> mySchedulerAgent.manage().handleJobCleanUp(job, true));
	}

	/**
	 * Method handles all received results of job cancellation. Method verifies if all parts of a given job has been
	 * successfully removed and if not, schedules cancellation retry.
	 *
	 * @param responses received results
	 */
	@Override
	protected void handleAllResultNotifications(final Vector responses) {
		MDC.put(MDC_JOB_ID, originalJobId);
		logger.info(JOB_CANCELLATION_COMPLETED_LOG);

		if (!mySchedulerAgent.getJobParts().get(originalJobId).isEmpty()) {
			processCancelRetry();
		} else {
			logger.info(JOB_CANCELLING_LOG, originalJobId);
		}
	}

	private void processCancelRetry() {
		final int size = mySchedulerAgent.getJobParts().get(originalJobId).size();
		logger.warn(NOT_ALL_JOB_PARTS_CANCELLED_LOG, size);

		mySchedulerAgent.addBehaviour(new RetryJobCancellation(mySchedulerAgent, originalJobId));
	}

}
