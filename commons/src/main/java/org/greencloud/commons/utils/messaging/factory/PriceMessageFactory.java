package org.greencloud.commons.utils.messaging.factory;

import static jade.lang.acl.ACLMessage.INFORM;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.EXECUTION_PRICE_MESSAGE;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.FINAL_EXECUTION_PRICE_MESSAGE;

import org.greencloud.commons.domain.job.instance.ImmutableJobInstanceWithPrice;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.jrba.utils.messages.MessageBuilder;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Class storing methods used in creating the messages related to passing information about costs
 */
public class PriceMessageFactory {

	/**
	 * Method prepares a message that informs a given agent about computed price
	 *
	 * @param receiver      receiver of the message
	 * @param jobInstanceId job for which price is computed
	 * @param price         final job execution price
	 * @param ruleIdx       index of the rule set used to process the message
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage preparePriceMessage(final AID receiver, JobInstanceIdentifier jobInstanceId,
			final double price, final int ruleIdx) {
		return MessageBuilder.builder(ruleIdx, INFORM)
				.withMessageProtocol(EXECUTION_PRICE_MESSAGE)
				.withReceivers(receiver)
				.withObjectContent(
						ImmutableJobInstanceWithPrice.builder().price(price).jobInstanceId(jobInstanceId).build())
				.build();
	}

	/**
	 * Method prepares a message that informs a given agent about final computed price
	 *
	 * @param replyWith     flag specifying the message to which agent replies with a price
	 * @param receiver      receiver of the message
	 * @param jobInstanceId job for which price is computed
	 * @param price         final job execution price
	 * @param ruleIdx       index of the rule set used to process the message
	 * @return INFORM ACLMessage
	 */
	public static ACLMessage prepareFinalPriceMessage(final String replyWith, final AID receiver,
			final JobInstanceIdentifier jobInstanceId, final double price, final int ruleIdx) {
		return MessageBuilder.builder(ruleIdx, INFORM)
				.withMessageProtocol(FINAL_EXECUTION_PRICE_MESSAGE)
				.withReceivers(receiver)
				.withReplyWith(replyWith)
				.withObjectContent(
						ImmutableJobInstanceWithPrice.builder().price(price).jobInstanceId(jobInstanceId).build())
				.build();
	}
}
