package com.greencloud.application.agents.server.behaviour.jobexecution.handler;

import static com.greencloud.application.agents.server.behaviour.jobexecution.handler.logs.JobHandlingHandlerLog.JOB_START_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.handler.logs.JobHandlingHandlerLog.JOB_START_NO_GREEN_SOURCE_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.handler.logs.JobHandlingHandlerLog.JOB_START_NO_INFORM_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.handler.logs.JobHandlingHandlerLog.JOB_START_NO_PRESENT_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobStartedMessage;
import static com.greencloud.application.utils.GUIUtils.displayMessageArrow;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.domain.job.ClientJob;
import com.greencloud.application.domain.job.JobStatusEnum;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour handles job execution start
 */
public class HandleJobStart extends WakerBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(HandleJobStart.class);

	private final ServerAgent myServerAgent;
	private final ClientJob jobToExecute;
	private final boolean informCNAStart;
	private final boolean informCNAFinish;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent           agent that is executing the behaviour
	 * @param startDate       time when the job execution should begin
	 * @param job             job which execution should start
	 * @param informCNAStart  flag indicating whether the cloud network should be informed about job start
	 * @param informCNAFinish flag indicating whether the cloud network should be informed about job finish
	 */
	private HandleJobStart(Agent agent, Date startDate,
			final ClientJob job,
			final boolean informCNAStart,
			final boolean informCNAFinish) {
		super(agent, startDate);
		this.jobToExecute = job;
		this.myServerAgent = (ServerAgent) agent;
		this.informCNAStart = informCNAStart;
		this.informCNAFinish = informCNAFinish;
	}

	/**
	 * Method calculates the time after which the job execution will start.
	 * If the provided time is later than the current time then the job execution will start immediately
	 *
	 * @param serverAgent     agent that will execute the behaviour
	 * @param job             job to execute
	 * @param informCNAStart  flag indicating whether the cloud network should be informed about job start
	 * @param informCNAFinish flag indicating whether the cloud network should be informed about job finish
	 * @return behaviour to be run
	 */
	public static HandleJobStart createFor(final ServerAgent serverAgent,
			final ClientJob job,
			final boolean informCNAStart,
			final boolean informCNAFinish) {
		final Instant startDate = getCurrentTime().isAfter(job.getStartTime()) ? getCurrentTime() : job.getStartTime();
		return new HandleJobStart(serverAgent, Date.from(startDate), job, informCNAStart,
				informCNAFinish);
	}

	/**
	 * Method starts the execution of the job.
	 * It updates the server state, then sends the information that the execution has started to the Green Source Agent and the Cloud Network.
	 * Finally, it schedules the behaviour executed upon job execution finish.
	 */
	@Override
	protected void onWake() {
		final String jobId = jobToExecute.getJobId();
		MDC.put(MDC_JOB_ID, jobId);

		if (!myServerAgent.getGreenSourceForJobMap().containsKey(jobId)) {
			logger.info(JOB_START_NO_GREEN_SOURCE_LOG, jobId);
		} else if (!myServerAgent.getServerJobs().containsKey(jobToExecute)) {
			logger.info(JOB_START_NO_PRESENT_LOG, jobId);
		} else {
			final String logMessage = informCNAStart ? JOB_START_LOG : JOB_START_NO_INFORM_LOG;
			logger.info(logMessage, jobId);

			myServerAgent.getServerJobs().replace(jobToExecute, JobStatusEnum.ACCEPTED, JobStatusEnum.IN_PROGRESS);
			sendJobStartMessage(jobId);
			myServerAgent.manage().incrementStartedJobs(jobId);
			myAgent.addBehaviour(HandleJobFinish.createFor(myServerAgent, jobToExecute, informCNAFinish));
		}
	}

	private void sendJobStartMessage(final String jobId) {
		final List<AID> receivers = informCNAStart
				? List.of(myServerAgent.getGreenSourceForJobMap().get(jobId),
				myServerAgent.getOwnerCloudNetworkAgent())
				: Collections.singletonList(myServerAgent.getGreenSourceForJobMap().get(jobToExecute.getJobId()));
		final ACLMessage startedJobMessage = prepareJobStartedMessage(jobId, jobToExecute.getStartTime(), receivers);

		displayMessageArrow(myServerAgent, receivers);
		myAgent.send(startedJobMessage);
	}
}
