package org.greencloud.rulescontroller.rule.template;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.FactTypeConstants.PROPOSAL_ACCEPT_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.PROPOSAL_CREATE_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.PROPOSAL_REJECT_MESSAGE;
import static org.greencloud.commons.enums.rules.RuleStepType.PROPOSAL_CREATE_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.PROPOSAL_HANDLE_ACCEPT_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.PROPOSAL_HANDLE_REJECT_STEP;
import static org.greencloud.rulescontroller.rule.AgentRuleType.PROPOSAL;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rest.domain.ProposalRuleRest;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.AgentRuleType;
import org.mvel2.MVEL;

import com.gui.agents.AgentNode;

import jade.lang.acl.ACLMessage;

/**
 * Abstract class defining structure of a rule which handles default Proposal initiator behaviour
 */
public class AgentProposalRule<T extends AgentProps, E extends AgentNode<T>> extends AgentBasicRule<T, E> {

	private List<AgentRule> stepRules;
	private Serializable expressionCreateProposal;
	private Serializable expressionHandleAcceptProposal;
	private Serializable expressionHandleRejectProposal;

	/**
	 * Constructor
	 *
	 * @param controller rules controller connected to the agent
	 */
	protected AgentProposalRule(final RulesController<T, E> controller) {
		super(controller);
		initializeSteps();
	}

	/**
	 * Constructor
	 *
	 * @param ruleRest rest representation of agent rule
	 */
	public AgentProposalRule(final ProposalRuleRest ruleRest) {
		super(ruleRest);
		if (nonNull(ruleRest.getCreateProposalMessage())) {
			this.expressionCreateProposal = MVEL.compileExpression(
					imports + " " + ruleRest.getCreateProposalMessage());
		}
		if (nonNull(ruleRest.getHandleAcceptProposal())) {
			this.expressionHandleAcceptProposal = MVEL.compileExpression(
					imports + " " + ruleRest.getHandleAcceptProposal());
		}
		if (nonNull(ruleRest.getHandleRejectProposal())) {
			this.expressionHandleRejectProposal = MVEL.compileExpression(
					imports + " " + ruleRest.getHandleRejectProposal());
		}
		initializeSteps();
	}

	public void initializeSteps() {
		stepRules = new ArrayList<>(List.of(new CreateProposalMessageRule(), new HandleAcceptProposalRule(),
				new HandleRejectProposalRule()));
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
		return PROPOSAL;
	}


	/**
	 * Method executed when proposal message is to be created
	 */
	protected ACLMessage createProposalMessage(final RuleSetFacts facts) {
		return null;
	}

	/**
	 * Method executed when ACCEPT_PROPOSAL message is to be handled
	 */
	protected void handleAcceptProposal(final ACLMessage accept, final RuleSetFacts facts) {
	}

	/**
	 * Method executed when REJECT_PROPOSAL message is to be handled
	 */
	protected void handleRejectProposal(final ACLMessage reject, final RuleSetFacts facts) {
	}

	// RULE EXECUTED WHEN PROPOSAL MESSAGE IS TO BE CREATED
	class CreateProposalMessageRule extends AgentBasicRule<T, E> {

		public CreateProposalMessageRule() {
			super(AgentProposalRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public void executeRule(final RuleSetFacts facts) {
			if (nonNull(AgentProposalRule.this.initialParameters)) {
				AgentProposalRule.this.initialParameters.replace("facts", facts);
			}
			final ACLMessage proposal = isNull(expressionCreateProposal) ?
					createProposalMessage(facts) :
					(ACLMessage) MVEL.executeExpression(expressionCreateProposal,
							AgentProposalRule.this.initialParameters);
			facts.put(PROPOSAL_CREATE_MESSAGE, proposal);
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentProposalRule.this.ruleType, PROPOSAL_CREATE_STEP,
					format("%s - create proposal message", AgentProposalRule.this.name),
					"rule performed when proposal message sent to other agents is to be created");
		}
	}

	// RULE EXECUTED WHEN ACCEPT_PROPOSAL MESSAGE IS RECEIVED
	class HandleAcceptProposalRule extends AgentBasicRule<T, E> {

		public HandleAcceptProposalRule() {
			super(AgentProposalRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public void executeRule(final RuleSetFacts facts) {
			final ACLMessage acceptMessage = facts.get(PROPOSAL_ACCEPT_MESSAGE);
			if (nonNull(AgentProposalRule.this.initialParameters)) {
				AgentProposalRule.this.initialParameters.replace("facts", facts);
			}

			if (isNull(expressionHandleAcceptProposal)) {
				handleAcceptProposal(acceptMessage, facts);
			} else {
				AgentProposalRule.this.initialParameters.put("acceptMessage", acceptMessage);
				MVEL.executeExpression(expressionHandleAcceptProposal, AgentProposalRule.this.initialParameters);
				AgentProposalRule.this.initialParameters.remove("acceptMessage");
			}
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentProposalRule.this.ruleType, PROPOSAL_HANDLE_ACCEPT_STEP,
					format("%s - handle accept proposal", AgentProposalRule.this.name),
					"rule that handles case when ACCEPT_PROPOSAL message is received");
		}
	}

	// RULE EXECUTED WHEN ACCEPT_PROPOSAL MESSAGE IS RECEIVED
	class HandleRejectProposalRule extends AgentBasicRule<T, E> {

		public HandleRejectProposalRule() {
			super(AgentProposalRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public void executeRule(final RuleSetFacts facts) {
			final ACLMessage rejectMessage = facts.get(PROPOSAL_REJECT_MESSAGE);
			if (nonNull(AgentProposalRule.this.initialParameters)) {
				AgentProposalRule.this.initialParameters.replace("facts", facts);
			}

			if (isNull(expressionHandleRejectProposal)) {
				handleRejectProposal(rejectMessage, facts);
			} else {
				AgentProposalRule.this.initialParameters.put("rejectMessage", rejectMessage);
				MVEL.executeExpression(expressionHandleRejectProposal, AgentProposalRule.this.initialParameters);
				AgentProposalRule.this.initialParameters.remove("rejectMessage");
			}
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentProposalRule.this.ruleType, PROPOSAL_HANDLE_REJECT_STEP,
					format("%s - handle reject proposal", AgentProposalRule.this.name),
					"rule that handles case when REJECT_PROPOSAL message is received");
		}
	}

}
