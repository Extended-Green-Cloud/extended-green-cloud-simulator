package org.greencloud.rulescontroller.behaviour.listen;

import static java.lang.Integer.parseInt;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGES;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_STEP;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.enums.rules.RuleStepType.MESSAGE_READER_READ_CONTENT_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.MESSAGE_READER_READ_STEP;

import java.util.List;
import java.util.Objects;

import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.mapper.FactsMapper;
import org.greencloud.rulescontroller.RulesController;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Abstract behaviour providing template to handle message retrieval
 */
public class ListenForMessages extends CyclicBehaviour {

	private final String ruleType;
	protected RulesController<?, ?> controller;
	protected boolean omitRuleSetFromMessage;

	/**
	 * Constructor
	 *
	 * @param agent      agent executing the behaviour
	 * @param ruleType   type of the rule that handles message retrieval execution
	 * @param controller rules controller
	 */
	protected ListenForMessages(final Agent agent, final String ruleType, final RulesController<?, ?> controller) {
		super(agent);
		this.ruleType = ruleType;
		this.controller = controller;
		this.omitRuleSetFromMessage = false;
	}

	/**
	 * Constructor
	 *
	 * @param agent                  agent executing the behaviour
	 * @param ruleType               type of the rule that handles message retrieval execution
	 * @param controller             rules controller
	 * @param omitRuleSetFromMessage flag which ignores the rule set passed in the message content
	 */
	protected ListenForMessages(final Agent agent, final String ruleType, final RulesController<?, ?> controller,
			final boolean omitRuleSetFromMessage) {
		this(agent, ruleType, controller);
		this.omitRuleSetFromMessage = omitRuleSetFromMessage;
	}

	/**
	 * Behaviour creator
	 *
	 * @param agent      agent executing the behaviour
	 * @param ruleType   type of the rule that handles message retrieval execution
	 * @param controller rules controller
	 */
	public static ListenForMessages create(final Agent agent, final String ruleType,
			final RulesController<?, ?> controller) {
		return new ListenForMessages(agent, ruleType, controller);
	}

	/**
	 * Behaviour creator
	 *
	 * @param agent                  agent executing the behaviour
	 * @param ruleType               type of the rule that handles message retrieval execution
	 * @param controller             rules controller
	 * @param omitRuleSetFromMessage flag which ignores the rule set passed in the message content
	 */
	public static ListenForMessages create(final Agent agent, final String ruleType,
			final RulesController<?, ?> controller, final boolean omitRuleSetFromMessage) {
		return new ListenForMessages(agent, ruleType, controller, omitRuleSetFromMessage);
	}

	/**
	 * Method listens for the upcoming messages that match indicated template
	 */
	@Override
	public void action() {
		final RuleSetFacts facts = constructMessageRetrievalFacts();
		facts.put(RULE_SET_IDX, controller.getLatestLongTermRuleSetIdx().get());
		controller.fire(facts);
		final List<ACLMessage> messages = facts.get(MESSAGES);

		if (nonNull(messages) && messages.stream().allMatch(Objects::nonNull) && !messages.isEmpty()) {
			messages.forEach(message -> {
				final RuleSetFacts factsToProcessMessage = FactsMapper.mapToRuleSetFacts(facts);
				factsToProcessMessage.put(MESSAGE, message);

				if (!omitRuleSetFromMessage && controller.getRuleSets().containsKey(parseInt(message.getOntology()))) {
					factsToProcessMessage.put(RULE_SET_IDX, parseInt(message.getOntology()));
				}

				factsToProcessMessage.put(RULE_STEP, MESSAGE_READER_READ_CONTENT_STEP);
				controller.fire(factsToProcessMessage);
				postProcessMessage(factsToProcessMessage);
			});

		} else {
			block();
		}
	}

	/**
	 * Method can be optionally overridden in order to perform facts-based actions at the end of message processing
	 */
	protected void postProcessMessage(final RuleSetFacts facts) {
		// to be overridden if necessary
	}

	private RuleSetFacts constructMessageRetrievalFacts() {
		final RuleSetFacts facts = new RuleSetFacts(0);
		facts.put(RULE_TYPE, ruleType);
		facts.put(RULE_STEP, MESSAGE_READER_READ_STEP);
		return facts;
	}
}
