package org.greencloud.rulescontroller.rule.template;

import static org.greencloud.commons.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.enums.rules.RuleStepType.SEARCH_AGENTS_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.SEARCH_HANDLE_NO_RESULTS_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.SEARCH_HANDLE_RESULTS_STEP;
import static java.lang.String.format;
import static org.greencloud.rulescontroller.rule.AgentRuleType.SEARCH;

import java.util.List;
import java.util.Set;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.AgentRuleType;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.AbstractNode;

import jade.core.AID;

/**
 * Abstract class defining structure of a rule which handles default DF search behaviour
 */
public abstract class AgentSearchRule<T extends AgentProps, E extends AbstractNode<?, T>> extends AgentBasicRule<T, E> {

	/**
	 * Constructor
	 *
	 * @param controller rules controller connected to the agent
	 */
	protected AgentSearchRule(final RulesController<T, E> controller) {
		super(controller);
	}

	@Override
	public AgentRuleType getAgentRuleType() {
		return SEARCH;
	}

	@Override
	public List<AgentRule> getRules() {
		return List.of(new SearchForAgentsRule(), new NoResultsRule(), new AgentsFoundRule());
	}

	/**
	 * Method which can be optionally overridden in order to read common fact objects
	 */
	protected void readConstantFacts(final StrategyFacts facts) {
	}

	/**
	 * Method searches for the agents in DF
	 */
	protected abstract Set<AID> searchAgents(final StrategyFacts facts);

	/**
	 * Method executed when DF retrieved no results
	 */
	protected abstract void handleNoResults(final StrategyFacts facts);

	/**
	 * Method executed when DF retrieved results
	 */
	protected abstract void handleResults(final Set<AID> dfResults, final StrategyFacts facts);

	// RULE EXECUTED WHEN DF IS TO BE SEARCHED
	class SearchForAgentsRule extends AgentBasicRule<T, E> {

		public SearchForAgentsRule() {
			super(AgentSearchRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public void executeRule(final StrategyFacts facts) {
			readConstantFacts(facts);
			final Set<AID> result = searchAgents(facts);
			facts.put(RESULT, result);
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentSearchRule.this.ruleType, SEARCH_AGENTS_STEP,
					format("%s - search for agents", AgentSearchRule.this.name),
					"rule performed when searching for agents in DF");
		}
	}

	// RULE EXECUTED WHEN DF RETURNED EMPTY RESULT LIST
	class NoResultsRule extends AgentBasicRule<T, E> {

		public NoResultsRule() {
			super(AgentSearchRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public void executeRule(final StrategyFacts facts) {
			handleNoResults(facts);
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentSearchRule.this.ruleType, SEARCH_HANDLE_NO_RESULTS_STEP,
					format("%s - no results", AgentSearchRule.this.name),
					"rule that handles case when no DF results were retrieved");
		}
	}

	// RULE EXECUTED WHEN DF RETURNED SET OF AGENTS
	class AgentsFoundRule extends AgentBasicRule<T, E> {

		public AgentsFoundRule() {
			super(AgentSearchRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public void executeRule(final StrategyFacts facts) {
			final Set<AID> agents = facts.get(RESULT);
			handleResults(agents, facts);
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentSearchRule.this.ruleType, SEARCH_HANDLE_RESULTS_STEP,
					format("%s - agents found", AgentSearchRule.this.name),
					"rule triggerred when DF returned set of agents");
		}
	}

}
