package org.greencloud.rulescontroller.behaviour.initiate;

import static jade.lang.acl.ACLMessage.FAILURE;
import static jade.lang.acl.ACLMessage.INFORM;
import static org.greencloud.commons.constants.FactTypeConstants.REQUEST_CREATE_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.REQUEST_FAILURE_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.REQUEST_FAILURE_RESULTS_MESSAGES;
import static org.greencloud.commons.constants.FactTypeConstants.REQUEST_INFORM_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.REQUEST_INFORM_RESULTS_MESSAGES;
import static org.greencloud.commons.constants.FactTypeConstants.REQUEST_REFUSE_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_STEP;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.enums.rules.RuleStepType.REQUEST_CREATE_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.REQUEST_HANDLE_ALL_RESULTS_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.REQUEST_HANDLE_FAILURE_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.REQUEST_HANDLE_INFORM_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.REQUEST_HANDLE_REFUSE_STEP;
import static org.greencloud.commons.utils.messaging.MessageReader.readForPerformative;

import java.util.Vector;

import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.commons.enums.rules.RuleType;
import org.greencloud.commons.mapper.FactsMapper;
import org.greencloud.rulescontroller.RulesController;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

public class InitiateRequest extends AchieveREInitiator {

	protected StrategyFacts facts;
	protected RulesController<?, ?> controller;

	protected InitiateRequest(final Agent agent, final StrategyFacts facts, final RulesController<?, ?> controller) {
		super(agent, facts.get(REQUEST_CREATE_MESSAGE));

		this.controller = controller;
		this.facts = facts;
	}

	/**
	 * Method creates behaviour
	 *
	 * @param agent      agent executing the behaviour
	 * @param facts      facts under which the Request message is to be created
	 * @param ruleType   type of the rule that handles Request execution
	 * @param controller rules controller
	 * @return InitiateRequest
	 */
	public static InitiateRequest create(final Agent agent, final StrategyFacts facts, final RuleType ruleType,
			final RulesController<?, ?> controller) {
		final StrategyFacts methodFacts = FactsMapper.mapToStrategyFacts(facts);
		methodFacts.put(RULE_TYPE, ruleType);
		methodFacts.put(RULE_STEP, REQUEST_CREATE_STEP);
		controller.fire(methodFacts);

		return new InitiateRequest(agent, methodFacts, controller);
	}

	/**
	 * Method handles INFORM message retrieved from the agent.
	 */
	@Override
	protected void handleInform(final ACLMessage inform) {
		facts.put(RULE_STEP, REQUEST_HANDLE_INFORM_STEP);
		facts.put(REQUEST_INFORM_MESSAGE, inform);
		controller.fire(facts);
		postProcessInform(facts);
	}

	/**
	 * Method handles REFUSE message retrieved from the agent.
	 */
	@Override
	protected void handleRefuse(final ACLMessage refuse) {
		facts.put(RULE_STEP, REQUEST_HANDLE_REFUSE_STEP);
		facts.put(REQUEST_REFUSE_MESSAGE, refuse);
		controller.fire(facts);
		postProcessRefuse(facts);
	}

	/**
	 * Method handles FAILURE message retrieved from the agent.
	 */
	@Override
	protected void handleFailure(final ACLMessage failure) {
		facts.put(RULE_STEP, REQUEST_HANDLE_FAILURE_STEP);
		facts.put(REQUEST_FAILURE_MESSAGE, failure);
		controller.fire(facts);
		postProcessFailure(facts);
	}

	@Override
	protected void handleAllResultNotifications(final Vector resultNotifications) {
		facts.put(RULE_STEP, REQUEST_HANDLE_ALL_RESULTS_STEP);
		facts.put(REQUEST_INFORM_RESULTS_MESSAGES, readForPerformative(resultNotifications, INFORM));
		facts.put(REQUEST_FAILURE_RESULTS_MESSAGES, readForPerformative(resultNotifications, FAILURE));
		controller.fire(facts);
		postProcessFailure(facts);
	}

	/**
	 * Method can be optionally overridden in order to perform facts-based actions after handling inform message
	 */
	protected void postProcessInform(final StrategyFacts facts) {
		// to be overridden if necessary
	}

	/**
	 * Method can be optionally overridden in order to perform facts-based actions after handling refuse message
	 */
	protected void postProcessRefuse(final StrategyFacts facts) {
		// to be overridden if necessary
	}

	/**
	 * Method can be optionally overridden in order to perform facts-based actions after handling failure message
	 */
	protected void postProcessFailure(final StrategyFacts facts) {
		// to be overridden if necessary
	}
}
