package com.greencloud.application.agents.scheduler.behaviour.jobscheduling.handler;

import static com.greencloud.application.agents.scheduler.behaviour.jobscheduling.handler.logs.JobSchedulingHandlerLog.ANNOUNCE_JOB_CNA_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.jobscheduling.handler.logs.JobSchedulingHandlerLog.JOB_ADJUST_TIME_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.jobscheduling.handler.logs.JobSchedulingHandlerLog.JOB_EXECUTION_AFTER_DEADLINE_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.jobscheduling.handler.logs.JobSchedulingHandlerLog.NO_AVAILABLE_CNA_LOG;
import static com.greencloud.application.agents.scheduler.domain.SchedulerAgentConstants.JOB_PROCESSING_DEADLINE_ADJUSTMENT;
import static com.greencloud.application.agents.scheduler.domain.SchedulerAgentConstants.JOB_PROCESSING_TIME_ADJUSTMENT;
import static com.greencloud.application.agents.scheduler.domain.SchedulerAgentConstants.SEND_NEXT_JOB_TIMEOUT;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.domain.job.JobStatusEnum.CREATED;
import static com.greencloud.application.domain.job.JobStatusEnum.PROCESSING;
import static com.greencloud.application.mapper.JobMapper.mapToJobWithNewTime;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.FAILED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.PROCESSING_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.SCHEDULER_JOB_CFP_PROTOCOL;
import static com.greencloud.application.messages.domain.factory.CallForProposalMessageFactory.createCallForProposal;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobAdjustmentMessage;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static java.time.temporal.ChronoUnit.MILLIS;

import java.time.Instant;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.scheduler.SchedulerAgent;
import com.greencloud.application.agents.scheduler.behaviour.jobscheduling.initiator.InitiateCNALookup;
import com.greencloud.commons.job.ClientJob;
import com.gui.agents.SchedulerAgentNode;

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
		if (myScheduler.getAvailableCloudNetworks().isEmpty()) {
			logger.info(NO_AVAILABLE_CNA_LOG);
		} else if (!myScheduler.getJobsToBeExecuted().isEmpty()) {
			final ClientJob jobToExecute = myScheduler.getJobsToBeExecuted().poll();

			if (Objects.nonNull(jobToExecute) && myScheduler.getClientJobs().get(jobToExecute).equals(CREATED)) {
				MDC.put(MDC_JOB_ID, jobToExecute.getJobId());
				final ClientJob adjustedJob = getAdjustedJob(jobToExecute);

				if(Objects.nonNull(adjustedJob)) {
					logger.info(ANNOUNCE_JOB_CNA_LOG, jobToExecute.getJobId());

					final ACLMessage cfp = createCallForProposal(jobToExecute, myScheduler.getAvailableCloudNetworks(),
							SCHEDULER_JOB_CFP_PROTOCOL);
					final ACLMessage clientMessage = prepareJobStatusMessageForClient(
							jobToExecute.getClientIdentifier(),
							PROCESSING_JOB_ID);

					myScheduler.getClientJobs().replace(jobToExecute, CREATED, PROCESSING);
					myScheduler.send(clientMessage);
					myScheduler.manage().updateJobQueue();
					myScheduler.addBehaviour(new InitiateCNALookup(myScheduler, cfp, jobToExecute));
				}
			}
		}
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
			myScheduler.send(prepareJobStatusMessageForClient(job.getClientIdentifier(), FAILED_JOB_ID));
			return null;
		}

		logger.info(JOB_ADJUST_TIME_LOG, job.getJobId());
		final ClientJob adjustedJob = mapToJobWithNewTime(job, newAdjustedStart, newAdjustedEnd);
		myScheduler.manage().swapJobInstances(adjustedJob, job);
		myScheduler.send(prepareJobAdjustmentMessage(job.getClientIdentifier(), adjustedJob));
		return adjustedJob;
	}
}
