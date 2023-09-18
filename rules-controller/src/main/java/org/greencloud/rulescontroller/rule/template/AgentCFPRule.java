package org.greencloud.rulescontroller.rule.template;

import static org.greencloud.commons.constants.FactTypeConstants.CFP_BEST_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.CFP_CREATE_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.CFP_NEW_PROPOSAL;
import static org.greencloud.commons.constants.FactTypeConstants.CFP_RECEIVED_PROPOSALS;
import static org.greencloud.commons.constants.FactTypeConstants.CFP_REJECT_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.CFP_RESULT;
import static org.greencloud.commons.enums.rules.RuleStepType.CFP_COMPARE_MESSAGES_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.CFP_CREATE_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.CFP_HANDLE_NO_AVAILABLE_AGENTS_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.CFP_HANDLE_NO_RESPONSES_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.CFP_HANDLE_REJECT_PROPOSAL_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.CFP_HANDLE_SELECTED_PROPOSAL_STEP;
import static java.lang.String.format;
import static org.greencloud.rulescontroller.rule.AgentRuleType.CFP;

import java.util.Collection;
import java.util.List;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.AgentRuleType;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.AbstractNode;

import jade.lang.acl.ACLMessage;

/**
 * Abstract class defining structure of a rule which handles default Call For Proposal initiator behaviour
 */
public abstract class AgentCFPRule<T extends AgentProps, E extends AbstractNode<?, T>> extends AgentBasicRule<T, E> {

	/**
	 * Constructor
	 *
	 * @param controller rules controller connected to the agent
	 */
	protected AgentCFPRule(final RulesController<T, E> controller) {
		super(controller);
	}

	@Override
	public List<AgentRule> getRules() {
		return List.of(
				new CreateCFPRule(),
				new CompareCFPMessageRule(),
				new HandleRejectProposalRule(),
				new HandleNoProposalsRule(),
				new HandleNoResponsesRule(),
				new HandleProposalsRule()
		);
	}

	@Override
	public AgentRuleType getAgentRuleType() {
		return CFP;
	}

	/**
	 * Method executed when CFP message is to be created
	 */
	protected abstract ACLMessage createCFPMessage(final StrategyFacts facts);

	/**
	 * Method executed when new proposal is retrieved, and it is to be compared with existing best proposal
	 */
	protected abstract int compareProposals(final ACLMessage bestProposal, final ACLMessage newProposal);

	/**
	 * Method executed when a proposal is to be rejected
	 */
	protected abstract void handleRejectProposal(final ACLMessage proposalToReject, final StrategyFacts facts);

	/**
	 * Method executed when agent received 0 responses
	 */
	protected abstract void handleNoResponses(final StrategyFacts facts);

	/**
	 * Method executed when agent received 0 proposals
	 */
	protected abstract void handleNoProposals(final StrategyFacts facts);

	/**
	 * Method executed when agent received some proposals
	 */
	protected abstract void handleProposals(final ACLMessage bestProposal, final Collection<ACLMessage> allProposals,
			final StrategyFacts facts);

	// RULE EXECUTED WHEN CFP MESSAGE IS TO BE CREATED
	class CreateCFPRule extends AgentBasicRule<T, E> {

		public CreateCFPRule() {
			super(AgentCFPRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public void executeRule(final StrategyFacts facts) {
			final ACLMessage cfp = createCFPMessage(facts);
			facts.put(CFP_CREATE_MESSAGE, cfp);
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentCFPRule.this.ruleType, CFP_CREATE_STEP,
					format("%s - create CFP message", AgentCFPRule.this.name),
					"when agent initiate CNA lookup, it creates CFP");
		}
	}

	// RULE EXECUTED WHEN TWO PROPOSALS ARE TO BE COMPARED
	class CompareCFPMessageRule extends AgentBasicRule<T, E> {

		public CompareCFPMessageRule() {
			super(AgentCFPRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public void executeRule(final StrategyFacts facts) {
			final ACLMessage bestProposal = facts.get(CFP_BEST_MESSAGE);
			final ACLMessage newProposal = facts.get(CFP_NEW_PROPOSAL);
			final int result = compareProposals(bestProposal, newProposal);

			facts.put(CFP_RESULT, result);
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentCFPRule.this.ruleType, CFP_COMPARE_MESSAGES_STEP,
					format("%s - compare received proposal message", AgentCFPRule.this.name),
					"when agent receives new proposal message, it compares it with current best proposal");
		}
	}

	// RULE EXECUTED WHEN AGENT REJECTS PROPOSAL RESPONSE
	class HandleRejectProposalRule extends AgentBasicRule<T, E> {

		public HandleRejectProposalRule() {
			super(AgentCFPRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public void executeRule(final StrategyFacts facts) {
			final ACLMessage proposalToReject = facts.get(CFP_REJECT_MESSAGE);
			handleRejectProposal(proposalToReject, facts);
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentCFPRule.this.ruleType, CFP_HANDLE_REJECT_PROPOSAL_STEP,
					format("%s - reject received proposal", AgentCFPRule.this.name),
					"rule executed when received proposal is to be rejected");
		}
	}

	// RULE EXECUTED WHEN NO RESPONSES WERE RECEIVED
	class HandleNoResponsesRule extends AgentBasicRule<T, E> {

		public HandleNoResponsesRule() {
			super(AgentCFPRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public void executeRule(final StrategyFacts facts) {
			handleNoResponses(facts);
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentCFPRule.this.ruleType, CFP_HANDLE_NO_RESPONSES_STEP,
					format("%s - no responses received", AgentCFPRule.this.name),
					"rule executed when there are 0 responses to CFP");
		}
	}

	// RULE EXECUTED WHEN THERE ARE NO PROPOSALS
	class HandleNoProposalsRule extends AgentBasicRule<T, E> {

		public HandleNoProposalsRule() {
			super(AgentCFPRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public void executeRule(final StrategyFacts facts) {
			handleNoProposals(facts);
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentCFPRule.this.ruleType, CFP_HANDLE_NO_AVAILABLE_AGENTS_STEP,
					format("%s - no proposals received", AgentCFPRule.this.name),
					"rule executed when there are 0 proposals to CFP");
		}
	}

	// RULE EXECUTED WHEN THERE ARE PROPOSALS
	class HandleProposalsRule extends AgentBasicRule<T, E> {

		public HandleProposalsRule() {
			super(AgentCFPRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public void executeRule(final StrategyFacts facts) {
			final ACLMessage bestProposal = facts.get(CFP_BEST_MESSAGE);
			final Collection<ACLMessage> allProposals = facts.get(CFP_RECEIVED_PROPOSALS);
			handleProposals(bestProposal, allProposals, facts);
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentCFPRule.this.ruleType, CFP_HANDLE_SELECTED_PROPOSAL_STEP,
					format("%s - handle proposals", AgentCFPRule.this.name),
					"rule executed when there are some proposals to CFP");
		}
	}

}
