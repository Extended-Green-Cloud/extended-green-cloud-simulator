package com.greencloud.application.agents.scheduler.behaviour.scheduling.handler;

import static com.greencloud.application.agents.scheduler.behaviour.scheduling.handler.logs.JobSchedulingHandlerLog.ANNOUNCE_JOB_CNA_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.scheduling.handler.logs.JobSchedulingHandlerLog.JOB_ADJUST_TIME_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.scheduling.handler.logs.JobSchedulingHandlerLog.JOB_EXECUTION_AFTER_DEADLINE_LOG;
import static com.greencloud.application.agents.scheduler.constants.SchedulerAgentConstants.JOB_PROCESSING_DEADLINE_ADJUSTMENT;
import static com.greencloud.application.agents.scheduler.constants.SchedulerAgentConstants.JOB_PROCESSING_TIME_ADJUSTMENT;
import static com.greencloud.application.agents.scheduler.constants.SchedulerAgentConstants.SEND_NEXT_JOB_TIMEOUT;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.mapper.JobMapper.mapToJobWithNewTime;
import static com.greencloud.application.messages.constants.MessageConversationConstants.FAILED_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.PROCESSING_JOB_ID;
import static com.greencloud.application.messages.factory.JobStatusMessageFactory.prepareJobAdjustmentMessage;
import static com.greencloud.application.messages.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.domain.job.enums.JobExecutionStateEnum.replaceStatusToActive;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.CREATED;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.scheduler.SchedulerAgent;
import com.greencloud.application.agents.scheduler.behaviour.scheduling.handler.logs.JobSchedulingHandlerLog;
import com.greencloud.application.agents.scheduler.behaviour.scheduling.initiator.InitiateCNALookup;
import com.greencloud.commons.domain.job.ClientJob;

import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour periodically announces new jobs to Cloud Network Agents
 */
public class HandleJobAnnouncement extends TickerBehaviour {

	private static final Logger logger = getLogger(HandleJobAnnouncement.class);
	private final SchedulerAgent myScheduler;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent scheduler agent executing behaviour
	 */
	public HandleJobAnnouncement(final SchedulerAgent agent) {
		super(agent, SEND_NEXT_JOB_TIMEOUT);
		this.myScheduler = agent;
	}

	@Override
	protected void onTick() {
		if (myScheduler.getAvailableCloudNetworks().isEmpty()) {
			logger.info(JobSchedulingHandlerLog.NO_AVAILABLE_CNA_LOG);
			return;
		}

		if (!myScheduler.getJobsToBeExecuted().isEmpty()) {
			final ClientJob jobToExecute = myScheduler.getJobsToBeExecuted().poll();
			myScheduler.manage().updateJobQueueGUI();
			if (canJobBeAnnounced().test(jobToExecute)) {
				announceJobToCloudNetworkAgents(requireNonNull(jobToExecute));
			}
		}
	}

	private Predicate<ClientJob> canJobBeAnnounced() {
		return (job -> nonNull(job)
				&& myScheduler.getClientJobs().containsKey(job)
				&& myScheduler.getClientJobs().get(job).equals(CREATED));
	}

	private void announceJobToCloudNetworkAgents(ClientJob jobToExecute) {
		MDC.put(MDC_JOB_ID, jobToExecute.getJobId());
		final ClientJob adjustedJob = getAdjustedJob(jobToExecute);

		if (nonNull(adjustedJob)) {
			logger.info(ANNOUNCE_JOB_CNA_LOG, mapToJobInstanceId(adjustedJob));
			final ACLMessage clientMessage = prepareJobStatusMessageForClient(adjustedJob, PROCESSING_JOB_ID);

			replaceStatusToActive(myScheduler.getClientJobs(), adjustedJob);
			myScheduler.send(clientMessage);
			myScheduler.addBehaviour(InitiateCNALookup.create(myScheduler, adjustedJob));
		}
	}

	private ClientJob getAdjustedJob(final ClientJob job) {
		final long jobDuration = MILLIS.between(job.getStartTime(), job.getEndTime());
		final Instant newAdjustedStart = getCurrentTime().plusMillis(JOB_PROCESSING_TIME_ADJUSTMENT);
		final Instant newAdjustedEnd = newAdjustedStart.plusMillis(jobDuration);

		if (job.getStartTime().isAfter(newAdjustedStart)) {
			return job;
		}
		if (newAdjustedEnd.isAfter(job.getDeadline().minusMillis(JOB_PROCESSING_DEADLINE_ADJUSTMENT))) {
			logger.info(JOB_EXECUTION_AFTER_DEADLINE_LOG, job.getJobId());
			myScheduler.getClientJobs().remove(job);
			myScheduler.manage().sendStatusMessageToClient(prepareJobStatusMessageForClient(job, FAILED_JOB_ID));
			return null;
		}

		logger.info(JOB_ADJUST_TIME_LOG, job.getJobId());
		final ClientJob adjustedJob = mapToJobWithNewTime(job, newAdjustedStart, newAdjustedEnd);
		myScheduler.manage().swapJobInstances(adjustedJob, job);
		myScheduler.manage().sendStatusMessageToClient(prepareJobAdjustmentMessage(adjustedJob));
		return adjustedJob;
	}
}
