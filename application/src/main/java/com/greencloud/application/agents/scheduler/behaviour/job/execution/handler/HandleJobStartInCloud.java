package com.greencloud.application.agents.scheduler.behaviour.job.execution.handler;

import static com.greencloud.application.agents.scheduler.behaviour.job.execution.handler.logs.CloudJobExecutionHandlerLog.JOB_CLOUD_START_LOG;
import static com.greencloud.application.agents.scheduler.behaviour.job.execution.handler.logs.CloudJobExecutionHandlerLog.JOB_CLOUD_START_NO_PRESENT_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.handler.logs.JobHandlingHandlerLog.JOB_ALREADY_STARTED_LOG;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.messages.factory.JobStatusMessageFactory.prepareJobStartedInCloudMessage;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.ACCEPTED;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.IN_PROGRESS_CLOUD;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.PLANNED_JOB_STATUSES;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.scheduler.SchedulerAgent;
import com.greencloud.commons.domain.job.ClientJob;

import jade.core.behaviours.WakerBehaviour;

/**
 * Behaviour starts the execution of the job in central cloud
 */
public class HandleJobStartInCloud extends WakerBehaviour {

	private static final Logger logger = getLogger(HandleJobStartInCloud.class);

	private final SchedulerAgent mySchedulerAgent;
	private final ClientJob jobToExecute;

	private HandleJobStartInCloud(final SchedulerAgent agent, final Date startDate, final ClientJob job) {
		super(agent, startDate);

		this.jobToExecute = job;
		this.mySchedulerAgent = agent;
	}

	/**
	 * Method calculates the time after which the job execution will start.
	 * If the provided time is later than the current time then the job execution will start immediately
	 *
	 * @param schedulerAgent agent that will execute the behaviour
	 * @param job            job to execute
	 * @return HandleJobStartInCloud
	 */
	public static HandleJobStartInCloud createFor(final SchedulerAgent schedulerAgent, final ClientJob job) {
		final Instant startDate = getCurrentTime().isAfter(job.getStartTime()) ? getCurrentTime() : job.getStartTime();
		return new HandleJobStartInCloud(schedulerAgent, Date.from(startDate), job);
	}

	/**
	 * Method starts the execution of the job in cloud.
	 * It updates the state of central cloud, then sends the information that the execution has started to the
	 * Client.
	 * Finally, it schedules the behaviour executed upon job execution finish.
	 */
	@Override
	protected void onWake() {
		final String jobId = jobToExecute.getJobId();
		MDC.put(MDC_JOB_ID, jobId);

		if (!mySchedulerAgent.getClientJobs().containsKey(jobToExecute)) {
			logger.info(JOB_CLOUD_START_NO_PRESENT_LOG, mapToJobInstanceId(jobToExecute));
			return;
		}

		if (PLANNED_JOB_STATUSES.contains(mySchedulerAgent.getClientJobs().getOrDefault(jobToExecute, ACCEPTED))) {
			logger.info(JOB_CLOUD_START_LOG, jobId);

			substituteJobStatus();
			mySchedulerAgent.getGuiController().updateActiveJobsCountByValue(1);
			myAgent.send(prepareJobStartedInCloudMessage(jobToExecute));
			myAgent.addBehaviour(HandleJobFinishInCloud.createFor(mySchedulerAgent, jobToExecute));
		} else {
			logger.info(JOB_ALREADY_STARTED_LOG, jobId);
		}
	}

	private void substituteJobStatus() {
		mySchedulerAgent.getClientJobs().replace(jobToExecute, IN_PROGRESS_CLOUD);
		mySchedulerAgent.getJobsExecutedInCloud().add(jobToExecute.getJobId());
		mySchedulerAgent.manage().updateGUI();
	}
}
