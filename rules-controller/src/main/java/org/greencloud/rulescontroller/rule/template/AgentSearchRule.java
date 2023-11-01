package org.greencloud.rulescontroller.rule.template;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.enums.rules.RuleStepType.SEARCH_AGENTS_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.SEARCH_HANDLE_NO_RESULTS_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.SEARCH_HANDLE_RESULTS_STEP;
import static org.greencloud.rulescontroller.rule.AgentRuleType.SEARCH;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.gui.agents.AgentNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rest.domain.SearchRuleRest;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.AgentRuleType;
import org.mvel2.MVEL;

import jade.core.AID;

/**
 * Abstract class defining structure of a rule which handles default DF search behaviour
 */
public class AgentSearchRule<T extends AgentProps, E extends AgentNode<T>> extends AgentBasicRule<T, E> {

	private List<AgentRule> stepRules;
	private Serializable expressionSearchAgents;
	private Serializable expressionHandleNoResults;
	private Serializable expressionHandleResults;

	/**
	 * Constructor
	 *
	 * @param controller rules controller connected to the agent
	 */
	protected AgentSearchRule(final RulesController<T, E> controller) {
		super(controller);
		initializeSteps();
	}

	/**
	 * Constructor
	 *
	 * @param ruleRest rest representation of agent rule
	 */
	public AgentSearchRule(final SearchRuleRest ruleRest) {
		super(ruleRest);
		if (nonNull(ruleRest.getSearchAgents())) {
			this.expressionSearchAgents = MVEL.compileExpression(
					imports + " " + ruleRest.getSearchAgents());
		}
		if (nonNull(ruleRest.getHandleNoResults())) {
			this.expressionHandleNoResults = MVEL.compileExpression(imports + " " + ruleRest.getHandleNoResults());
		}
		if (nonNull(ruleRest.getHandleResults())) {
			this.expressionHandleResults = MVEL.compileExpression(
					imports + " " + ruleRest.getHandleResults());
		}
		initializeSteps();
	}

	public void initializeSteps() {
		stepRules = new ArrayList<>(List.of(new SearchForAgentsRule(), new NoResultsRule(), new AgentsFoundRule()));
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
		return SEARCH;
	}

	/**
	 * Method searches for the agents in DF
	 */
	protected Set<AID> searchAgents(final RuleSetFacts facts) {
		return new HashSet<>();
	}

	/**
	 * Method executed when DF retrieved no results
	 */
	protected void handleNoResults(final RuleSetFacts facts) {
	}

	/**
	 * Method executed when DF retrieved results
	 */
	protected void handleResults(final Set<AID> dfResults, final RuleSetFacts facts) {
	}

	// RULE EXECUTED WHEN DF IS TO BE SEARCHED
	class SearchForAgentsRule extends AgentBasicRule<T, E> {

		public SearchForAgentsRule() {
			super(AgentSearchRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public void executeRule(final RuleSetFacts facts) {
			if (nonNull(AgentSearchRule.this.initialParameters)) {
				AgentSearchRule.this.initialParameters.replace("facts", facts);
			}

			final Set<AID> result = isNull(expressionSearchAgents) ?
					searchAgents(facts) :
					(Set<AID>) MVEL.executeExpression(expressionSearchAgents, AgentSearchRule.this.initialParameters);
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
		public void executeRule(final RuleSetFacts facts) {
			if (nonNull(AgentSearchRule.this.initialParameters)) {
				AgentSearchRule.this.initialParameters.replace("facts", facts);
			}

			if (isNull(expressionHandleNoResults)) {
				handleNoResults(facts);
			} else {
				MVEL.executeExpression(expressionHandleNoResults, AgentSearchRule.this.initialParameters);
			}
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
		public void executeRule(final RuleSetFacts facts) {
			if (nonNull(AgentSearchRule.this.initialParameters)) {
				AgentSearchRule.this.initialParameters.replace("facts", facts);
			}

			final Set<AID> agents = facts.get(RESULT);

			if (isNull(expressionHandleResults)) {
				handleResults(agents, facts);
			} else {
				AgentSearchRule.this.initialParameters.put("agents", agents);
				MVEL.executeExpression(expressionHandleResults, AgentSearchRule.this.initialParameters);
				AgentSearchRule.this.initialParameters.remove("agents");
			}
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentSearchRule.this.ruleType, SEARCH_HANDLE_RESULTS_STEP,
					format("%s - agents found", AgentSearchRule.this.name),
					"rule triggerred when DF returned set of agents");
		}
	}

}
