package com.greencloud.application.agents.server.behaviour.jobexecution.handler;

import static com.greencloud.application.agents.server.behaviour.jobexecution.handler.logs.JobHandlingHandlerLog.JOB_FINISH_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.ACCEPTED_JOB_STATUSES;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.commons.job.ClientJob;

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;

/**
 * Behaviour handles job execution finish
 */
public class HandleJobFinish extends WakerBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(HandleJobFinish.class);

	private final ClientJob jobToExecute;
	private final ServerAgent myServerAgent;
	private final boolean informCNA;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent     agent that is executing the behaviour
	 * @param endTime   time when the behaviour should be executed
	 * @param job       job which execution is to be finished
	 * @param informCNA flag indicating whether cloud network should be informed about the job finish
	 */
	private HandleJobFinish(Agent agent, Date endTime, final ClientJob job, final boolean informCNA) {
		super(agent, endTime);
		this.jobToExecute = job;
		this.myServerAgent = (ServerAgent) agent;
		this.informCNA = informCNA;
	}

	/**
	 * Method calculates the time after which the job execution should finish.
	 *
	 * @param serverAgent agent that will execute the behaviour
	 * @param jobToFinish job which execution is to be terminated
	 * @param informCNA   flag indicating whether cloud network should be informed about the job finish
	 * @return behaviour to be run
	 */
	public static HandleJobFinish createFor(final ServerAgent serverAgent, final ClientJob jobToFinish,
			final boolean informCNA) {
		final long jobDuration = Duration.between(jobToFinish.getStartTime(), jobToFinish.getEndTime()).toMillis();
		final Instant expectedEndTime = getCurrentTime().plus(jobDuration, ChronoUnit.MILLIS);

		final Instant endTime = getCurrentTime().isAfter(jobToFinish.getDeadline()) ?
				getCurrentTime() :
				(expectedEndTime.isAfter(jobToFinish.getDeadline()) ? jobToFinish.getDeadline() : expectedEndTime);

		return new HandleJobFinish(serverAgent, Date.from(endTime), jobToFinish, informCNA);
	}

	/**
	 * Method performs postpone actions executed upon job finish.
	 * It sends the messages to the Green Source Agent and the Cloud Network Agent
	 * informing that the job execution has finished as well as it updates the server's state
	 */
	@Override
	protected void onWake() {
		MDC.put(MDC_JOB_ID, jobToExecute.getJobId());
		if (Objects.nonNull(myServerAgent.getServerJobs().get(jobToExecute)) && ACCEPTED_JOB_STATUSES.contains(
				myServerAgent.getServerJobs().get(jobToExecute))) {
			logger.info(JOB_FINISH_LOG, jobToExecute.getJobId(), jobToExecute.getEndTime());
			myServerAgent.manage().finishJobExecution(jobToExecute, informCNA);
		}
	}
}
