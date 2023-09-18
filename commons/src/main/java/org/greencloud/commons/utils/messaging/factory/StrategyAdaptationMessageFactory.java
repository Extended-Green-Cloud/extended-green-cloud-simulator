package org.greencloud.commons.utils.messaging.factory;

import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REQUEST;
import static java.lang.String.valueOf;

import java.util.Collection;

import org.greencloud.commons.domain.strategy.ImmutableStrategyUpdate;
import org.greencloud.commons.domain.strategy.StrategyUpdate;
import org.greencloud.commons.enums.strategy.StrategyType;
import org.greencloud.commons.utils.messaging.MessageBuilder;
import org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Class storing methods producing messages used in strategy adaptation
 */
public class StrategyAdaptationMessageFactory {

	/**
	 * Method prepares the request that asks to adapt the agents strategy
	 *
	 * @param currentStrategyIdx index of current strategy
	 * @param newStrategyIdx     index of new strategy
	 * @param strategyType       type of new strategy
	 * @param receivers          message receivers
	 * @return reply ACLMessage
	 */
	public static ACLMessage prepareStrategyAdaptationRequest(final int currentStrategyIdx, final int newStrategyIdx,
			final StrategyType strategyType, final Collection<AID> receivers) {
		final StrategyUpdate updateData = new ImmutableStrategyUpdate(newStrategyIdx, strategyType);
		return MessageBuilder.builder(currentStrategyIdx)
				.withMessageProtocol(MessageProtocolConstants.CHANGE_STRATEGY_PROTOCOL)
				.withPerformative(REQUEST)
				.withObjectContent(updateData)
				.withReceivers(receivers)
				.build();
	}

	/**
	 * Method prepares the reply message informing about strategy update
	 *
	 * @param msg         ACLMessage to be replied to
	 * @param strategyIdx index of new strategy
	 * @return reply ACLMessage
	 */
	public static ACLMessage prepareStrategyRequestReply(final ACLMessage msg, final int strategyIdx) {
		return MessageBuilder.builder(strategyIdx)
				.copy(msg.createReply())
				.withOntology(valueOf(strategyIdx))
				.withPerformative(INFORM)
				.withObjectContent(INFORM)
				.build();
	}

	/**
	 * Method prepares the request asking given agents to remove a strategy indicated by the index
	 *
	 * @param strategyId index of new strategy
	 * @param receivers  message receivers
	 * @return reply ACLMessage
	 */
	public static ACLMessage prepareStrategyRemovalRequest(final int strategyId, final Collection<AID> receivers) {
		return MessageBuilder.builder(strategyId)
				.withMessageProtocol(MessageProtocolConstants.REMOVE_STRATEGY_PROTOCOL)
				.withPerformative(REQUEST)
				.withStringContent(valueOf(strategyId))
				.withReceivers(receivers)
				.build();
	}
}
