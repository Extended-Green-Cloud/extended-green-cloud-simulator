package org.greencloud.rulescontroller.rule.template;

import static org.greencloud.commons.constants.FactTypeConstants.PROPOSAL_ACCEPT_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.PROPOSAL_CREATE_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.PROPOSAL_REJECT_MESSAGE;
import static org.greencloud.commons.enums.rules.RuleStepType.PROPOSAL_CREATE_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.PROPOSAL_HANDLE_ACCEPT_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.PROPOSAL_HANDLE_REJECT_STEP;
import static java.lang.String.format;
import static org.greencloud.rulescontroller.rule.AgentRuleType.PROPOSAL;

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
 * Abstract class defining structure of a rule which handles default Proposal initiator behaviour
 */
public abstract class AgentProposalRule<T extends AgentProps, E extends AbstractNode<?, T>>
		extends AgentBasicRule<T, E> {

	/**
	 * Constructor
	 *
	 * @param controller rules controller connected to the agent
	 */
	protected AgentProposalRule(final RulesController<T, E> controller) {
		super(controller);
	}

	@Override
	public AgentRuleType getAgentRuleType() {
		return PROPOSAL;
	}

	@Override
	public List<AgentRule> getRules() {
		return List.of(new CreateProposalMessageRule(), new HandleAcceptProposalRule(), new HandleRejectProposalRule());
	}

	/**
	 * Method executed when proposal message is to be created
	 */
	protected abstract ACLMessage createProposalMessage(final StrategyFacts facts);

	/**
	 * Method executed when ACCEPT_PROPOSAL message is to be handled
	 */
	protected abstract void handleAcceptProposal(final ACLMessage accept, final StrategyFacts facts);

	/**
	 * Method executed when REJECT_PROPOSAL message is to be handled
	 */
	protected abstract void handleRejectProposal(final ACLMessage reject, final StrategyFacts facts);

	// RULE EXECUTED WHEN PROPOSAL MESSAGE IS TO BE CREATED
	class CreateProposalMessageRule extends AgentBasicRule<T, E> {

		public CreateProposalMessageRule() {
			super(AgentProposalRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public void executeRule(final StrategyFacts facts) {
			final ACLMessage proposal = createProposalMessage(facts);
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
		public void executeRule(final StrategyFacts facts) {
			final ACLMessage acceptMessage = facts.get(PROPOSAL_ACCEPT_MESSAGE);
			handleAcceptProposal(acceptMessage, facts);
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
		public void executeRule(final StrategyFacts facts) {
			final ACLMessage rejectMessage = facts.get(PROPOSAL_REJECT_MESSAGE);
			handleRejectProposal(rejectMessage, facts);
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentProposalRule.this.ruleType, PROPOSAL_HANDLE_REJECT_STEP,
					format("%s - handle reject proposal", AgentProposalRule.this.name),
					"rule that handles case when REJECT_PROPOSAL message is received");
		}
	}

}
