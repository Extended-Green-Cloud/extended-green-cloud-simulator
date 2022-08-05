package agents.client.behaviour;

import static agents.client.ClientAgentConstants.CLOUD_NETWORK_AGENTS;
import static agents.client.ClientAgentConstants.MAX_RETRIES;
import static agents.client.ClientAgentConstants.MAX_TRAFFIC_DIFFERENCE;
import static agents.client.ClientAgentConstants.RETRY_PAUSE_MILLISECONDS;
import static common.constant.MessageProtocolConstants.CLIENT_JOB_CFP_PROTOCOL;
import static jade.lang.acl.ACLMessage.ACCEPT_PROPOSAL;
import static mapper.JsonMapper.getMapper;
import static messages.MessagingUtils.rejectJobOffers;
import static messages.MessagingUtils.retrieveProposals;

import agents.client.ClientAgent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gui.agents.ClientAgentNode;
import com.gui.agents.domain.JobStatusEnum;
import common.constant.InvalidJobIdConstant;
import domain.job.Job;
import domain.job.PricedJob;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import java.util.List;
import java.util.Vector;
import java.util.function.Predicate;
import messages.domain.CallForProposalMessageFactory;
import messages.domain.ReplyMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Behaviour responsible for sending and handling job's call for proposal
 */
public class RequestJobExecution extends ContractNetInitiator {

    private static final Logger logger = LoggerFactory.getLogger(RequestJobExecution.class);

    private final transient Job job;
    private final ClientAgent myClientAgent;
    private final String guid;
    private final transient Predicate<ACLMessage> isValidProposal;

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
        this.isValidProposal = message -> {
            try {
                getMapper().readValue(message.getContent(), PricedJob.class);
                return true;
            } catch (JsonProcessingException e) {
                return false;
            }
        };
    }

    /**
     * Method which prepares the call for proposal message.
     *
     * @param callForProposal default call for proposal message
     * @return vector containing the call for proposals with job characteristics sent to the Cloud Network Agents
     */
    @Override
    protected Vector prepareCfps(final ACLMessage callForProposal) {
        logger.info("[{}] Sending call for proposal to Cloud Network Agents with job request for a jobId {}", guid,
            job.getJobId());
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
        } else if (proposals.isEmpty() && myClientAgent.getRetries() < MAX_RETRIES) {
            logger.info("[{}] All Cloud Network Agents refused to the call for proposal - will retry for {} time",
                guid, myClientAgent.getRetries());
            myClientAgent.retry();
            myClientAgent.addBehaviour(new ScheduleJobRequestRetry(myAgent, RETRY_PAUSE_MILLISECONDS, job));
        } else if (proposals.isEmpty() && myClientAgent.getRetries() >= MAX_RETRIES){
            logger.info("[{}] All Cloud Network Agents refused to the call for proposal", guid);
            myClientAgent.getGuiController().updateClientsCountByValue(-1);
            ((ClientAgentNode) myClientAgent.getAgentNode()).updateJobStatus(JobStatusEnum.REJECTED);
        } else {
            List<ACLMessage> validProposals = proposals.stream().filter(isValidProposal).toList();

            if (validProposals.isEmpty()) {
                rejectJobOffers(myClientAgent, InvalidJobIdConstant.INVALID_JOB_ID, null, proposals);
                logger.info("[{}] I didn't understand any proposal from Cloud Network Agents", guid);
                myClientAgent.getGuiController().updateClientsCountByValue(-1);
                ((ClientAgentNode) myClientAgent.getAgentNode()).updateJobStatus(JobStatusEnum.REJECTED);
                return;
            }

            ACLMessage chosenOffer = chooseCNAToExecuteJob(validProposals);
            PricedJob pricedJob;

            try {
                pricedJob = getMapper().readValue(chosenOffer.getContent(), PricedJob.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }

            logger.info("[{}] Sending ACCEPT_PROPOSAL to {}", guid, chosenOffer.getSender().getName());
            myClientAgent.setChosenCloudNetworkAgent(chosenOffer.getSender());
            acceptances.add(ReplyMessageFactory.prepareStringReply(chosenOffer.createReply(), pricedJob.getJobId(), ACCEPT_PROPOSAL));
            rejectJobOffers(myClientAgent, pricedJob.getJobId(), chosenOffer, proposals);
        }
    }

    private ACLMessage chooseCNAToExecuteJob(final List<ACLMessage> receivedOffers) {
        return receivedOffers.stream().min(this::compareCNAOffers).orElseThrow();
    }

    private int compareCNAOffers(final ACLMessage cnaOffer1, final ACLMessage cnaOffer2) {
        PricedJob cna1;
        PricedJob cna2;
        try {
            cna1 = getMapper().readValue(cnaOffer1.getContent(), PricedJob.class);
            cna2 = getMapper().readValue(cnaOffer2.getContent(), PricedJob.class);
        } catch (JsonProcessingException e) {
            return Integer.MAX_VALUE;
        }
        double powerDifference = cna1.getPowerInUse() - cna2.getPowerInUse();
        int priceDifference = (int) (cna1.getPriceForJob() - cna2.getPriceForJob());
        return MAX_TRAFFIC_DIFFERENCE.isValidIntValue((int) powerDifference) ? priceDifference : (int) powerDifference;
    }
}
