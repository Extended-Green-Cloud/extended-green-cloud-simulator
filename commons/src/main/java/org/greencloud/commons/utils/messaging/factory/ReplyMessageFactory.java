package org.greencloud.commons.utils.messaging.factory;

import static jade.lang.acl.ACLMessage.ACCEPT_PROPOSAL;
import static jade.lang.acl.ACLMessage.FAILURE;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REFUSE;

import org.greencloud.commons.domain.job.extended.ImmutableJobWithProtocol;
import org.greencloud.commons.domain.job.extended.JobWithProtocol;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.commons.utils.messaging.MessageBuilder;

import jade.lang.acl.ACLMessage;

/**
 * Class storing methods producing messages replies
 */
public class ReplyMessageFactory {

	/**
	 * Method prepares the reply message containing the object content
	 *
	 * @param msg          ACLMessage to be replied to
	 * @param responseData object data that is attached as message content
	 * @param performative performative of the reply message
	 * @return reply ACLMessage
	 */
	public static ACLMessage prepareReply(final ACLMessage msg, final Object responseData, final Integer performative) {
		return MessageBuilder.builder(msg.getOntology())
				.copy(msg.createReply())
				.withPerformative(performative)
				.withObjectContent(responseData)
				.build();
	}

	/**
	 * Method prepares the reply message containing the simple string content
	 *
	 * @param msg          ACLMessage to be replied to
	 * @param content      string that is attached as message content
	 * @param performative performative of the reply message
	 * @return reply ACLMessage
	 */
	public static ACLMessage prepareStringReply(final ACLMessage msg, final String content,
			final Integer performative) {
		return MessageBuilder.builder(msg.getOntology())
				.copy(msg.createReply())
				.withPerformative(performative)
				.withStringContent(content)
				.build();
	}

	/**
	 * Method prepares the reply refusal message
	 *
	 * @param msg ACLMessage to be replied to
	 * @return FAILURE ACLMessage
	 */
	public static ACLMessage prepareFailureReply(final ACLMessage msg) {
		return MessageBuilder.builder(msg.getOntology())
				.copy(msg.createReply())
				.withPerformative(FAILURE)
				.withStringContent("FAILURE")
				.build();
	}

	/**
	 * Method prepares the reply failure message containing the conversation topic as content protocol
	 *
	 * @param msg      ACLMessage to be replied to
	 * @param content  message content
	 * @param protocol message protocol
	 * @return FAILURE ACLMessage
	 */
	public static ACLMessage prepareFailureReply(final ACLMessage msg, final Object content, final String protocol) {
		return MessageBuilder.builder(msg.getOntology())
				.copy(msg.createReply())
				.withPerformative(FAILURE)
				.withObjectContent(content)
				.withMessageProtocol(protocol)
				.build();
	}

	/**
	 * Method prepares the reply refusal message
	 *
	 * @param msg ACLMessage to be replied to
	 * @return REFUSE ACLMessage
	 */
	public static ACLMessage prepareRefuseReply(final ACLMessage msg) {
		return MessageBuilder.builder(msg.getOntology())
				.copy(msg.createReply())
				.withPerformative(REFUSE)
				.withStringContent("REFUSE")
				.build();
	}

	/**
	 * Method prepares the reply inform message
	 *
	 * @param msg ACLMessage to be replied to
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage prepareInformReply(final ACLMessage msg) {
		return MessageBuilder.builder(msg.getOntology())
				.copy(msg.createReply())
				.withPerformative(INFORM)
				.withStringContent("INFORM")
				.build();
	}

	/**
	 * Method prepares the reply message containing the object content
	 *
	 * @param msg          ACLMessage to be replied to
	 * @param responseData object data that is attached as message content
	 * @param performative performative of the reply message
	 * @param replyProtocol protocol of the reply message
	 * @return reply ACLMessage
	 */
	public static ACLMessage prepareReply(final ACLMessage msg, final Object responseData, final Integer performative,
			final String replyProtocol) {
		return MessageBuilder.builder(msg.getOntology())
				.copy(msg.createReply())
				.withPerformative(performative)
				.withMessageProtocol(replyProtocol)
				.withObjectContent(responseData)
				.build();
	}

	/**
	 * Method prepares the reply accept message containing the conversation topic as content protocol
	 *
	 * @param msg           ACLMessage to be replied to
	 * @param jobInstanceId unique job instance identifier
	 * @param protocol      message protocol
	 * @return ACCEPT_PROPOSAL ACLMessage
	 */
	public static ACLMessage prepareAcceptJobOfferReply(final ACLMessage msg,
			final JobInstanceIdentifier jobInstanceId, final String protocol) {
		final JobWithProtocol jobWithProtocol = new ImmutableJobWithProtocol(jobInstanceId, protocol);
		return MessageBuilder.builder(msg.getOntology())
				.copy(msg.createReply())
				.withPerformative(ACCEPT_PROPOSAL)
				.withObjectContent(jobWithProtocol)
				.build();
	}
}
