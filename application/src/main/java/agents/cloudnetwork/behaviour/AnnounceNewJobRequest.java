package agents.cloudnetwork.behaviour;

import static agents.cloudnetwork.CloudNetworkAgentConstants.MAX_POWER_DIFFERENCE;
import static mapper.JsonMapper.getMapper;
import static messages.MessagingUtils.rejectJobOffers;
import static messages.MessagingUtils.retrieveProposals;
import static messages.domain.JobOfferMessageFactory.makeJobOfferForClient;

import agents.cloudnetwork.CloudNetworkAgent;
import com.fasterxml.jackson.core.JsonProcessingException;
import domain.ServerData;
import exception.IncorrectServerOfferException;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import messages.domain.ReplyMessageFactory;
import net.miginfocom.layout.AC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * Behaviour which is responsible for broadcasting client's job to servers and choosing server to execute the job
 */
public class AnnounceNewJobRequest extends ContractNetInitiator {
    private static final Logger logger = LoggerFactory.getLogger(AnnounceNewJobRequest.class);

    private final ACLMessage replyMessage;
    private final CloudNetworkAgent myCloudNetworkAgent;
    private final String guid;

    /**
     * Behaviour constructor.
     *
     * @param agent        agent which is executing the behaviour
     * @param cfp          call for proposal message containing job requriements sent to the servers
     * @param replyMessage reply message sent to client after retreiving the servers' responses
     */
    public AnnounceNewJobRequest(final Agent agent, final ACLMessage cfp, final ACLMessage replyMessage) {
        super(agent, cfp);
        this.myCloudNetworkAgent = (CloudNetworkAgent) myAgent;
        this.guid = agent.getName();
        this.replyMessage = replyMessage;
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
            logger.info("[{}] No Servers available - sending refuse message to client", guid);
            myAgent.send(ReplyMessageFactory.prepareRefuseReply(replyMessage));
        } else {
            final ACLMessage chosenServerOffer = chooseServerToExecuteJob(proposals);
            logger.info("[{}] Chosen Server for the job: {}", guid, chosenServerOffer.getSender().getName());
            final ACLMessage serverReplyMessage = chosenServerOffer.createReply();

            ServerData chosenServerData;
            try {
                chosenServerData = getMapper().readValue(chosenServerOffer.getContent(), ServerData.class);
            } catch (JsonProcessingException e) {
                throw new IncorrectServerOfferException();
            }

            logger.info("[{}] Sending job execution offer to Client", guid);
            myCloudNetworkAgent.getServerForJobMap().put(chosenServerData.getJobId(), chosenServerOffer.getSender());
            myAgent.addBehaviour(new ProposeJobOffer(myAgent, makeJobOfferForClient(chosenServerData, replyMessage), serverReplyMessage));
            rejectJobOffers(myCloudNetworkAgent, chosenServerData.getJobId(), chosenServerOffer, proposals);
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
            throw new IncorrectServerOfferException();
        }
        int powerDifference = server1.getAvailablePower() - server2.getAvailablePower();
        int priceDifference = (int) (server1.getServicePrice() - server2.getServicePrice());
        return MAX_POWER_DIFFERENCE.isValidIntValue(powerDifference) ? priceDifference : powerDifference;
    }
}
