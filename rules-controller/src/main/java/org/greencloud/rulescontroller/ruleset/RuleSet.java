package org.greencloud.rulescontroller.ruleset;

import static org.greencloud.commons.constants.FactTypeConstants.RULE_STEP;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.mvel.MVELRuleMapper;
import org.greencloud.rulescontroller.rest.domain.RuleSetRest;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;

import lombok.Getter;
import lombok.Setter;

/**
 * Class represents rule set of a given system part
 */
@Getter
public class RuleSet {

	protected final RulesEngine rulesEngine;
	private final List<AgentRule> agentRules;
	protected RulesController<?, ?> rulesController;
	@Setter
	private String name;
	private boolean callInitializeRules;

	/**
	 * Constructor
	 *
	 * @param ruleSetRest JSON Rest object from which rule set is to be created
	 */
	public RuleSet(final RuleSetRest ruleSetRest) {
		this.rulesEngine = new DefaultRulesEngine();
		this.name = ruleSetRest.getName();
		this.agentRules = ruleSetRest.getRules().stream()
				.map(ruleRest -> MVELRuleMapper.getRuleForType(ruleRest, this))
				.map(AgentRule.class::cast)
				.toList();
		this.callInitializeRules = false;
	}

	/**
	 * Constructor
	 *
	 * @param ruleSet rule set to create copy from
	 */
	public RuleSet(final RuleSet ruleSet) {
		this.rulesEngine = new DefaultRulesEngine();
		this.name = ruleSet.getName();
		this.agentRules = new ArrayList<>(ruleSet.getAgentRules());
		this.rulesController = ruleSet.getRulesController();
		this.callInitializeRules = false;
	}

	/**
	 * Constructor
	 *
	 * @param ruleSet    ruleSet template from ruleSet map
	 * @param controller controller which runs given rule set
	 */
	public RuleSet(final RuleSet ruleSet, final RulesController<?, ?> controller) {
		this.rulesEngine = new DefaultRulesEngine();
		this.rulesController = controller;
		this.name = ruleSet.getName();

		if (!ruleSet.callInitializeRules) {
			this.agentRules = ruleSet.getAgentRules().stream()
					.filter(rule -> rule.getAgentType().equals(controller.getAgentProps().getAgentType())).toList();
			agentRules.forEach(agentRule -> agentRule.connectToController(controller));
		} else {
			this.agentRules = ruleSet.initializeRules(controller);
		}
	}

	/**
	 * Constructor
	 *
	 * @param name name of the rule set
	 */
	protected RuleSet(final String name) {
		this.rulesEngine = new DefaultRulesEngine();
		this.agentRules = new ArrayList<>();
		this.name = name;
		this.callInitializeRules = true;
	}

	/**
	 * Method fires agent rule set for a set of facts
	 *
	 * @param facts set of facts based on which actions are going to be taken
	 */
	public void fireRuleSet(final RuleSetFacts facts) {
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
	 * @param rulesController controller which runs given rule set
	 * @return list of agent rules
	 */
	protected List<AgentRule> initializeRules(RulesController<?, ?> rulesController) {
		return new ArrayList<>();
	}

}
