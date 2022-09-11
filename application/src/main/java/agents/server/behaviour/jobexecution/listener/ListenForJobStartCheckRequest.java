package agents.server.behaviour.jobexecution.listener;

import static agents.server.behaviour.jobexecution.listener.logs.JobHandlingListenerLog.JOB_START_STATUS_RECEIVED_REQUEST_LOG;
import static agents.server.behaviour.jobexecution.listener.templates.JobHandlingMessageTemplates.JOB_STATUS_REQUEST_TEMPLATE;
import static domain.job.JobStatusEnum.RUNNING_JOB_STATUSES;
import static jade.lang.acl.ACLMessage.AGREE;
import static jade.lang.acl.ACLMessage.FAILURE;
import static jade.lang.acl.ACLMessage.INFORM;
import static messages.domain.factory.ReplyMessageFactory.prepareStringReply;

import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agents.server.ServerAgent;
import domain.job.ClientJob;
import domain.job.JobStatusEnum;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour handles request coming from Cloud Network Agent asking for the job start status
 */
public class ListenForJobStartCheckRequest extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForJobStartCheckRequest.class);

	private ServerAgent myServerAgent;
	private String guid;

	/**
	 * Method casts the abstract agent to agent of type Server Agent
	 */
	@Override
	public void onStart() {
		super.onStart();
		this.myServerAgent = (ServerAgent) myAgent;
		this.guid = myServerAgent.getName();
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
			myServerAgent.send(prepareStringReply(request.createReply(), "REQUEST PROCESSING", AGREE));
			logger.info(JOB_START_STATUS_RECEIVED_REQUEST_LOG, guid, jobId);
			final Map.Entry<ClientJob, JobStatusEnum> jobInstance = myServerAgent.manage().getCurrentJobInstance(jobId);
			myServerAgent.send(createReplyWithJobStatus(request, jobInstance));
		} else {
			block();
		}
	}

	private ACLMessage createReplyWithJobStatus(final ACLMessage message,
			final Map.Entry<ClientJob, JobStatusEnum> jobInstance) {
		return Objects.nonNull(jobInstance) && RUNNING_JOB_STATUSES.contains(jobInstance.getValue()) ?
				prepareStringReply(message.createReply(), "JOB STARTED", INFORM) :
				prepareStringReply(message.createReply(), "JOB HAS NOT STARTED", FAILURE);
	}
}
