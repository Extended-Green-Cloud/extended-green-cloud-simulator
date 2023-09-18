package org.greencloud.commons.utils.messaging.factory;

import static jade.lang.acl.ACLMessage.CFP;

import java.util.Collection;

import org.greencloud.commons.utils.messaging.MessageBuilder;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Class storing methods used in creating Call For Proposal messages
 */
public class CallForProposalMessageFactory {

	/**
	 * Method creates the call for proposal message that is to be sent to multiple receivers
	 *
	 * @param content      content that is to be sent in call for proposal
	 * @param receiverList list of the message receivers
	 * @param protocol     protocol of the call for proposal message
	 * @return call for proposal ACLMessage
	 */
	public static ACLMessage prepareCallForProposal(final Object content, final Collection<AID> receiverList,
			final String protocol, final Integer strategy) {
		return MessageBuilder.builder(strategy)
				.withPerformative(CFP)
				.withMessageProtocol(protocol)
				.withObjectContent(content)
				.withReceivers(receiverList)
				.build();
	}
}
