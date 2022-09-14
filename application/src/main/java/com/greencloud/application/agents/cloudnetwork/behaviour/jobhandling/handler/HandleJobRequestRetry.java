package com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.handler;

import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.handler.logs.JobHandlingHandlerLog.TRIGGER_RETRY_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour retries the process of looking for available server for the job execution
 */
public class HandleJobRequestRetry extends WakerBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(HandleJobRequestRetry.class);

	private final ACLMessage originalMessage;
	private final String jobId;
	private final String guid;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent           agent executing the behaviour
	 * @param timeout         time after which the behaviour execution should begin
	 * @param originalMessage message that is to be resent
	 * @param jobId           id of the job for which the retry process is to be triggered
	 */
	public HandleJobRequestRetry(Agent agent, long timeout, ACLMessage originalMessage, String jobId) {
		super(agent, timeout);
		this.originalMessage = originalMessage;
		this.jobId = jobId;
		this.guid = agent.getName();
	}

	/**
	 * Method resents the client's original new job CFP message
	 */
	@Override
	protected void onWake() {
		myAgent.send(originalMessage);
		MDC.put(MDC_JOB_ID, jobId);
		logger.info(TRIGGER_RETRY_LOG, guid, jobId);
	}
}
