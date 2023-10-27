package org.greencloud.rulescontroller.behaviour.initiate;

import static org.greencloud.commons.constants.FactTypeConstants.PROPOSAL_ACCEPT_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.PROPOSAL_CREATE_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.PROPOSAL_REJECT_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_STEP;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.enums.rules.RuleStepType.PROPOSAL_CREATE_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.PROPOSAL_HANDLE_ACCEPT_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.PROPOSAL_HANDLE_REJECT_STEP;

import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.commons.mapper.FactsMapper;
import org.greencloud.rulescontroller.RulesController;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ProposeInitiator;

/**
 * Abstract behaviour providing template initiating Proposal protocol handled with rules
 */
public class InitiateProposal extends ProposeInitiator {

	protected StrategyFacts facts;
	protected RulesController<?, ?> controller;

	protected InitiateProposal(final Agent agent, final StrategyFacts facts, final RulesController<?, ?> controller) {
		super(agent, facts.get(PROPOSAL_CREATE_MESSAGE));

		this.controller = controller;
		this.facts = facts;
	}

	/**
	 * Method creates behaviour
	 *
	 * @param agent      agent executing the behaviour
	 * @param facts      facts under which the Proposal message is to be created
	 * @param ruleType   type of the rule that handles Proposal execution
	 * @param controller rules controller
	 * @return InitiateProposal
	 */
	public static InitiateProposal create(final Agent agent, final StrategyFacts facts, final String ruleType,
			final RulesController<?, ?> controller) {
		final StrategyFacts methodFacts = FactsMapper.mapToStrategyFacts(facts);
		methodFacts.put(RULE_TYPE, ruleType);
		methodFacts.put(RULE_STEP, PROPOSAL_CREATE_STEP);
		controller.fire(methodFacts);

		return new InitiateProposal(agent, methodFacts, controller);
	}

	/**
	 * Method handles ACCEPT_PROPOSAL message retrieved from the agent.
	 */
	@Override
	protected void handleAcceptProposal(final ACLMessage accept) {
		facts.put(RULE_STEP, PROPOSAL_HANDLE_ACCEPT_STEP);
		facts.put(PROPOSAL_ACCEPT_MESSAGE, accept);
		controller.fire(facts);
		postProcessAcceptProposal(facts);
	}

	/**
	 * Method handles REJECT_PROPOSAL message retrieved from the agent.
	 */
	@Override
	protected void handleRejectProposal(final ACLMessage reject) {
		facts.put(RULE_STEP, PROPOSAL_HANDLE_REJECT_STEP);
		facts.put(PROPOSAL_REJECT_MESSAGE, reject);
		controller.fire(facts);
		postProcessRejectProposal(facts);
	}

	/**
	 * Method can be optionally overridden in order to perform facts-based actions after handling acceptance message
	 */
	protected void postProcessAcceptProposal(final StrategyFacts facts) {
		// to be overridden if necessary
	}

	/**
	 * Method can be optionally overridden in order to perform facts-based actions after handling rejection message
	 */
	protected void postProcessRejectProposal(final StrategyFacts facts) {
		// to be overridden if necessary
	}

}
