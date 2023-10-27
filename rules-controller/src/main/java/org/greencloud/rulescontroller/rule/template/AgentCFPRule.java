package org.greencloud.rulescontroller.rule.template;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
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
import static org.greencloud.rulescontroller.rule.AgentRuleType.CFP;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rest.domain.CallForProposalRuleRest;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.AgentRuleType;
import org.mvel2.MVEL;

import com.gui.agents.AgentNode;

import jade.lang.acl.ACLMessage;

/**
 * Abstract class defining structure of a rule which handles default Call For Proposal initiator behaviour
 */
public class AgentCFPRule<T extends AgentProps, E extends AgentNode<T>> extends AgentBasicRule<T, E> {

	private List<AgentRule> stepRules;

	private Serializable expressionCreateCFP;
	private Serializable expressionCompareProposals;
	private Serializable expressionHandleRejectProposal;
	private Serializable expressionHandleNoResponses;
	private Serializable expressionHandleNoProposals;
	private Serializable expressionHandleProposals;

	/**
	 * Constructor
	 *
	 * @param controller rules controller connected to the agent
	 */
	protected AgentCFPRule(final RulesController<T, E> controller) {
		super(controller);
		initializeSteps();
	}

	/**
	 * Constructor
	 *
	 * @param ruleRest rest representation of agent rule
	 */
	public AgentCFPRule(final CallForProposalRuleRest ruleRest) {
		super(ruleRest);
		if (nonNull(ruleRest.getCreateCFP())) {
			this.expressionCreateCFP = MVEL.compileExpression(
					imports + " " + ruleRest.getCreateCFP());
		}
		if (nonNull(ruleRest.getCompareProposals())) {
			this.expressionCompareProposals = MVEL.compileExpression(
					imports + " " + ruleRest.getCompareProposals());
		}
		if (nonNull(ruleRest.getHandleRejectProposal())) {
			this.expressionHandleRejectProposal = MVEL.compileExpression(
					imports + " " + ruleRest.getHandleRejectProposal());
		}
		if (nonNull(ruleRest.getHandleNoResponses())) {
			this.expressionHandleNoResponses = MVEL.compileExpression(
					imports + " " + ruleRest.getHandleNoResponses());
		}
		if (nonNull(ruleRest.getHandleNoProposals())) {
			this.expressionHandleNoProposals = MVEL.compileExpression(
					imports + " " + ruleRest.getHandleNoProposals());
		}
		if (nonNull(ruleRest.getHandleProposals())) {
			this.expressionHandleProposals = MVEL.compileExpression(
					imports + " " + ruleRest.getHandleProposals());
		}
		initializeSteps();
	}

