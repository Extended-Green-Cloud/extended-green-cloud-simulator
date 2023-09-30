package org.greencloud.rulescontroller.rule.simple;

import static java.util.Objects.isNull;
import static org.greencloud.rulescontroller.rule.AgentRuleType.CHAIN;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.rest.domain.RuleRest;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.rule.AgentRuleType;
import org.greencloud.rulescontroller.strategy.Strategy;
import org.jeasy.rules.api.Facts;
import org.mvel2.MVEL;

import com.gui.agents.AgentNode;

import lombok.Getter;

/**
 * Abstract class defining structure of a rule which after successful execution, triggers once again strategy on
 * the updated set of facts
 */
@Getter
public class AgentChainRule<T extends AgentProps, E extends AgentNode<T>> extends AgentBasicRule<T, E> {

	private final Strategy strategy;

	/**
	 * Constructor
	 *
	 * @param controller rules controller connected to the agent
	 * @param priority   priority of the rule execution
	 * @param strategy   currently executed strategy
	 */
	protected AgentChainRule(final RulesController<T, E> controller, final int priority, final Strategy strategy) {
		super(controller, priority);
		this.strategy = strategy;
	}

	/**
	 * Constructor
	 *
	 * @param controller rules controller connected to the agent
	 * @param strategy   currently executed strategy
	 */
	protected AgentChainRule(final RulesController<T, E> controller, final Strategy strategy) {
		super(controller);
		this.strategy = strategy;
	}

	/**
	 * Constructor
	 *
	 * @param ruleRest rest representation of agent rule
	 * @param strategy currently executed strategy
	 */
	public AgentChainRule(final RuleRest ruleRest, final Strategy strategy) {
		super(ruleRest);
		this.strategy = strategy;
	}

	@Override
	public void execute(final Facts facts) throws Exception {
		if (isNull(executeExpression)) {
			this.executeRule((StrategyFacts) facts);
		} else {
			initialParameters.replace("facts", facts);
			MVEL.executeExpression(executeExpression, initialParameters);
		}
		controller.fire((StrategyFacts) facts);
	}

	@Override
	public AgentRuleType getAgentRuleType() {
		return CHAIN;
	}
}
