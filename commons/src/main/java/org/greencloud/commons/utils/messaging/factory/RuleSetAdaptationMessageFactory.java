package org.greencloud.commons.utils.messaging.factory;

import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REQUEST;
import static java.lang.String.valueOf;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.CHANGE_RULE_SET_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.REMOVE_RULE_SET_PROTOCOL;

import java.util.Collection;

import org.jrba.rulesengine.ruleset.domain.ImmutableRuleSetUpdate;
import org.jrba.rulesengine.ruleset.domain.RuleSetUpdate;
import org.jrba.utils.messages.MessageBuilder;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Class storing methods producing messages used in rule set adaptation
 */
public class RuleSetAdaptationMessageFactory {

	/**
	 * Method prepares the request that asks to adapt the agents rule set
	 *
	 * @param currentRuleSetIdx index of current rule set
	 * @param newRuleSetIdx     index of new rule set
	 * @param ruleSetType       type of new rule set
	 * @param receivers         message receivers
	 * @return reply ACLMessage
	 */
	public static ACLMessage prepareRuleSetAdaptationRequest(final int currentRuleSetIdx, final int newRuleSetIdx,
			final String ruleSetType, final Collection<AID> receivers) {
		final RuleSetUpdate updateData = ImmutableRuleSetUpdate.builder()
				.ruleSetIdx(newRuleSetIdx)
				.ruleSetType(ruleSetType)
				.build();
		return MessageBuilder.builder(currentRuleSetIdx, REQUEST)
				.withMessageProtocol(CHANGE_RULE_SET_PROTOCOL)
				.withObjectContent(updateData)
				.withReceivers(receivers)
				.build();
	}

	/**
	 * Method prepares the reply message informing about rule set update
	 *
	 * @param msg        ACLMessage to be replied to
	 * @param ruleSetIdx index of new rule set
	 * @return reply ACLMessage
	 */
	public static ACLMessage prepareRuleSetRequestReply(final ACLMessage msg, final int ruleSetIdx) {
		return MessageBuilder.builder(ruleSetIdx)
				.copy(msg.createReply())
				.withOntology(valueOf(ruleSetIdx))
				.withPerformative(INFORM)
				.withObjectContent(INFORM)
				.build();
	}

	/**
	 * Method prepares the request asking given agents to remove a rule set indicated by the index
	 *
	 * @param ruleSetIdx index of new rule set
	 * @param receivers  message receivers
	 * @return reply ACLMessage
	 */
	public static ACLMessage prepareRuleSetRemovalRequest(final int ruleSetIdx, final Collection<AID> receivers) {
		return MessageBuilder.builder(ruleSetIdx, REQUEST)
				.withMessageProtocol(REMOVE_RULE_SET_PROTOCOL)
				.withStringContent(valueOf(ruleSetIdx))
				.withReceivers(receivers)
				.build();
	}
}
