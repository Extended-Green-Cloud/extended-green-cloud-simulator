package com.greencloud.application.messages.domain.factory;

import static com.greencloud.application.mapper.JsonMapper.getMapper;
import static jade.lang.acl.ACLMessage.ACCEPT_PROPOSAL;
import static jade.lang.acl.ACLMessage.FAILURE;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REFUSE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greencloud.application.domain.job.ImmutableJobWithProtocol;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.JobWithProtocol;

import jade.lang.acl.ACLMessage;

/**
 * Class storing methods used in creating reply messages
 */
public class ReplyMessageFactory {

	/**
	 * Method prepares the reply message containing the object content
	 *
	 * @param reply        reply ACLMessage that is to be sent
	 * @param responseData object data that is attached as message content
	 * @param performative performative of the reply message
	 * @return reply ACLMessage
	 */
	public static ACLMessage prepareReply(ACLMessage reply, Object responseData, Integer performative) {
		reply.setPerformative(performative);
		try {
			reply.setContent(getMapper().writeValueAsString(responseData));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return reply;
	}

	/**
	 * Method prepares the reply message containing the simple string content
	 *
	 * @param reply        reply ACLMessage that is to be sent
	 * @param content      string that is attached as message content
	 * @param performative performative of the reply message
	 * @return reply ACLMessage
	 */
	public static ACLMessage prepareStringReply(ACLMessage reply, String content, Integer performative) {
		reply.setPerformative(performative);
		reply.setContent(content);
		return reply;
	}

	/**
	 * Method prepares the reply refusal message
	 *
	 * @param replyMessage reply ACLMessage that is to be sent
	 * @return reply ACLMessage
	 */
	public static ACLMessage prepareRefuseReply(final ACLMessage replyMessage) {
		replyMessage.setPerformative(REFUSE);
		replyMessage.setContent("REFUSE");
		return replyMessage;
	}

	/**
	 * Method prepares the reply inform message
	 *
	 * @param replyMessage reply ACLMessage that is to be sent
	 * @return reply ACLMessage
	 */
	public static ACLMessage prepareInformReply(final ACLMessage replyMessage) {
		replyMessage.setPerformative(INFORM);
		replyMessage.setContent("INFORM");
		return replyMessage;
	}

	/**
	 * Method prepares the reply accept message containing the conversation topic as content protocol
	 *
	 * @param replyMessage  reply ACLMessage that is to be sent
	 * @param jobInstanceId unique job instance identifier
	 * @param protocol      conversation topic being expected response protocol
	 * @return reply ACLMessage
	 */
	public static ACLMessage prepareAcceptReplyWithProtocol(final ACLMessage replyMessage,
			final JobInstanceIdentifier jobInstanceId, final String protocol) {
		final JobWithProtocol pricedJob = ImmutableJobWithProtocol.builder()
				.jobInstanceIdentifier(jobInstanceId)
				.replyProtocol(protocol)
				.build();
		replyMessage.setPerformative(ACCEPT_PROPOSAL);
		try {
			replyMessage.setContent(getMapper().writeValueAsString(pricedJob));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return replyMessage;
	}

	/**
	 * Method prepares the reply accept message containing the conversation topic as content protocol
	 *
	 * @param replyMessage reply ACLMessage that is to be sent
	 * @param content      unique job instance identifier
	 * @param protocol     message protocol
	 * @return reply ACLMessage
	 */
	public static ACLMessage prepareFailureReply(final ACLMessage replyMessage,
			final Object content, final String protocol) {
		replyMessage.setProtocol(protocol);
		replyMessage.setPerformative(FAILURE);
		try {
			replyMessage.setContent(getMapper().writeValueAsString(content));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return replyMessage;
	}

}
