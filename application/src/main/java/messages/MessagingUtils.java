package messages;

import static common.GUIUtils.displayMessageArrow;
import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;

import agents.AbstractAgent;
import jade.lang.acl.ACLMessage;
import messages.domain.ReplyMessageFactory;

import java.util.List;
import java.util.Vector;

/**
 * Service providing common utilities for message exchange
 */
public class MessagingUtils {

    /**
     * Method retrieved from all responses, the ones which are the proposals
     *
     * @param responses all responses
     * @return responses which are the proposals
     */
    public static List<ACLMessage> retrieveProposals(final Vector responses) {
        return ((Vector<ACLMessage>) responses).stream()
                .filter(response -> response.getPerformative() == ACLMessage.PROPOSE)
                .toList();
    }

    /**
     * Method sends the reject proposal messages to all agents which sent the offers except the one
     * which was chosen
     *
     * @param agent          agent which is sent the reject proposal messages
     * @param jobId          unique identifier of the job of interest
     * @param chosenOffer    chosen offer message
     * @param receivedOffers all retrieved offer messages
     */
    public static void rejectJobOffers(final AbstractAgent agent, final String jobId, final ACLMessage chosenOffer, final List<ACLMessage> receivedOffers) {
        receivedOffers.stream()
                .filter(offer -> !offer.equals(chosenOffer))
                .forEach(offer -> {
                    displayMessageArrow(agent, offer.getSender());
                    agent.send(ReplyMessageFactory.prepareReply(offer.createReply(), jobId, REJECT_PROPOSAL));
                });
    }
}
