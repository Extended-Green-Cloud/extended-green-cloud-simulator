package org.greencloud.rulescontroller.rule.template;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGES;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_STEP;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.enums.rules.RuleStepType.MESSAGE_READER_PROCESS_CONTENT_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.MESSAGE_READER_READ_CONTENT_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.MESSAGE_READER_READ_STEP;
import static org.greencloud.commons.utils.messaging.MessageReader.readMessageContent;
import static org.greencloud.rulescontroller.rule.AgentRuleType.LISTENER;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.mapper.FactsMapper;
import org.greencloud.gui.agents.AgentNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rest.domain.MessageListenerRuleRest;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.AgentRuleType;
import org.greencloud.rulescontroller.rule.simple.AgentChainRule;
import org.greencloud.rulescontroller.ruleset.RuleSet;
import org.jeasy.rules.api.Facts;
import org.mvel2.MVEL;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import lombok.Getter;

/**
 * Abstract class defining structure of a rule which handles default message retrieval behaviour
 */
@Getter
public class AgentMessageListenerRule<T extends AgentProps, E extends AgentNode<T>> extends AgentBasicRule<T, E> {

	final RuleSet ruleSet;
	final Class<?> contentType;
	final MessageTemplate messageTemplate;
	final int batchSize;
	final String handlerRuleType;
	private List<AgentRule> stepRules;
	private Serializable expressionSelectRuleSetIdx;

	/**
	 * Constructor
	 *
	 * @param controller      rules controller connected to the agent
	 * @param ruleSet         currently executed rule set
	 * @param contentType     type of content read in the messages
	 * @param template        template used to read messages
	 * @param batchSize       number of messages read at once
	 * @param handlerRuleType rule run when the messages are present
	 */
	protected AgentMessageListenerRule(final RulesController<T, E> controller,
			final RuleSet ruleSet, final Class<?> contentType, final MessageTemplate template, final int batchSize,
			final String handlerRuleType) {
		super(controller);
		this.contentType = contentType;
		this.messageTemplate = template;
		this.ruleSet = ruleSet;
		this.batchSize = batchSize;
		this.handlerRuleType = handlerRuleType;
		initializeSteps();
	}

	/**
	 * Constructor
	 *
	 * @param controller      rules controller connected to the agent
	 * @param ruleSet         currently executed rule set
	 * @param template        template used to read messages
	 * @param batchSize       number of messages read at once
	 * @param handlerRuleType rule run when the messages are present
	 */
	protected AgentMessageListenerRule(final RulesController<T, E> controller, final RuleSet ruleSet,
			final MessageTemplate template, final int batchSize, final String handlerRuleType) {
		super(controller);
		this.contentType = null;
		this.messageTemplate = template;
		this.ruleSet = ruleSet;
		this.batchSize = batchSize;
		this.handlerRuleType = handlerRuleType;
		initializeSteps();
	}

	/**
	 * Constructor
	 *
	 * @param ruleRest rest representation of agent rule
	 * @param ruleSet  currently executed rule set
	 */
	public AgentMessageListenerRule(final MessageListenerRuleRest ruleRest, final RuleSet ruleSet) {
		super(ruleRest);
		try {
			final Serializable msgTemplateExpression = MVEL.compileExpression(
					imports + " " + ruleRest.getMessageTemplate());
			this.messageTemplate = (MessageTemplate) MVEL.executeExpression(msgTemplateExpression);
			this.contentType = Class.forName(ruleRest.getClassName());
			this.ruleSet = ruleSet;
			this.batchSize = ruleRest.getBatchSize();
			this.handlerRuleType = ruleRest.getActionHandler();

			if (nonNull(ruleRest.getSelectRuleSetIdx())) {
				this.expressionSelectRuleSetIdx = MVEL.compileExpression(
						imports + " " + ruleRest.getSelectRuleSetIdx());
			}
			initializeSteps();
		} catch (ClassNotFoundException classNotFoundException) {
			throw new ClassCastException("Content type class was not found!");
		}
		initializeSteps();
	}

	@Override
	public AgentRuleType getAgentRuleType() {
		return LISTENER;
	}

	public void initializeSteps() {
		stepRules = new ArrayList<>(
				List.of(new ReadMessagesRule(), new ReadMessagesContentRule(), new HandleMessageRule()));
	}

	@Override
	public List<AgentRule> getRules() {
		return stepRules;
	}

	/**
	 * Method can be optionally overwritten in order to change rule set based on facts after reading message content
	 */
	protected int selectRuleSetIdx(final RuleSetFacts facts) {
		return facts.get(RULE_SET_IDX);
	}

	class ReadMessagesRule extends AgentBasicRule<T, E> {

		public ReadMessagesRule() {
			super(AgentMessageListenerRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public void executeRule(final RuleSetFacts facts) {
			final List<ACLMessage> messages = agent.receive(messageTemplate, batchSize);
			facts.put(MESSAGES, ofNullable(messages).orElse(emptyList()));
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentMessageListenerRule.this.ruleType, MESSAGE_READER_READ_STEP,
					format("%s - read messages", AgentMessageListenerRule.this.name),
					"when new message event is triggerred, agent attempts to read messages corresponding to"
							+ "selected template");
		}
	}

	class ReadMessagesContentRule extends AgentChainRule<T, E> {

		public ReadMessagesContentRule() {
			super(AgentMessageListenerRule.this.controller, AgentMessageListenerRule.this.ruleSet);
			this.isRuleStep = true;
		}

		@Override
		public void executeRule(final RuleSetFacts facts) {
			final ACLMessage message = facts.get(MESSAGE);

			if (nonNull(contentType)) {
				final Object content = readMessageContent(message, contentType);
				facts.put(MESSAGE_CONTENT, content);
			}
			if (nonNull(AgentMessageListenerRule.this.initialParameters)) {
				AgentMessageListenerRule.this.initialParameters.replace("facts", facts);
			}

			int ruleSetIdx = selectRuleSetIdx(facts);

			if (nonNull(expressionSelectRuleSetIdx)) {
				ruleSetIdx = (int) MVEL.executeExpression(expressionSelectRuleSetIdx,
						AgentMessageListenerRule.this.initialParameters);
			}

			facts.put(RULE_SET_IDX, ruleSetIdx);
			facts.put(MESSAGE_TYPE, ofNullable(message.getConversationId()).orElse(""));
			facts.put(RULE_STEP, MESSAGE_READER_PROCESS_CONTENT_STEP);
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentMessageListenerRule.this.ruleType,
					MESSAGE_READER_READ_CONTENT_STEP,
					format("%s - read message content", AgentMessageListenerRule.this.name),
					"when new message matching given template is present, then agent reads its content");
		}
	}

	class HandleMessageRule extends AgentBasicRule<T, E> {

		public HandleMessageRule() {
			super(AgentMessageListenerRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public boolean evaluateRule(final RuleSetFacts facts) {
			return true;
		}

		@Override
		public void execute(final Facts facts) throws Exception {
			final RuleSetFacts triggerFacts = FactsMapper.mapToRuleSetFacts(facts);
			triggerFacts.put(RULE_TYPE, handlerRuleType);
			controller.fire(triggerFacts);
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentMessageListenerRule.this.ruleType,
					MESSAGE_READER_PROCESS_CONTENT_STEP,
					format("%s - handle message", AgentMessageListenerRule.this.name),
					"when agent reads message of given type, its handler is run");
		}
	}

}
