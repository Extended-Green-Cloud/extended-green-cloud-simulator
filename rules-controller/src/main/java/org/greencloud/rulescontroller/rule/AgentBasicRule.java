package org.greencloud.rulescontroller.rule;

import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.rulescontroller.rule.AgentRuleType.BASIC;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.core.BasicRule;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.enums.rules.RuleType;
import org.greencloud.commons.enums.rules.RuleStepType;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.AbstractNode;

import jade.core.Agent;
import lombok.Getter;

/**
 * Abstract class defining structure of a rule used in given system strategy
 */
@Getter
public abstract class AgentBasicRule<T extends AgentProps, E extends AbstractNode<?, T>> extends BasicRule
		implements AgentRule {

	protected boolean isRuleStep;
	protected RulesController<T, E> controller;
	protected T agentProps;
	protected E agentNode;
	protected Agent agent;
	protected RuleType ruleType;
	protected RuleType subRuleType;
	protected RuleStepType stepType;

	/**
	 * Constructor
	 *
	 * @param rulesController rules controller connected to the agent
	 */
	protected AgentBasicRule(final RulesController<T, E> rulesController) {
		this.agent = rulesController.getAgent();
		this.agentProps = rulesController.getAgentProps();
		this.agentNode = rulesController.getAgentNode();
		this.controller = rulesController;
		this.isRuleStep = false;

		final AgentRuleDescription ruleDescription = initializeRuleDescription();
		this.name = ruleDescription.ruleName();
		this.stepType = ruleDescription.stepType();
		this.description = ruleDescription.ruleDescription();
		this.ruleType = ruleDescription.ruleType();
		this.subRuleType = ruleDescription.subType();
	}

	/**
	 * Constructor
	 *
	 * @param rulesController rules controller connected to the agent
	 * @param priority        priority of the rule execution
	 */
	protected AgentBasicRule(final RulesController<T, E> rulesController, final int priority) {
		this(rulesController);
		this.priority = priority;
	}

	@Override
	public AgentRuleType getAgentRuleType() {
		return BASIC;
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		return ruleType.equals(facts.get(RULE_TYPE));
	}

	@Override
	public boolean evaluate(final Facts facts) {
		return evaluateRule((StrategyFacts) facts);
	}

	@Override
	public void execute(final Facts facts) throws Exception {
		executeRule((StrategyFacts) facts);
	}
}
