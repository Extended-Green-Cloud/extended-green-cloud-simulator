package agents.cloudnetwork.behaviour;

import static agents.cloudnetwork.domain.CloudNetworkAgentConstants.MAX_POWER_DIFFERENCE;
import static agents.cloudnetwork.domain.CloudNetworkAgentConstants.RETRY_LIMIT;
import static agents.cloudnetwork.domain.CloudNetworkAgentConstants.RETRY_PAUSE_MILLISECONDS;
import static mapper.JsonMapper.getMapper;
import static messages.MessagingUtils.readMessageContent;
import static messages.MessagingUtils.rejectJobOffers;
import static messages.MessagingUtils.retrieveProposals;
import static messages.MessagingUtils.retrieveValidMessages;
import static messages.domain.factory.JobOfferMessageFactory.makeJobOfferForClient;
import static utils.JobMapUtils.getJobById;

import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import agents.cloudnetwork.CloudNetworkAgent;
import common.mapper.JobMapper;
import domain.ServerData;
import domain.job.Job;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import messages.domain.factory.ReplyMessageFactory;

/**
 * Behaviour which is responsible for broadcasting client's job to servers and choosing server to execute the job
 */
public class AnnounceNewJobRequest extends ContractNetInitiator {

	private static final Logger logger = LoggerFactory.getLogger(AnnounceNewJobRequest.class);

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
	public AnnounceNewJobRequest(final Agent agent, final ACLMessage cfp, final ACLMessage originalMessage,
			String jobId) {
		super(agent, cfp);
		this.myCloudNetworkAgent = (CloudNetworkAgent) myAgent;
		this.guid = agent.getName();
		this.originalMessage = originalMessage;
		this.replyMessage = originalMessage.createReply();
		this.jobId = jobId;
	}

	/**
	 * Method which waits for all Server Agent responses. It is responsible for analyzing the received proposals,
	 * choosing the Server Agent for job execution and rejecting the remaining Server Agents.
	 *
	 * @param responses   retrieved responses from Server Agents
	 * @param acceptances vector containing accept proposal message sent back to the chosen server (not used)
	 */
	@Override
	protected void handleAllResponses(final Vector responses, final Vector acceptances) {
		final List<ACLMessage> proposals = retrieveProposals(responses);

		if (responses.isEmpty()) {
			logger.info("[{}] No responses were retrieved", guid);
		} else if (proposals.isEmpty()) {
			var retries = myCloudNetworkAgent.getJobRequestRetries().get(jobId);
			if (retries >= RETRY_LIMIT) {
				logger.info("[{}] No Servers available - reached limit of retires - sending refuse message to client",
						guid);
				myCloudNetworkAgent.getJobRequestRetries().remove(jobId);
				myAgent.send(ReplyMessageFactory.prepareRefuseReply(replyMessage));
			} else {
				myCloudNetworkAgent.getJobRequestRetries().put(jobId, ++retries);
				logger.info("[{}] No Servers available - schedule {} retry to process the message", guid, retries);
				myAgent.addBehaviour(new ScheduleReceiveJobRequestRetry(myCloudNetworkAgent, RETRY_PAUSE_MILLISECONDS,
						originalMessage,
						jobId));
			}
		} else {
			final List<ACLMessage> validProposals = retrieveValidMessages(proposals, ServerData.class);
			if (!validProposals.isEmpty()) {
				final ACLMessage chosenServerOffer = chooseServerToExecuteJob(validProposals);
				final ServerData chosenServerData = readMessageContent(chosenServerOffer, ServerData.class);
				final Job job = getJobById(myCloudNetworkAgent.getNetworkJobs(), jobId);
				logger.info("[{}] Chosen Server for the job {}: {}", guid, jobId,
						chosenServerOffer.getSender().getName());
				final ACLMessage serverReplyMessage = chosenServerOffer.createReply();
				logger.info("[{}] Sending job execution offer to Client", guid);
				myCloudNetworkAgent.getServerForJobMap()
						.put(chosenServerData.getJobId(), chosenServerOffer.getSender());
				myCloudNetworkAgent.addBehaviour(new ProposeJobOffer(myCloudNetworkAgent,
						makeJobOfferForClient(chosenServerData,
								myCloudNetworkAgent.manage().getCurrentPowerInUse(),
								replyMessage), serverReplyMessage));
				rejectJobOffers(myCloudNetworkAgent, JobMapper.mapToJobInstanceId(job), chosenServerOffer, proposals);
			} else {
				handleInvalidResponses(proposals);
			}
		}
	}

	private void handleInvalidResponses(final List<ACLMessage> proposals) {
		logger.info("[{}] I didn't understand any proposal from Server Agents", guid);
		final Job job = getJobById(myCloudNetworkAgent.getNetworkJobs(), jobId);
		rejectJobOffers(myCloudNetworkAgent, JobMapper.mapToJobInstanceId(job), null, proposals);
		myAgent.send(ReplyMessageFactory.prepareRefuseReply(replyMessage));
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
