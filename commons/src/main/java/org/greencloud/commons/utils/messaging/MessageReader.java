package org.greencloud.commons.utils.messaging;

import static com.google.common.collect.Collections2.filter;

import java.util.Collection;
import java.util.Vector;

import org.greencloud.commons.exception.IncorrectMessageContentException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jade.lang.acl.ACLMessage;

/**
 * Class defines set of utilities used to read messages
 */
public class MessageReader {

	private static final ObjectMapper MAPPER = new ObjectMapper()
			.registerModules(new GuavaModule())
			.registerModule(new JavaTimeModule());

	/**
	 * Method reads the message object content
	 *
	 * @param message           messages to read
	 * @param expectedClassType class type of the message body
	 * @return mapped to Object message content
	 */
	public static <T> T readMessageContent(final ACLMessage message, final Class<T> expectedClassType) {
		try {
			return MAPPER.readValue(message.getContent(), expectedClassType);
		} catch (MismatchedInputException | JsonParseException e) {
			throw new IncorrectMessageContentException();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new IncorrectMessageContentException();
		}
	}

	/**
	 * Method retrieves messages from all responses, the ones which match the given performative
	 *
	 * @param responses    all responses
	 * @param performative performative to filter by
	 * @return responses matching the performative
	 */
	public static Collection<ACLMessage> readForPerformative(Vector<ACLMessage> responses, Integer performative) {
		return filter(responses, response -> response.getPerformative() == performative);
	}

}