	public void initializeSteps() {
		stepRules = new ArrayList<>(List.of(
				new CreateCFPRule(),
				new CompareCFPMessageRule(),
				new HandleRejectProposalRule(),
				new HandleNoProposalsRule(),
				new HandleNoResponsesRule(),
				new HandleProposalsRule()
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
		return CFP;
	}

	/**
	 * Method executed when CFP message is to be created
	 */
	protected ACLMessage createCFPMessage(final RuleSetFacts facts) {
		return null;
	}

	/**
	 * Method executed when new proposal is retrieved, and it is to be compared with existing best proposal
	 */
	protected int compareProposals(final RuleSetFacts facts, final ACLMessage bestProposal,
			final ACLMessage newProposal) {
		return 0;
	}

	/**
	 * Method executed when a proposal is to be rejected
	 */
	protected void handleRejectProposal(final ACLMessage proposalToReject, final RuleSetFacts facts) {

	}

	/**
	 * Method executed when agent received 0 responses
	 */
	protected void handleNoResponses(final RuleSetFacts facts) {
	}

	/**
	 * Method executed when agent received 0 proposals
	 */
	protected void handleNoProposals(final RuleSetFacts facts) {
	}

	/**
	 * Method executed when agent received some proposals
	 */
	protected void handleProposals(final ACLMessage bestProposal, final Collection<ACLMessage> allProposals,
			final RuleSetFacts facts) {
	}

	// RULE EXECUTED WHEN CFP MESSAGE IS TO BE CREATED
	class CreateCFPRule extends AgentBasicRule<T, E> {

		public CreateCFPRule() {
			super(AgentCFPRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public void executeRule(final RuleSetFacts facts) {
			if (nonNull(AgentCFPRule.this.initialParameters)) {
				AgentCFPRule.this.initialParameters.replace("facts", facts);
			}

			final ACLMessage cfp = isNull(expressionCreateCFP) ? createCFPMessage(facts)
					: (ACLMessage) MVEL.executeExpression(expressionCreateCFP, AgentCFPRule.this.initialParameters);
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
		public void executeRule(final RuleSetFacts facts) {
			final ACLMessage bestProposal = facts.get(CFP_BEST_MESSAGE);
			final ACLMessage newProposal = facts.get(CFP_NEW_PROPOSAL);

			if (nonNull(AgentCFPRule.this.initialParameters)) {
				AgentCFPRule.this.initialParameters.replace("facts", facts);
			}

			int result = 0;

			if (isNull(expressionCompareProposals)) {
				result = compareProposals(facts, bestProposal, newProposal);
			} else {
				AgentCFPRule.this.initialParameters.put("bestProposal", bestProposal);
				AgentCFPRule.this.initialParameters.put("newProposal", newProposal);
				result = (int) MVEL.executeExpression(expressionCompareProposals, AgentCFPRule.this.initialParameters);
				AgentCFPRule.this.initialParameters.remove("bestProposal");
				AgentCFPRule.this.initialParameters.remove("newProposal");
			}
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
		public void executeRule(final RuleSetFacts facts) {
			final ACLMessage proposalToReject = facts.get(CFP_REJECT_MESSAGE);
			if (nonNull(AgentCFPRule.this.initialParameters)) {
				AgentCFPRule.this.initialParameters.replace("facts", facts);
			}

			if (isNull(expressionHandleRejectProposal)) {
				handleRejectProposal(proposalToReject, facts);
			} else {
				AgentCFPRule.this.initialParameters.put("proposalToReject", proposalToReject);
				MVEL.executeExpression(expressionHandleRejectProposal, AgentCFPRule.this.initialParameters);
				AgentCFPRule.this.initialParameters.remove("proposalToReject");
			}
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
		public void executeRule(final RuleSetFacts facts) {
			if (nonNull(AgentCFPRule.this.initialParameters)) {
				AgentCFPRule.this.initialParameters.replace("facts", facts);
			}

			if (isNull(expressionHandleNoResponses)) {
				handleNoResponses(facts);
			} else {
				MVEL.executeExpression(expressionHandleNoResponses, AgentCFPRule.this.initialParameters);
			}
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
		public void executeRule(final RuleSetFacts facts) {
			if (nonNull(AgentCFPRule.this.initialParameters)) {
				AgentCFPRule.this.initialParameters.replace("facts", facts);
			}

			if (isNull(expressionHandleNoProposals)) {
				handleNoProposals(facts);
			} else {
				MVEL.executeExpression(expressionHandleNoProposals, AgentCFPRule.this.initialParameters);
			}
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
		public void executeRule(final RuleSetFacts facts) {
			final ACLMessage bestProposal = facts.get(CFP_BEST_MESSAGE);
			final Collection<ACLMessage> allProposals = facts.get(CFP_RECEIVED_PROPOSALS);
			if (nonNull(AgentCFPRule.this.initialParameters)) {
				AgentCFPRule.this.initialParameters.replace("facts", facts);
			}

			if (isNull(expressionHandleProposals)) {
				handleProposals(bestProposal, allProposals, facts);
			} else {
				AgentCFPRule.this.initialParameters.put("bestProposal", bestProposal);
				AgentCFPRule.this.initialParameters.put("allProposals", allProposals);
				MVEL.executeExpression(expressionHandleProposals, AgentCFPRule.this.initialParameters);
				AgentCFPRule.this.initialParameters.remove("bestProposal");
				AgentCFPRule.this.initialParameters.remove("allProposals");
			}
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentCFPRule.this.ruleType, CFP_HANDLE_SELECTED_PROPOSAL_STEP,
					format("%s - handle proposals", AgentCFPRule.this.name),
					"rule executed when there are some proposals to CFP");
		}
	}

}
