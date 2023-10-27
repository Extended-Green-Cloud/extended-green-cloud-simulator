package org.greencloud.rulescontroller.rule.simple;

import static java.util.Objects.isNull;
import static org.greencloud.rulescontroller.rule.AgentRuleType.CHAIN;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.rest.domain.RuleRest;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.rule.AgentRuleType;
import org.greencloud.rulescontroller.ruleset.RuleSet;
import org.jeasy.rules.api.Facts;
import org.mvel2.MVEL;

import com.gui.agents.AgentNode;

import lombok.Getter;

/**
 * Abstract class defining structure of a rule which after successful execution, triggers once again rule set on
 * the updated set of facts
 */
@Getter
public class AgentChainRule<T extends AgentProps, E extends AgentNode<T>> extends AgentBasicRule<T, E> {

	private final RuleSet ruleSet;

	/**
	 * Constructor
	 *
	 * @param controller rules controller connected to the agent
	 * @param priority   priority of the rule execution
	 * @param ruleSet   currently executed rule set
	 */
	protected AgentChainRule(final RulesController<T, E> controller, final int priority, final RuleSet ruleSet) {
		super(controller, priority);
		this.ruleSet = ruleSet;
	}

	/**
	 * Constructor
	 *
	 * @param controller rules controller connected to the agent
	 * @param ruleSet   currently executed rule set
	 */
	protected AgentChainRule(final RulesController<T, E> controller, final RuleSet ruleSet) {
		super(controller);
		this.ruleSet = ruleSet;
	}

	/**
	 * Constructor
	 *
	 * @param ruleRest rest representation of agent rule
	 * @param ruleSet currently executed rule set
	 */
	public AgentChainRule(final RuleRest ruleRest, final RuleSet ruleSet) {
		super(ruleRest);
		this.ruleSet = ruleSet;
	}

	@Override
	public void execute(final Facts facts) throws Exception {
		if (isNull(executeExpression)) {
			this.executeRule((RuleSetFacts) facts);
		} else {
			initialParameters.replace("facts", facts);
			MVEL.executeExpression(executeExpression, initialParameters);
		}
		controller.fire((RuleSetFacts) facts);
	}

	@Override
	public AgentRuleType getAgentRuleType() {
		return CHAIN;
	}
}
