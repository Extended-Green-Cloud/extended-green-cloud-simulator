package org.greencloud.commons.utils.messaging;

import static org.jrba.utils.messages.MessageReader.readMessageContent;

import java.util.Comparator;

import org.greencloud.commons.exception.IncorrectMessageContentException;

import jade.lang.acl.ACLMessage;

/**
 *
 */
public class MessageComparator {

	/**
	 * Generic method used for comparing agent messages
	 *
	 * @param message1 first message for comparison
	 * @param message2 second message for comparison
	 * @param type     type of the messages content
	 * @return method returns:
	 * <p> val > 0 - if the message1 is better</p>
	 * <p> val = 0 - if both messages are equivalently good</p>
	 * <p> val < 0 - if the message2 is better</p>
	 */
	public static <T> int compareMessages(final ACLMessage message1, final ACLMessage message2,
			final Class<T> type, final Comparator<T> comparator) {
		try {
			final T message1Content = readMessageContent(message1, type);
			final T message2Content = readMessageContent(message2, type);

			return comparator.compare(message1Content, message2Content);
		} catch (IncorrectMessageContentException e) {
			e.printStackTrace();
			return Integer.MAX_VALUE;
		}
	}
}
