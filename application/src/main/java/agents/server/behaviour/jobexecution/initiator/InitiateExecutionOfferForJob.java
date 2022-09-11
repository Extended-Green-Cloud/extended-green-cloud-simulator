package agents.server.behaviour.jobexecution.initiator;

import static agents.server.behaviour.jobexecution.initiator.logs.JobHandlingInitiatorLog.SERVER_OFFER_ACCEPT_PROPOSAL_GS_LOG;
import static agents.server.behaviour.jobexecution.initiator.logs.JobHandlingInitiatorLog.SERVER_OFFER_REJECT_LOG;
import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;
import static messages.MessagingUtils.readMessageContent;
import static messages.domain.factory.ReplyMessageFactory.prepareAcceptReplyWithProtocol;
import static utils.GUIUtils.displayMessageArrow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agents.server.ServerAgent;
import domain.job.ClientJob;
import domain.job.JobInstanceIdentifier;
import domain.job.JobStatusEnum;
import domain.job.JobWithProtocol;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ProposeInitiator;
import messages.domain.factory.ReplyMessageFactory;

/**
 * Behaviours sends job execution offer to Cloud Network Agent and handles received responses
 */
public class InitiateExecutionOfferForJob extends ProposeInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiateExecutionOfferForJob.class);

	private final ServerAgent myServerAgent;
	private final String guid;
	private final ACLMessage replyMessage;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent        agent executing the behaviour
	 * @param msg          proposal message that has to be sent to the Cloud Network
	 * @param replyMessage reply message sent to chosen Green Source after receiving the Cloud Network
	 *                     response
	 */
	public InitiateExecutionOfferForJob(final Agent agent, final ACLMessage msg, final ACLMessage replyMessage) {
		super(agent, msg);
		this.replyMessage = replyMessage;
		this.myServerAgent = (ServerAgent) myAgent;
		this.guid = myServerAgent.getName();
	}

	/**
	 * Method handles ACCEPT_PROPOSAL response received from the Cloud Network Agents.
	 * It schedules the job execution, updates the network data and responds with ACCEPT_PROPOSAL to the
	 * chosen Green Source Agent
	 *
	 * @param accept_proposal accept proposal message retrieved from the Cloud Network
	 */
	@Override
	protected void handleAcceptProposal(final ACLMessage accept_proposal) {
		logger.info(SERVER_OFFER_ACCEPT_PROPOSAL_GS_LOG, guid);
		final JobWithProtocol jobWithProtocol = readMessageContent(accept_proposal, JobWithProtocol.class);
		final JobInstanceIdentifier jobInstanceId = jobWithProtocol.getJobInstanceIdentifier();
		myServerAgent.getServerJobs()
				.replace(myServerAgent.manage().getJobByIdAndStartDate(jobInstanceId), JobStatusEnum.ACCEPTED);
		myServerAgent.manage().updateClientNumberGUI();
		displayMessageArrow(myServerAgent, replyMessage.getAllReceiver());
		myAgent.send(prepareAcceptReplyWithProtocol(replyMessage, jobInstanceId, jobWithProtocol.getReplyProtocol()));
	}

	/**
	 * Method handles REJECT_PROPOSAL response received from the Cloud Network Agents.
	 * It forwards the REJECT_PROPOSAL to the Green Source Agent
	 *
	 * @param reject_proposal reject proposal message retrieved from the Cloud Network
	 */
	@Override
	protected void handleRejectProposal(final ACLMessage reject_proposal) {
		logger.info(SERVER_OFFER_REJECT_LOG, guid, reject_proposal.getSender().getLocalName());
		final JobInstanceIdentifier jobInstanceId = readMessageContent(reject_proposal, JobInstanceIdentifier.class);
		final ClientJob job = myServerAgent.manage().getJobByIdAndStartDate(jobInstanceId);
		myServerAgent.getGreenSourceForJobMap().remove(jobInstanceId.getJobId());
		myServerAgent.getServerJobs().remove(job);
		displayMessageArrow(myServerAgent, replyMessage.getAllReceiver());
		myServerAgent.send(ReplyMessageFactory.prepareReply(replyMessage, jobInstanceId, REJECT_PROPOSAL));
	}
}
