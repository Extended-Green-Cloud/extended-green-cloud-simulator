package com.greencloud.application.agents.scheduler.behaviour.job.scheduling.handler;

import static com.greencloud.application.agents.scheduler.behaviour.job.scheduling.handler.logs.JobSchedulingHandlerLog.ANNOUNCE_JOB_CNA_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.job.scheduling.handler.logs.JobSchedulingHandlerLog.JOB_ADJUST_TIME_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.job.scheduling.handler.logs.JobSchedulingHandlerLog.JOB_EXECUTION_AFTER_DEADLINE_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.job.scheduling.handler.logs.JobSchedulingHandlerLog.NO_AVAILABLE_CNA_LOG;
import static com.greencloud.application.agents.scheduler.domain.SchedulerAgentConstants.JOB_PROCESSING_DEADLINE_ADJUSTMENT;
import static com.greencloud.application.agents.scheduler.domain.SchedulerAgentConstants.JOB_PROCESSING_TIME_ADJUSTMENT;
import static com.greencloud.application.agents.scheduler.domain.SchedulerAgentConstants.SEND_NEXT_JOB_TIMEOUT;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.CREATED;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.PROCESSING;
import static com.greencloud.application.mapper.JobMapper.mapToJobWithNewTime;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.FAILED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.PROCESSING_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.SCHEDULER_JOB_CFP_PROTOCOL;
import static com.greencloud.application.messages.domain.factory.CallForProposalMessageFactory.createCallForProposal;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobAdjustmentMessage;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Objects.isNull;

import java.time.Instant;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.scheduler.SchedulerAgent;
import com.greencloud.application.agents.scheduler.behaviour.job.scheduling.initiator.InitiateCNALookup;
import com.greencloud.commons.job.ClientJob;

import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour periodically announces new jobs to Cloud Network Agents
 */
public class HandleJobAnnouncement extends TickerBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(HandleJobAnnouncement.class);
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
		if (myScheduler.getJobsToBeExecuted().isEmpty()) {
			// do nothing
			return;
		}

		if (myScheduler.getAvailableCloudNetworks().isEmpty()) {
			// do nothing
			MDC.clear();
			logger.info(NO_AVAILABLE_CNA_LOG);
			return;
		}

		final ClientJob jobToExecute = myScheduler.getJobsToBeExecuted().poll();
		if (Objects.nonNull(jobToExecute)
				&& !myScheduler.getFailedJobs().contains(jobToExecute.getJobId().split("#")[0])
				&& myScheduler.getClientJobs().containsKey(jobToExecute)
				&& myScheduler.getClientJobs().get(jobToExecute).equals(CREATED)) {
			announceJobToCloudNetworkAgents(jobToExecute);
		}
	}

	private void announceJobToCloudNetworkAgents(ClientJob jobToExecute) {
		MDC.put(MDC_JOB_ID, jobToExecute.getJobId());
		final ClientJob adjustedJob = getAdjustedJob(jobToExecute);
		if (isNull(adjustedJob)) {
			// do nothing
			return;
		}

		logger.info(ANNOUNCE_JOB_CNA_LOG, adjustedJob.getJobId());
		final ACLMessage cfp = createCallForProposal(adjustedJob, myScheduler.getAvailableCloudNetworks(),
				SCHEDULER_JOB_CFP_PROTOCOL);
		final ACLMessage clientMessage = prepareJobStatusMessageForClient(adjustedJob.getClientIdentifier(),
				adjustedJob.getJobId(), PROCESSING_JOB_ID);

		myScheduler.getClientJobs().replace(adjustedJob, CREATED, PROCESSING);
		myScheduler.send(clientMessage);
		myScheduler.manage().updateJobQueue();
		((ParallelBehaviour) parent).addSubBehaviour(new InitiateCNALookup(myScheduler, cfp, adjustedJob));
	}

	private ClientJob getAdjustedJob(final ClientJob job) {
		final long jobDuration = MILLIS.between(job.getStartTime(), job.getEndTime());
		final Instant newAdjustedStart = getCurrentTime().plusMillis(JOB_PROCESSING_TIME_ADJUSTMENT);
		final Instant newAdjustedEnd = newAdjustedStart.plusMillis(jobDuration);

		if (job.getStartTime().isBefore(newAdjustedStart)) {
			return job;
		}
		if (newAdjustedEnd.isAfter(job.getDeadline().plusMillis(JOB_PROCESSING_DEADLINE_ADJUSTMENT))) {
			logger.info(JOB_EXECUTION_AFTER_DEADLINE_LOG, job.getJobId());
			myScheduler.getClientJobs().remove(job);
			myScheduler.send(
					prepareJobStatusMessageForClient(job.getClientIdentifier(), job.getJobId(), FAILED_JOB_ID));
			return null;
		}

		logger.info(JOB_ADJUST_TIME_LOG, job.getJobId());
		final ClientJob adjustedJob = mapToJobWithNewTime(job, newAdjustedStart, newAdjustedEnd);
		myScheduler.manage().swapJobInstances(adjustedJob, job);
		myScheduler.send(prepareJobAdjustmentMessage(job.getClientIdentifier(), adjustedJob));
		return adjustedJob;
	}
}
