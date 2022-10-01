package com.greencloud.application.messages.domain.factory;

import java.io.IOException;
import java.util.List;

import com.greencloud.application.mapper.JsonMapper;

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
	public static ACLMessage createCallForProposal(final Object content, final List<AID> receiverList,
			final String protocol) {
		final ACLMessage proposal = new ACLMessage(ACLMessage.CFP);
		proposal.setProtocol(protocol);
		try {
			proposal.setContent(JsonMapper.getMapper().writeValueAsString(content));
		} catch (IOException e) {
			e.printStackTrace();
		}
		receiverList.forEach(proposal::addReceiver);
		return proposal;
	}
}
