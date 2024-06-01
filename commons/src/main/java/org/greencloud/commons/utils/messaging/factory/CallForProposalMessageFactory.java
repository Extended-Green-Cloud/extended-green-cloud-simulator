package org.greencloud.commons.utils.messaging.factory;

import static jade.lang.acl.ACLMessage.CFP;
import static jade.lang.acl.ACLMessage.REQUEST;
import static org.greencloud.commons.mapper.JobMapper.mapToAllocatedJobs;

import java.util.Collection;
import java.util.List;

import org.greencloud.commons.domain.job.basic.ClientJob;
import org.jrba.utils.messages.MessageBuilder;

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
			final String protocol, final Integer ruleSetIdx) {
		return MessageBuilder.builder(ruleSetIdx, CFP)
				.withMessageProtocol(protocol)
				.withObjectContent(content)
				.withReceivers(receiverList)
				.build();
	}

	/**
	 * Method creates a message requesting allocated jobs execution
	 *
	 * @param jobs  jobs that are to asked for execution
	 * @param agent agent to which the jobs are allocated
	 * @return REQUEST message
	 */
	public static <T extends ClientJob> ACLMessage prepareExecutionRequest(final List<T> jobs, final AID agent,
			final int ruleSetIdx, final String protocol) {
		return MessageBuilder.builder(ruleSetIdx, REQUEST)
				.withMessageProtocol(protocol)
				.withObjectContent(mapToAllocatedJobs(jobs))
				.withReceivers(agent)
				.build();
	}
}
