package agents.cloudnetwork.behaviour.jobhandling.initiator;

import static agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.CHOSEN_SERVER_FOR_JOB_LOG;
import static agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.INCORRECT_PROPOSAL_FORMAT_LOG;
import static agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.NO_SERVERS_AVAILABLE_RETRIES_LIMIT_LOG;
import static agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.NO_SERVERS_AVAILABLE_RETRY_LOG;
import static agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.NO_SERVER_RESPONSES_LOG;
import static agents.cloudnetwork.domain.CloudNetworkAgentConstants.MAX_POWER_DIFFERENCE;
import static agents.cloudnetwork.domain.CloudNetworkAgentConstants.RETRY_LIMIT;
import static agents.cloudnetwork.domain.CloudNetworkAgentConstants.RETRY_PAUSE_MILLISECONDS;
import static mapper.JsonMapper.getMapper;
import static messages.MessagingUtils.readMessageContent;
import static messages.MessagingUtils.rejectJobOffers;
import static messages.MessagingUtils.retrieveProposals;
import static messages.MessagingUtils.retrieveValidMessages;
import static messages.domain.factory.OfferMessageFactory.makeJobOfferForClient;

import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import agents.cloudnetwork.CloudNetworkAgent;
import agents.cloudnetwork.behaviour.jobhandling.handler.HandleJobRequestRetry;
import domain.ServerData;
import domain.job.ClientJob;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import mapper.JobMapper;
import messages.domain.factory.ReplyMessageFactory;

/**
 * Behaviour initiates lookup for server which will execute the client's job
 */
public class InitiateNewJobExecutorLookup extends ContractNetInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiateNewJobExecutorLookup.class);

	private final ACLMessage originalMessage;
	private final ACLMessage replyMessage;
	private final CloudNetworkAgent myCloudNetworkAgent;
	private final String guid;
	private final String jobId;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent           agent which is executing the behaviour
	 * @param cfp             call for proposal message containing job requriements sent to the servers
	 * @param originalMessage original message received from the client
	 */
	public InitiateNewJobExecutorLookup(final Agent agent, final ACLMessage cfp, final ACLMessage originalMessage,
			String jobId) {
		super(agent, cfp);
		this.myCloudNetworkAgent = (CloudNetworkAgent) myAgent;
		this.guid = agent.getName();
		this.originalMessage = originalMessage;
		this.replyMessage = originalMessage.createReply();
		this.jobId = jobId;
	}

	/**
	 * Method waits for all Server Agent responses.
	 * It chooses the Server Agent for job execution and rejects the remaining ones.
	 *
	 * @param responses   retrieved responses from Server Agents
	 * @param acceptances vector containing accept proposal message sent back to the chosen server (not used)
	 */
	@Override
	protected void handleAllResponses(final Vector responses, final Vector acceptances) {
		final List<ACLMessage> proposals = retrieveProposals(responses);

		if (responses.isEmpty()) {
			logger.info(NO_SERVER_RESPONSES_LOG, guid);
		} else if (proposals.isEmpty()) {
			initiateRetryProcess();
		} else {
			final List<ACLMessage> validProposals = retrieveValidMessages(proposals, ServerData.class);
			if (!validProposals.isEmpty()) {
				final ACLMessage chosenServerOffer = chooseServerToExecuteJob(validProposals);
				final ServerData chosenServerData = readMessageContent(chosenServerOffer, ServerData.class);
				final ClientJob job = myCloudNetworkAgent.manage().getJobById(jobId);

				logger.info(CHOSEN_SERVER_FOR_JOB_LOG, guid, jobId, chosenServerOffer.getSender().getName());

				final ACLMessage reply = chosenServerOffer.createReply();
				final ACLMessage offer = makeJobOfferForClient(chosenServerData,
						myCloudNetworkAgent.manage().getCurrentPowerInUse(), replyMessage);

				myCloudNetworkAgent.getServerForJobMap().put(jobId, chosenServerOffer.getSender());
				myCloudNetworkAgent.addBehaviour(new InitiateMakingNewJobOffer(myCloudNetworkAgent, offer, reply));
				rejectJobOffers(myCloudNetworkAgent, JobMapper.mapToJobInstanceId(job), chosenServerOffer, proposals);
			} else {
				handleInvalidResponses(proposals);
			}
		}
	}

	private void handleInvalidResponses(final List<ACLMessage> proposals) {
		logger.info(INCORRECT_PROPOSAL_FORMAT_LOG, guid);
		final ClientJob job = myCloudNetworkAgent.manage().getJobById(jobId);
		rejectJobOffers(myCloudNetworkAgent, JobMapper.mapToJobInstanceId(job), null, proposals);
		myAgent.send(ReplyMessageFactory.prepareRefuseReply(replyMessage));
	}

	private void initiateRetryProcess() {
		int retries = myCloudNetworkAgent.getJobRequestRetries().get(jobId);

		if (retries >= RETRY_LIMIT) {
			logger.info(NO_SERVERS_AVAILABLE_RETRIES_LIMIT_LOG, guid);
			myCloudNetworkAgent.getJobRequestRetries().remove(jobId);
			myAgent.send(ReplyMessageFactory.prepareRefuseReply(replyMessage));
		} else {
			myCloudNetworkAgent.getJobRequestRetries().put(jobId, ++retries);
			logger.info(NO_SERVERS_AVAILABLE_RETRY_LOG, guid, retries);
			myAgent.addBehaviour(new HandleJobRequestRetry(myCloudNetworkAgent, RETRY_PAUSE_MILLISECONDS,
					originalMessage, jobId));
		}
	}

	private ACLMessage chooseServerToExecuteJob(final List<ACLMessage> serverOffers) {
		return serverOffers.stream().min(this::compareServerOffers).orElseThrow();
	}

	private int compareServerOffers(final ACLMessage serverOffer1, final ACLMessage serverOffer2) {
		ServerData server1;
		ServerData server2;
		try {
			server1 = getMapper().readValue(serverOffer1.getContent(), ServerData.class);
			server2 = getMapper().readValue(serverOffer2.getContent(), ServerData.class);
		} catch (JsonProcessingException e) {
			return Integer.MAX_VALUE;
		}
		int powerDifference = server1.getAvailablePower() - server2.getAvailablePower();
		int priceDifference = (int) (server1.getServicePrice() - server2.getServicePrice());
		return MAX_POWER_DIFFERENCE.isValidIntValue(powerDifference) ? priceDifference : powerDifference;
	}
}
