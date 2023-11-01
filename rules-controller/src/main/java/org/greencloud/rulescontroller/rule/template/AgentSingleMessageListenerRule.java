package org.greencloud.rulescontroller.rule.template;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_EXPIRATION;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_TEMPLATE;
import static org.greencloud.commons.constants.FactTypeConstants.RECEIVED_MESSAGE;
import static org.greencloud.commons.enums.rules.RuleStepType.SINGLE_MESSAGE_READER_CREATE_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.SINGLE_MESSAGE_READER_HANDLE_MESSAGE_STEP;
import static org.greencloud.rulescontroller.rule.AgentRuleType.LISTENER_SINGLE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.gui.agents.AgentNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rest.domain.SingleMessageListenerRuleRest;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.AgentRuleType;
import org.mvel2.MVEL;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Abstract class defining structure of a rule which handles default single message retrieval behaviour
 */
public class AgentSingleMessageListenerRule<T extends AgentProps, E extends AgentNode<T>>
		extends AgentBasicRule<T, E> {

	private List<AgentRule> stepRules;
	private Serializable expressionConstructMessageTemplate;
	private Serializable expressionSpecifyExpirationTime;
	private Serializable expressionHandleMessageProcessing;
	private Serializable expressionHandleMessageNotReceived;

	/**
	 * Constructor
	 *
	 * @param controller rules controller connected to the agent
	 */
	protected AgentSingleMessageListenerRule(final RulesController<T, E> controller) {
		super(controller);
		initializeSteps();
	}

	/**
	 * Constructor
	 *
	 * @param ruleRest rest representation of agent rule
	 */
	public AgentSingleMessageListenerRule(final SingleMessageListenerRuleRest ruleRest) {
		super(ruleRest);
		if (nonNull(ruleRest.getConstructMessageTemplate())) {
			this.expressionConstructMessageTemplate = MVEL.compileExpression(
					imports + " " + ruleRest.getConstructMessageTemplate());
		}
		if (nonNull(ruleRest.getSpecifyExpirationTime())) {
			this.expressionSpecifyExpirationTime = MVEL.compileExpression(
					imports + " " + ruleRest.getSpecifyExpirationTime());
		}
		if (nonNull(ruleRest.getHandleMessageProcessing())) {
			this.expressionHandleMessageProcessing = MVEL.compileExpression(
					imports + " " + ruleRest.getHandleMessageProcessing());
		}
		if (nonNull(ruleRest.getHandleMessageNotReceived())) {
			this.expressionHandleMessageNotReceived = MVEL.compileExpression(
					imports + " " + ruleRest.getHandleMessageNotReceived());
		}
		initializeSteps();
	}

	public void initializeSteps() {
		stepRules = new ArrayList<>(List.of(
				new CreateSingleMessageListenerRule(),
				new HandleReceivedMessageRule()
		));
	}

	@Override
	public List<AgentRule> getRules() {
		return stepRules;
	}

	@Override
	public void connectToController(final RulesController<?, ?> rulesController) {
		super.connectToController(rulesController);
		stepRules.forEach(rule -> rule.connectToController(rulesController));
	}

	@Override
	public AgentRuleType getAgentRuleType() {
		return LISTENER_SINGLE;
	}

	/**
	 * Method construct template used to retrieve the message
	 */
	protected MessageTemplate constructMessageTemplate(final RuleSetFacts facts) {
		return null;
	}

	/**
	 * Method specifies the time after which the message will not be processed
	 */
	protected long specifyExpirationTime(final RuleSetFacts facts) {
		return 0;
	}

	/**
	 * Method defines handler used to process received message
	 */
	protected void handleMessageProcessing(final ACLMessage message, final RuleSetFacts facts) {
	}

	/**
	 * Method handles case when message was not received on time
	 */
	protected void handleMessageNotReceived(final RuleSetFacts facts) {

	}

	// RULE EXECUTED WHEN SINGLE MESSAGE LISTENER IS BEING INITIATED
	class CreateSingleMessageListenerRule extends AgentBasicRule<T, E> {

		public CreateSingleMessageListenerRule() {
			super(AgentSingleMessageListenerRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public void executeRule(final RuleSetFacts facts) {
			if (nonNull(AgentSingleMessageListenerRule.this.initialParameters)) {
				AgentSingleMessageListenerRule.this.initialParameters.replace("facts", facts);
			}

			final MessageTemplate messageTemplate = isNull(expressionConstructMessageTemplate) ?
					constructMessageTemplate(facts) :
					(MessageTemplate) MVEL.executeExpression(expressionConstructMessageTemplate,
							AgentSingleMessageListenerRule.this.initialParameters);
			final long expirationDuration = isNull(expressionSpecifyExpirationTime) ?
					specifyExpirationTime(facts) :
					(long) MVEL.executeExpression(expressionSpecifyExpirationTime,
							AgentSingleMessageListenerRule.this.initialParameters);

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
		public boolean evaluateRule(final RuleSetFacts facts) {
			return ((Optional<?>) facts.get(RECEIVED_MESSAGE)).isPresent();
		}

		@Override
		public void executeRule(final RuleSetFacts facts) {
			final Optional<ACLMessage> receivedMessage = facts.get(RECEIVED_MESSAGE);
			if (nonNull(AgentSingleMessageListenerRule.this.initialParameters)) {
				AgentSingleMessageListenerRule.this.initialParameters.replace("facts", facts);
			}

			receivedMessage.ifPresent(message -> {
				if (isNull(expressionHandleMessageProcessing)) {
					handleMessageProcessing(message, facts);
				} else {
					AgentSingleMessageListenerRule.this.initialParameters.put("message", message);
					MVEL.executeExpression(expressionHandleMessageProcessing,
							AgentSingleMessageListenerRule.this.initialParameters);
					AgentSingleMessageListenerRule.this.initialParameters.remove("message");
				}
			});

			if (receivedMessage.isEmpty()) {
				if (isNull(expressionHandleMessageNotReceived)) {
					handleMessageNotReceived(facts);
				} else {
					MVEL.executeExpression(expressionHandleMessageNotReceived,
							AgentSingleMessageListenerRule.this.initialParameters);
				}
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
