package com.greencloud.application.messages;

import static com.greencloud.application.utils.GUIUtils.displayMessageArrow;
import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;

import java.util.List;
import java.util.Vector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.exception.IncorrectMessageContentException;
import com.greencloud.application.mapper.JsonMapper;
import com.greencloud.application.messages.domain.factory.ReplyMessageFactory;

import jade.lang.acl.ACLMessage;

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
				.forEach(offer -> {
					displayMessageArrow(agent, offer.getSender());
					agent.send(ReplyMessageFactory.prepareReply(offer.createReply(), jobInstanceId, REJECT_PROPOSAL));
				});
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
				.forEach(offer -> {
					displayMessageArrow(agent, offer.getSender());
					agent.send(ReplyMessageFactory.prepareStringReply(offer.createReply(), jobId, REJECT_PROPOSAL));
				});
	}

	/**
	 * Method verifies if the content of the message can be parsed to the given class type
	 *
	 * @param message      message to be validated
	 * @param expectedType expected class type
	 * @return boolean value
	 */
	public static boolean isMessageContentValid(final ACLMessage message, final Class<?> expectedType) {
		try {
			JsonMapper.getMapper().readValue(message.getContent(), expectedType);
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
	public static List<ACLMessage> retrieveValidMessages(final List<ACLMessage> messages,
			final Class<?> expectedClassType) {
		return messages.stream().filter(message -> isMessageContentValid(message, expectedClassType)).toList();
	}

	/**
	 * Method reads the message object content
	 *
	 * @param message           messages to read
	 * @param expectedClassType class type of the message body
	 * @return mapped to Object message content
	 */
	public static <T> T readMessageContent(final ACLMessage message, final Class<?> expectedClassType) {
		try {
			return (T) JsonMapper.getMapper().readValue(message.getContent(), expectedClassType);
		} catch (MismatchedInputException e) {
			throw new IncorrectMessageContentException();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new IncorrectMessageContentException();
		}
	}
}
