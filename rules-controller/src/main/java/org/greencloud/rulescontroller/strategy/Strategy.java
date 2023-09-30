package org.greencloud.rulescontroller.strategy;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_STEP;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.mvel.MVELRuleMapper;
import org.greencloud.rulescontroller.rest.domain.StrategyRest;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;

import lombok.Getter;
import lombok.Setter;

/**
 * Class represents strategy of a given system part
 */
@Getter
@SuppressWarnings("unchecked")
public class Strategy {

	protected final RulesEngine rulesEngine;
	@Setter
	private String name;
	protected RulesController<?, ?> rulesController;
	private final List<AgentRule> agentRules;
	private boolean callInitializeRules;

	/**
	 * Constructor
	 *
	 * @param strategyRest JSON Rest object from which strategy is to be created
	 */
	public Strategy(final StrategyRest strategyRest) {
		this.rulesEngine = new DefaultRulesEngine();
		this.name = strategyRest.getName();
		this.agentRules = strategyRest.getRules().stream()
				.map(ruleRest -> MVELRuleMapper.getRuleForType(ruleRest, this))
				.map(AgentRule.class::cast)
				.toList();
		this.callInitializeRules = false;
	}

	/**
	 * Constructor
	 *
	 * @param strategy   strategy template from strategy map
	 * @param controller controller which runs given strategy
	 */
	public Strategy(final Strategy strategy, final RulesController<?, ?> controller) {
		this.rulesEngine = new DefaultRulesEngine();
		this.rulesController = controller;
		this.name = strategy.getName();

		if (!strategy.callInitializeRules) {
			this.agentRules = strategy.getAgentRules().stream()
					.filter(rule -> rule.getAgentType().equals(controller.getAgentProps().getAgentType())).toList();
			agentRules.forEach(agentRule -> agentRule.connectToController(controller));
		} else {
			this.agentRules = strategy.initializeRules(controller);
		}
	}

	/**
	 * Constructor
	 *
	 * @param name       name of the strategy
	 */
	protected Strategy(final String name) {
		this.rulesEngine = new DefaultRulesEngine();
		this.agentRules = new ArrayList<>();
		this.name = name;
		this.callInitializeRules = true;
	}

	/**
	 * Method fires agent strategy for a set of facts
	 *
	 * @param facts set of facts based on which actions are going to be taken
	 */
	public void fireStrategy(final StrategyFacts facts) {
		final Rules rules = new Rules();
		agentRules.stream()
				.filter(agentRule -> agentRule.getRuleType().equals(facts.get(RULE_TYPE)))
				.map(AgentRule::getRules)
				.flatMap(Collection::stream)
				.filter(agentRule -> agentRule.isRuleStep()
						? agentRule.getStepType().equals(facts.get(RULE_STEP))
						: agentRule.getRuleType().equals(facts.get(RULE_TYPE)))
				.forEach(rules::register);

		if (!rules.isEmpty()) {
			rulesEngine.fire(rules, facts);
		}
	}

	/**
	 * Method that can be optionally overridden to initialize rules
	 *
	 * @param rulesController controller which runs given strategy
	 * @return list of agent rules
	 */
	protected List<AgentRule> initializeRules(RulesController<?, ?> rulesController) {
		return new ArrayList<>();
	}

}
