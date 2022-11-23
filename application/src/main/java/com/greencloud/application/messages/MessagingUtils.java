package com.greencloud.application.messages;

import static com.google.common.collect.Collections2.filter;
import static com.greencloud.application.mapper.JsonMapper.getMapper;
import static jade.lang.acl.ACLMessage.PROPOSE;
import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.exception.IncorrectMessageContentException;
import com.greencloud.application.messages.domain.factory.ReplyMessageFactory;

import jade.lang.acl.ACLMessage;

/**
 * Service providing common utilities for message exchange
 */
public class MessagingUtils {

	/**
	 * Method retrieves messages from all responses, the ones which are the proposals
	 *
	 * @param responses all responses
	 * @return responses which are the proposals
	 */
	public static List<ACLMessage> retrieveProposals(final Vector<ACLMessage> responses) {
		return retrieveForPerformative(responses, PROPOSE).stream().toList();
	}

	/**
	 * Method retrieves messages from all responses, the ones which match the given performative
	 *
	 * @param responses    all responses
	 * @param performative performative to filter by
	 * @return responses matching the performative
	 */
	public static Collection<ACLMessage> retrieveForPerformative(Vector<ACLMessage> responses, Integer performative) {
		return filter(responses, response -> response.getPerformative() == performative);
	}

	/**
	 * Method sends the reject proposal messages to all com.greencloud.application.agents which sent the offers except the one
	 * which was chosen
	 *
	 * @param agent          agent which is sent the reject proposal messages
	 * @param jobInstanceId  unique identifier of the job instance
	 * @param chosenOffer    chosen offer message
	 * @param receivedOffers all retrieved offer messages
	 */
	public static void rejectJobOffers(final AbstractAgent agent, final JobInstanceIdentifier jobInstanceId,
			final ACLMessage chosenOffer, final List<ACLMessage> receivedOffers) {
		receivedOffers.stream()
				.filter(offer -> !offer.equals(chosenOffer))
				.forEach(offer -> agent.send(
						ReplyMessageFactory.prepareReply(offer.createReply(), jobInstanceId, REJECT_PROPOSAL)));
	}

	/**
	 * Method sends the reject proposal messages to all com.greencloud.application.agents which sent the offers except the one
	 * which was chosen
	 *
	 * @param agent          agent which is sent the reject proposal messages
	 * @param jobId          unique identifier of the job of interest
	 * @param chosenOffer    chosen offer message
	 * @param receivedOffers all retrieved offer messages
	 */
	public static void rejectJobOffers(final AbstractAgent agent, final String jobId, final ACLMessage chosenOffer,
			final List<ACLMessage> receivedOffers) {
		receivedOffers.stream()
				.filter(offer -> !offer.equals(chosenOffer))
				.forEach(offer -> agent.send(
						ReplyMessageFactory.prepareStringReply(offer.createReply(), jobId, REJECT_PROPOSAL)));
	}

	/**
	 * Method verifies if the content of the message can be parsed to the given class type
	 *
	 * @param message      message to be validated
	 * @param expectedType expected class type
	 * @return boolean value
	 */
	public static <T> boolean isMessageContentValid(final ACLMessage message, final Class<T> expectedType) {
		try {
			getMapper().readValue(message.getContent(), expectedType);
			return true;
		} catch (JsonProcessingException e) {
			return false;
		}
	}

	/**
	 * Method retrieves from the list of messages, only the ones which have the valid content body
	 *
	 * @param messages          messages to traverse
	 * @param expectedClassType class type of the message body
	 * @return list of valid messages
	 */
	public static <T> List<ACLMessage> retrieveValidMessages(final List<ACLMessage> messages,
			final Class<T> expectedClassType) {
		return messages.stream()
				.filter(message -> isMessageContentValid(message, expectedClassType))
				.toList();
	}

	/**
	 * Method reads the message object content
	 *
	 * @param message           messages to read
	 * @param expectedClassType class type of the message body
	 * @return mapped to Object message content
	 */
	public static <T> T readMessageContent(final ACLMessage message, final Class<T> expectedClassType) {
		try {
			return getMapper().readValue(message.getContent(), expectedClassType);
		} catch (MismatchedInputException e) {
			throw new IncorrectMessageContentException();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new IncorrectMessageContentException();
		}
	}

	/**
	 * Method reads the message object content
	 *
	 * @param message           messages to read
	 * @param expectedClassType class type of the message body
	 * @return mapped to List of objects message content
	 */
	public static <T> List<T> readMessageListContent(final ACLMessage message, final Class<T> expectedClassType) {
		try {
			return getMapper().readValue(message.getContent(),
					getMapper().getTypeFactory().constructCollectionType(List.class, expectedClassType));
		} catch (MismatchedInputException e) {
			throw new IncorrectMessageContentException();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new IncorrectMessageContentException();
		}
	}
}
