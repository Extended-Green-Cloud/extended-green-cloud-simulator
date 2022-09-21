package com.greencloud.application.agents.server.behaviour.jobexecution.listener;

import static com.greencloud.application.agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog.JOB_START_STATUS_RECEIVED_REQUEST_LOG;
import static com.greencloud.application.agents.server.behaviour.jobexecution.listener.templates.JobHandlingMessageTemplates.JOB_STATUS_REQUEST_TEMPLATE;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static jade.lang.acl.ACLMessage.AGREE;
import static jade.lang.acl.ACLMessage.FAILURE;
import static jade.lang.acl.ACLMessage.INFORM;

import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.domain.job.Job;
import com.greencloud.application.domain.job.JobStatusEnum;
import com.greencloud.application.messages.domain.factory.ReplyMessageFactory;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour handles request coming from Cloud Network Agent asking for the job start status
 */
public class ListenForJobStartCheckRequest extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForJobStartCheckRequest.class);

	private ServerAgent myServerAgent;

	/**
	 * Method casts the abstract agent to agent of type Server Agent
	 */
	@Override
	public void onStart() {
		super.onStart();
		this.myServerAgent = (ServerAgent) myAgent;
	}

	/**
	 * Method listens for the request coming from Cloud Network and responds with either AGREE when the job has started,
	 * or REFUSE if it has not started yet
	 **/
	@Override
	public void action() {
		final ACLMessage request = myAgent.receive(JOB_STATUS_REQUEST_TEMPLATE);

		if (Objects.nonNull(request)) {
			final String jobId = request.getContent();
			myServerAgent.send(
					ReplyMessageFactory.prepareStringReply(request.createReply(), "REQUEST PROCESSING", AGREE));
			MDC.put(MDC_JOB_ID, jobId);
			logger.info(JOB_START_STATUS_RECEIVED_REQUEST_LOG, jobId);
			final Map.Entry<Job, JobStatusEnum> jobInstance = myServerAgent.manage().getCurrentJobInstance(jobId);
			myServerAgent.send(createReplyWithJobStatus(request, jobInstance));
		} else {
			block();
		}
	}

	private ACLMessage createReplyWithJobStatus(final ACLMessage message,
			final Map.Entry<Job, JobStatusEnum> jobInstance) {
		return Objects.nonNull(jobInstance) && JobStatusEnum.RUNNING_JOB_STATUSES.contains(jobInstance.getValue()) ?
				ReplyMessageFactory.prepareStringReply(message.createReply(), "JOB STARTED", INFORM) :
				ReplyMessageFactory.prepareStringReply(message.createReply(), "JOB HAS NOT STARTED", FAILURE);
	}
}
