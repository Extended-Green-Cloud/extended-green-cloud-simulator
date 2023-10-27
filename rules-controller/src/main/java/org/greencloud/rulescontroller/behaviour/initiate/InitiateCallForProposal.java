package org.greencloud.rulescontroller.behaviour.initiate;

import static jade.lang.acl.ACLMessage.PROPOSE;
import static java.util.Objects.isNull;
import static org.greencloud.commons.constants.FactTypeConstants.CFP_BEST_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.CFP_CREATE_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.CFP_NEW_PROPOSAL;
import static org.greencloud.commons.constants.FactTypeConstants.CFP_RECEIVED_PROPOSALS;
import static org.greencloud.commons.constants.FactTypeConstants.CFP_REJECT_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.CFP_RESULT;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_STEP;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.enums.rules.RuleStepType.CFP_COMPARE_MESSAGES_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.CFP_CREATE_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.CFP_HANDLE_NO_AVAILABLE_AGENTS_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.CFP_HANDLE_NO_RESPONSES_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.CFP_HANDLE_REJECT_PROPOSAL_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.CFP_HANDLE_SELECTED_PROPOSAL_STEP;
import static org.greencloud.commons.utils.messaging.MessageReader.readForPerformative;

import java.util.Vector;

import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.commons.mapper.FactsMapper;
import org.greencloud.rulescontroller.RulesController;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

/**
 * Abstract behaviour providing template initiating Call For Proposal protocol handled with rules
 */
public class InitiateCallForProposal extends ContractNetInitiator {

	protected ACLMessage bestProposal;
	protected StrategyFacts facts;
	protected RulesController<?, ?> controller;

	protected InitiateCallForProposal(final Agent agent, final StrategyFacts facts,
			final RulesController<?, ?> controller) {
		super(agent, facts.get(CFP_CREATE_MESSAGE));

		this.controller = controller;
		this.facts = facts;
	}

	/**
	 * Method creates behaviour
	 *
	 * @param agent      agent executing the behaviour
	 * @param facts      facts under which the CFP message is to be created
	 * @param ruleType   type of the rule that handles CFP execution
	 * @param controller rules controller
	 * @return InitiateCallForProposal
	 */
	public static InitiateCallForProposal create(final Agent agent, final StrategyFacts facts, final String ruleType,
			final RulesController<?, ?> controller) {
		final StrategyFacts methodFacts = FactsMapper.mapToStrategyFacts(facts);
		methodFacts.put(RULE_TYPE, ruleType);
		methodFacts.put(RULE_STEP, CFP_CREATE_STEP);
		controller.fire(methodFacts);

		return new InitiateCallForProposal(agent, methodFacts, controller);
	}

	/**
	 * Generic way of handling PROPOSE response.
	 * Method verifies if newly received proposal is better than the current best one and, upon that, updates
	 * the information regarding the best proposal and executes handler for rejected proposal.
	 *
	 * @param propose     received proposal
	 * @param acceptances vector containing accept proposal message sent back to the chosen server (not used)
	 */
	@Override
	protected void handlePropose(final ACLMessage propose, final Vector acceptances) {
		if (isNull(bestProposal)) {
			bestProposal = propose;
			facts.put(CFP_BEST_MESSAGE, bestProposal);
			return;
		}

		facts.put(RULE_STEP, CFP_COMPARE_MESSAGES_STEP);
		facts.put(CFP_BEST_MESSAGE, bestProposal);
		facts.put(CFP_NEW_PROPOSAL, propose);
		controller.fire(facts);

		final int comparisonResult = facts.get(CFP_RESULT);
		final ACLMessage proposalToReject = comparisonResult < 0 ? (ACLMessage) bestProposal.clone() : propose;

		facts.put(RULE_STEP, CFP_HANDLE_REJECT_PROPOSAL_STEP);
		facts.put(CFP_REJECT_MESSAGE, proposalToReject);
		controller.fire(facts);

		if (comparisonResult < 0) {
			bestProposal = propose;
		}
		postProcessProposal(facts);
	}

	/**
	 * Generic way of handling all received responses.
	 * Method handles 3 cases:
	 * <p> 1) case when no responses were retrieved </p>
	 * <p> 2) case when all agents refused to provide a given service </p>
	 * <p> 3) case when there exists best proposal message </p>
	 * <p> In order to handle the aforementioned cases the method uses specific rule steps and fires agent strategy. </p>
	 *
	 * @param responses   retrieved responses
	 * @param acceptances vector containing accept proposal message sent back to the chosen server (not used)
	 */
	@Override
	protected void handleAllResponses(final Vector responses, final Vector acceptances) {
		if (responses.isEmpty()) {
			facts.put(RULE_STEP, CFP_HANDLE_NO_RESPONSES_STEP);
		} else if (isNull(bestProposal)) {
			facts.put(RULE_STEP, CFP_HANDLE_NO_AVAILABLE_AGENTS_STEP);
		} else {
			facts.put(CFP_BEST_MESSAGE, bestProposal);
			facts.put(CFP_RECEIVED_PROPOSALS, readForPerformative(responses, PROPOSE));
			facts.put(RULE_STEP, CFP_HANDLE_SELECTED_PROPOSAL_STEP);
		}
		controller.fire(facts);
		postProcessAllResponses(facts);
		myAgent.removeBehaviour(this);
	}

	/**
	 * Method can be optionally overridden in order to perform facts-based actions after handling all responses
	 */
	protected void postProcessAllResponses(final StrategyFacts facts) {
		// to be overridden if necessary
	}

	/**
	 * Method can be optionally overridden in order to perform facts-based actions after handling single proposal message
	 */
	protected void postProcessProposal(final StrategyFacts facts) {
		// to be overridden if necessary
	}

}
