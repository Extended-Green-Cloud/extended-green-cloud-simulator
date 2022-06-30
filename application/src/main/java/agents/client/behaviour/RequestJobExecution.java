package agents.client.behaviour;

import static agents.client.ClientAgentConstants.CLOUD_NETWORK_AGENTS;
import static common.GUIUtils.announceNewClient;
import static common.constant.MessageProtocolConstants.CLIENT_JOB_CFP_PROTOCOL;
import static jade.lang.acl.ACLMessage.ACCEPT_PROPOSAL;
import static mapper.JsonMapper.getMapper;
import static messages.MessagingUtils.rejectJobOffers;
import static messages.MessagingUtils.retrieveProposals;

import agents.client.ClientAgent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gui.domain.nodes.ClientAgentNode;
import com.gui.domain.types.JobStatusEnum;
import domain.job.Job;
import domain.job.PricedJob;
import exception.IncorrectCloudNetworkOfferException;
import exception.IncorrectServerOfferException;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import messages.domain.CallForProposalMessageFactory;
import messages.domain.ReplyMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Vector;

/**
 * Behaviour responsible for sending and handling job's call for proposal
 */
public class RequestJobExecution extends ContractNetInitiator {

    private static final Logger logger = LoggerFactory.getLogger(RequestJobExecution.class);

    private final Job job;
    private final ClientAgent myClientAgent;
    private final String guid;

    /**
     * Behaviour constructor.
     *
     * @param agent agent executing the behaviour
     * @param cfp   call for proposal message containing job details that will be sent to Cloud Network Agents
     * @param job   the job that the client want to be executed
     */
    public RequestJobExecution(final Agent agent, final ACLMessage cfp, final Job job) {
        super(agent, cfp);
        this.myClientAgent = (ClientAgent) agent;
        this.guid = agent.getName();
        this.job = job;
    }

    /**
     * Method which prepares the call for proposal message.
     *
     * @param callForProposal default call for proposal message
     * @return vector containing the call for proposals with job characteristics sent to the Cloud Network Agents
     */
    @Override
    protected Vector prepareCfps(final ACLMessage callForProposal) {
        logger.info("[{}] Sending call for proposal to Cloud Network Agents", guid);
        final Vector<ACLMessage> vector = new Vector<>();
        final List<AID> cloudNetworks = (List<AID>) getParent().getDataStore().get(CLOUD_NETWORK_AGENTS);
        vector.add(CallForProposalMessageFactory.createCallForProposal(job, cloudNetworks, CLIENT_JOB_CFP_PROTOCOL));
        return vector;
    }

    /**
     * Method handles the responses retrieved from the Cloud Network Agents. It is responsible for analyzing the
     * retrieved responses, choosing one Cloud Network Agent that will execute the job and rejecting the remaining ones.
     *
     * @param responses   all retrieved Cloud Network Agents' responses
     * @param acceptances vector containing accept proposal message that will be sent back to the chosen
     *                    Cloud Network Agent
     */
    @Override
    protected void handleAllResponses(final Vector responses, final Vector acceptances) {
        final List<ACLMessage> proposals = retrieveProposals(responses);

        if (responses.isEmpty()) {
            logger.info("[{}] No responses were retrieved", guid);
            myAgent.doDelete();
        } else if (proposals.isEmpty()) {
            logger.info("[{}] All Cloud Network Agents refused to the call for proposal", guid);
            myClientAgent.getGuiController().updateClientsCountByValue(-1);
            ((ClientAgentNode) myClientAgent.getAgentNode()).updateJobStatus(JobStatusEnum.REJECTED);
        } else {
            final ACLMessage chosenOffer = chooseCNAToExecuteJob(proposals);
            logger.info("[{}] Sending ACCEPT_PROPOSAL to {}", guid, chosenOffer.getSender().getName());
            myClientAgent.setChosenCloudNetworkAgent(chosenOffer.getSender());
            PricedJob pricedJob;
            try {
                pricedJob = getMapper().readValue(chosenOffer.getContent(), PricedJob.class);
            } catch (JsonProcessingException e) {
                throw new IncorrectServerOfferException();
            }
            acceptances.add(ReplyMessageFactory.prepareStringReply(chosenOffer.createReply(), pricedJob.getJobId(), ACCEPT_PROPOSAL));
            rejectJobOffers(myClientAgent, pricedJob.getJobId(), chosenOffer, proposals);
        }
    }

    /**
     * Method that handles the information sent by Cloud Network Agent implying that the job execution has started.
     *
     * @param inform retrieved inform message
     */
    @Override
    protected void handleInform(final ACLMessage inform) {
        logger.info("[{}] The execution of my job started!", myAgent);
        ((ClientAgentNode) myClientAgent.getAgentNode()).updateJobStatus(JobStatusEnum.IN_PROGRESS);
    }

    private ACLMessage chooseCNAToExecuteJob(final List<ACLMessage> receivedOffers) {
        final Comparator<ACLMessage> compareCNA = Comparator.comparingDouble(offer -> {
            try {
                return getMapper().readValue(offer.getContent(), PricedJob.class).getPriceForJob();
            } catch (JsonProcessingException e) {
                throw new IncorrectCloudNetworkOfferException();
            }
        });
        return receivedOffers.stream().min(compareCNA).orElseThrow();
    }
}
