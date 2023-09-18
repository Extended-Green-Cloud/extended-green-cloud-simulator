package org.greencloud.rulescontroller.rule.template;

import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_EXPIRATION;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_TEMPLATE;
import static org.greencloud.commons.constants.FactTypeConstants.RECEIVED_MESSAGE;
import static org.greencloud.commons.enums.rules.RuleStepType.SINGLE_MESSAGE_READER_CREATE_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.SINGLE_MESSAGE_READER_HANDLE_MESSAGE_STEP;
import static java.lang.String.format;
import static org.greencloud.rulescontroller.rule.AgentRuleType.LISTENER_SINGLE;

import java.util.List;
import java.util.Optional;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.AgentRuleType;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.AbstractNode;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Abstract class defining structure of a rule which handles default single message retrieval behaviour
 */
public abstract class AgentSingleMessageListenerRule<T extends AgentProps, E extends AbstractNode<?, T>>
		extends AgentBasicRule<T, E> {

	/**
	 * Constructor
	 *
	 * @param controller rules controller connected to the agent
	 */
	protected AgentSingleMessageListenerRule(final RulesController<T, E> controller) {
		super(controller);
	}

	@Override
	public AgentRuleType getAgentRuleType() {
		return LISTENER_SINGLE;
	}

	@Override
	public List<AgentRule> getRules() {
		return List.of(
				new CreateSingleMessageListenerRule(),
				new HandleReceivedMessageRule()
		);
	}

	/**
	 * Method construct template used to retrieve the message
	 */
	protected abstract MessageTemplate constructMessageTemplate(final StrategyFacts facts);

	/**
	 * Method specifies the time after which the message will not be processed
	 */
	protected abstract long specifyExpirationTime(final StrategyFacts facts);

	/**
	 * Method defines handler used to process received message
	 */
	protected abstract void handleMessageProcessing(final ACLMessage message, final StrategyFacts facts);

	/**
	 * Method handles case when message was not received on time
	 */
	protected void handleMessageNotReceived(final StrategyFacts facts) {

	}

	// RULE EXECUTED WHEN SINGLE MESSAGE LISTENER IS BEING INITIATED
	class CreateSingleMessageListenerRule extends AgentBasicRule<T, E> {

		public CreateSingleMessageListenerRule() {
			super(AgentSingleMessageListenerRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public void executeRule(final StrategyFacts facts) {
			final MessageTemplate messageTemplate = constructMessageTemplate(facts);
			final long expirationDuration = specifyExpirationTime(facts);

			facts.put(MESSAGE_TEMPLATE, messageTemplate);
			facts.put(MESSAGE_EXPIRATION, expirationDuration);
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentSingleMessageListenerRule.this.ruleType,
					SINGLE_MESSAGE_READER_CREATE_STEP,
					format("%s - initialization of behaviour", AgentSingleMessageListenerRule.this.name),
					"rule constructs message template and specifies expiration duration");
		}
	}

	// RULE EXECUTED WHEN MESSAGE IS RECEIVED
	class HandleReceivedMessageRule extends AgentBasicRule<T, E> {

		public HandleReceivedMessageRule() {
			super(AgentSingleMessageListenerRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public boolean evaluateRule(final StrategyFacts facts) {
			return ((Optional<?>) facts.get(RECEIVED_MESSAGE)).isPresent();
		}

		@Override
		public void executeRule(final StrategyFacts facts) {
			final Optional<ACLMessage> receivedMessage = facts.get(RECEIVED_MESSAGE);

			receivedMessage.ifPresent(message -> handleMessageProcessing(message, facts));

			if (receivedMessage.isEmpty()) {
				handleMessageNotReceived(facts);
			}
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentSingleMessageListenerRule.this.ruleType,
					SINGLE_MESSAGE_READER_HANDLE_MESSAGE_STEP,
					format("%s - handling received message", AgentSingleMessageListenerRule.this.name),
					"rule triggers method which handles received message");
		}
	}
}
