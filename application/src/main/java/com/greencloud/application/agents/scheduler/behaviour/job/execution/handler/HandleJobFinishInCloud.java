package com.greencloud.application.agents.scheduler.behaviour.job.execution.handler;

import static com.greencloud.application.agents.scheduler.behaviour.job.execution.handler.logs.CloudJobExecutionHandlerLog.JOB_CLOUD_FINISH_LOG;
import static com.greencloud.application.messages.factory.JobStatusMessageFactory.prepareJobFinishInCloudMessage;
import static com.greencloud.application.utils.GuiUtils.announceFinishedJob;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.ACCEPTED_JOB_STATUSES;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.scheduler.SchedulerAgent;
import com.greencloud.commons.domain.job.ClientJob;

import jade.core.behaviours.WakerBehaviour;

/**
 * Behaviour finish the execution of the job in cloud
 */
public class HandleJobFinishInCloud extends WakerBehaviour {

	private static final Logger logger = getLogger(HandleJobFinishInCloud.class);

	private final ClientJob jobToExecute;
	private final SchedulerAgent mySchedulerAgent;

	private HandleJobFinishInCloud(final SchedulerAgent agent, final Date endTime, final ClientJob job) {
		super(agent, endTime);

		this.jobToExecute = job;
		this.mySchedulerAgent = agent;
	}

	/**
	 * Method calculates the time after which the job execution should finish in cloud.
	 *
	 * @param schedulerAgent agent that will execute the behaviour
	 * @param job            job which execution is to be terminated
	 * @return HandleJobFinishInCloud
	 */
	public static HandleJobFinishInCloud createFor(final SchedulerAgent schedulerAgent, final ClientJob job) {
		final Instant endTime = getCurrentTime().isAfter(job.getEndTime()) ? getCurrentTime() : job.getEndTime();
		return new HandleJobFinishInCloud(schedulerAgent, Date.from(endTime), job);
	}

	/**
	 * Method performs action that finished job execution in cloud.
	 * It sends the messages to the Client informing that the job execution has finished
	 * as well as it updates the central cloud's state
	 */
	@Override
	protected void onWake() {
		MDC.put(MDC_JOB_ID, jobToExecute.getJobId());
		if (mySchedulerAgent.getClientJobs().containsKey(jobToExecute) &&
				ACCEPTED_JOB_STATUSES.contains(mySchedulerAgent.getClientJobs().get(jobToExecute))) {
			logger.info(JOB_CLOUD_FINISH_LOG, jobToExecute.getJobId(), jobToExecute.getEndTime());

			mySchedulerAgent.send(prepareJobFinishInCloudMessage(jobToExecute));
			mySchedulerAgent.manage().handleJobCleanUp(jobToExecute, false);
			announceFinishedJob(mySchedulerAgent);
			mySchedulerAgent.manage().updateGUI();
		}
	}
}
