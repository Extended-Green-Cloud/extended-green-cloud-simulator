package org.greencloud.rulescontroller.behaviour.listen;

import static java.lang.System.currentTimeMillis;
import static java.util.Optional.ofNullable;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_EXPIRATION;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_TEMPLATE;
import static org.greencloud.commons.constants.FactTypeConstants.RECEIVED_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_STEP;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.enums.rules.RuleStepType.SINGLE_MESSAGE_READER_CREATE_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.SINGLE_MESSAGE_READER_HANDLE_MESSAGE_STEP;

import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.commons.enums.rules.RuleType;
import org.greencloud.commons.mapper.FactsMapper;
import org.greencloud.rulescontroller.RulesController;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.states.MsgReceiver;

/**
 * Abstract behaviour providing template to handle single and scheduled message retrieval
 */
public class ListenForSingleMessage extends MsgReceiver {

	protected StrategyFacts facts;
	protected RulesController<?, ?> controller;

	private ListenForSingleMessage(final Agent agent, final StrategyFacts facts,
			final RulesController<?, ?> controller) {
		super(agent, facts.get(MESSAGE_TEMPLATE), (long) facts.get(MESSAGE_EXPIRATION) + currentTimeMillis(),
				null, null);

		this.facts = facts;
		this.controller = controller;
	}

	/**
	 * Method creates behaviour
	 *
	 * @param agent      agent executing the behaviour
	 * @param facts      facts used in single message receiver
	 * @param ruleType   type of the rule that handles single message receiver
	 * @param controller rules controller
	 * @return ListenForSingleMessage
	 */
	public static ListenForSingleMessage create(final Agent agent, final StrategyFacts facts, final RuleType ruleType,
			final RulesController<?, ?> controller) {
		final StrategyFacts methodFacts = FactsMapper.mapToStrategyFacts(facts);
		methodFacts.put(RULE_TYPE, ruleType);
		methodFacts.put(RULE_STEP, SINGLE_MESSAGE_READER_CREATE_STEP);
		controller.fire(methodFacts);

		return new ListenForSingleMessage(agent, methodFacts, controller);
	}

	@Override
	protected void handleMessage(ACLMessage msg) {
		facts.put(RULE_STEP, SINGLE_MESSAGE_READER_HANDLE_MESSAGE_STEP);
		facts.put(RECEIVED_MESSAGE, ofNullable(msg));
		controller.fire(facts);
	}
}
