package org.greencloud.commons.utils.messaging;

import static java.lang.Integer.parseInt;
import static org.greencloud.commons.mapper.JsonMapper.getMapper;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonProcessingException;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Class used to build various messages in the system
 */
public class MessageBuilder {

	private ACLMessage aclMessage;

	private MessageBuilder(final Integer ruleIdx) {
		aclMessage = new ACLMessage();
		aclMessage.setOntology(ruleIdx.toString());
	}

	public static MessageBuilder builder(final Integer ruleIdx) {
		return new MessageBuilder(ruleIdx);
	}

	public static MessageBuilder builder(final String ruleIdx) {
		return new MessageBuilder(parseInt(ruleIdx));
	}

	public MessageBuilder withMessageProtocol(final String messageProtocol) {
		this.aclMessage.setProtocol(messageProtocol);
		return this;
	}

	public MessageBuilder withOntology(final String messageOntology) {
		this.aclMessage.setOntology(messageOntology);
		return this;
	}

	public MessageBuilder withConversationId(final String conversationId) {
		this.aclMessage.setConversationId(conversationId);
		return this;
	}

	public MessageBuilder withGeneratedReplyWith() {
		final String replyWith = UUID.randomUUID().toString();
		this.aclMessage.setReplyWith(replyWith);
		return this;
	}

	public MessageBuilder withStringContent(final String content) {
		this.aclMessage.setContent(content);
		return this;
	}

	public MessageBuilder withObjectContent(final Object content) {
		try {
			this.aclMessage.setContent(getMapper().writeValueAsString(content));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	public MessageBuilder withObjectContent(final Object content, final Consumer<Exception> errorHandler) {
		try {
			this.aclMessage.setContent(getMapper().writeValueAsString(content));
		} catch (JsonProcessingException e) {
			errorHandler.accept(e);
		}
		return this;
	}

	public MessageBuilder withPerformative(final Integer performative) {
		this.aclMessage.setPerformative(performative);
		return this;
	}

	public MessageBuilder withReplyWith(final String replyWith) {
		aclMessage.setReplyWith(replyWith);
		return this;
	}

	public MessageBuilder withReceivers(final AID... aids) {
		Arrays.stream(aids).forEach(aclMessage::addReceiver);
		return this;
	}

	public MessageBuilder withNewReceivers(final AID... aids) {
		aclMessage.clearAllReceiver();
		Arrays.stream(aids).forEach(aclMessage::addReceiver);
		return this;
	}

	public MessageBuilder withReceivers(final Collection<AID> aids) {
		aids.forEach(aclMessage::addReceiver);
		return this;
	}

	public MessageBuilder copy(final ACLMessage message) {
		aclMessage = (ACLMessage) message.clone();
		return this;
	}

	public ACLMessage build() {
		return aclMessage;
	}
}
