package agents.cloudnetwork.behaviour;

import static agents.cloudnetwork.domain.CloudNetworkAgentConstants.MAX_ERROR_IN_JOB_START;
import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;
import static messages.domain.constants.MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL;
import static utils.GUIUtils.displayMessageArrow;
import static utils.TimeUtils.getCurrentTime;
import static utils.JobMapUtils.getJobById;

import java.time.Instant;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agents.cloudnetwork.CloudNetworkAgent;
import agents.cloudnetwork.behaviour.jobstatus.ReturnJobDelay;
import common.mapper.JobMapper;
import domain.job.Job;
import domain.job.JobStatusEnum;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ProposeInitiator;
import messages.domain.factory.ReplyMessageFactory;

/**
 * Behaviour responsible for sending proposal with job execution offer to the client
 */
public class ProposeJobOffer extends ProposeInitiator {

	private static final Logger logger = LoggerFactory.getLogger(ProposeJobOffer.class);

	private final ACLMessage replyMessage;
	private final CloudNetworkAgent myCloudNetworkAgent;
	private final String guid;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent        agent which is executing the behaviour
	 * @param msg          proposal message with job execution price that will be sent to the client
	 * @param replyMessage reply message sent to server with ACCEPT/REJECT proposal
	 */
	public ProposeJobOffer(final Agent agent, final ACLMessage msg, final ACLMessage replyMessage) {
		super(agent, msg);
		this.myCloudNetworkAgent = (CloudNetworkAgent) myAgent;
		this.guid = agent.getName();
		this.replyMessage = replyMessage;
	}

	/**
	 * Method handles accept proposal message retrieved from the Client Agent. It sends accept proposal to the
	 * chosen for job execution Server Agent and updates the network state.
	 *
	 * @param accept_proposal received accept proposal message
	 */
	@Override
	protected void handleAcceptProposal(final ACLMessage accept_proposal) {
		logger.info("[{}] Sending ACCEPT_PROPOSAL to Server Agent", guid);
		final String jobId = accept_proposal.getContent();
		final Job job = getJobById(myCloudNetworkAgent.getNetworkJobs(), jobId);
		myCloudNetworkAgent.getNetworkJobs().replace(job, JobStatusEnum.ACCEPTED);
		myAgent.addBehaviour(new ReturnJobDelay(myCloudNetworkAgent, calculateExpectedJobStart(job), job.getJobId()));
		displayMessageArrow(myCloudNetworkAgent, replyMessage.getAllReceiver());
		myAgent.send(ReplyMessageFactory.prepareAcceptReplyWithProtocol(replyMessage, JobMapper.mapToJobInstanceId(job),
				SERVER_JOB_CFP_PROTOCOL));
	}

	/**
	 * Method handles reject proposal message retrieved from the Client Agent. It sends reject proposal to the
	 * Server Agent previously chosen for the job execution.
	 *
	 * @param reject_proposal received reject proposal message
	 */
	@Override
	protected void handleRejectProposal(final ACLMessage reject_proposal) {
		logger.info("[{}] Client {} rejected the job proposal", guid, reject_proposal.getSender().getName());
		final String jobId = reject_proposal.getContent();
		final Job job = getJobById(myCloudNetworkAgent.getNetworkJobs(), jobId);
		myCloudNetworkAgent.getServerForJobMap().remove(jobId);
		myCloudNetworkAgent.getNetworkJobs().remove(getJobById(myCloudNetworkAgent.getNetworkJobs(), jobId));
		displayMessageArrow(myCloudNetworkAgent, replyMessage.getAllReceiver());
		myCloudNetworkAgent.send(
				ReplyMessageFactory.prepareReply(replyMessage, JobMapper.mapToJobInstanceId(job), REJECT_PROPOSAL));
	}

	private Date calculateExpectedJobStart(final Job job) {
		final Instant startTime = getCurrentTime().isAfter(job.getStartTime()) ?
				getCurrentTime() :
				job.getStartTime();
		return Date.from(startTime.plusSeconds(MAX_ERROR_IN_JOB_START));
	}
}
