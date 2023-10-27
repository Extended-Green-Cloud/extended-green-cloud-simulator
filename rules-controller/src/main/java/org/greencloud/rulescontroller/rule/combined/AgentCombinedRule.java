package org.greencloud.rulescontroller.rule.combined;

import static java.util.Collections.singletonList;
import static org.greencloud.rulescontroller.rule.AgentRuleType.BASIC;
import static org.greencloud.rulescontroller.rule.AgentRuleType.COMBINED;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.enums.rules.RuleStepType;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.mvel.MVELRuleMapper;
import org.greencloud.rulescontroller.rest.domain.CombinedRuleRest;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.AgentRuleType;
import org.greencloud.rulescontroller.rule.combined.domain.AgentCombinedRuleType;
import org.greencloud.rulescontroller.ruleset.RuleSet;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.support.composite.ActivationRuleGroup;
import org.jeasy.rules.support.composite.UnitRuleGroup;

import com.gui.agents.AgentNode;

import lombok.Getter;
import lombok.Setter;

/**
 * Abstract class defining structure of a rule which combines multiple rules and defines how they should be handled
 */
@Getter
public class AgentCombinedRule<T extends AgentProps, E extends AgentNode<T>> extends AgentBasicRule<T, E> {

	protected final RuleSet ruleSet;
	protected final AgentCombinedRuleType combinationType;
	protected final List<AgentRule> rulesToCombine;

	/**
	 * Constructor
	 *
	 * @param controller      rules controller connected to the agent
	 * @param combinationType way in which agent rules are to be combined
	 */
	protected AgentCombinedRule(final RulesController<T, E> controller, final AgentCombinedRuleType combinationType) {
		super(controller);
		this.combinationType = combinationType;
		this.ruleSet = null;
		this.rulesToCombine = new ArrayList<>(constructRules());
	}

	/**
	 * Constructor
	 *
	 * @param controller      rules controller connected to the agent
	 * @param combinationType way in which agent rules are to be combined
	 * @param priority        priority of rule execution
	 */
	protected AgentCombinedRule(final RulesController<T, E> controller, final AgentCombinedRuleType combinationType,
			final int priority) {
		super(controller, priority);
		this.combinationType = combinationType;
		this.ruleSet = null;
		this.rulesToCombine = new ArrayList<>(constructRules());
	}

	/**
	 * Constructor
	 *
	 * @param controller      rules controller connected to the agent
	 * @param ruleSet         currently executed rule set
	 * @param combinationType way in which agent rules are to be combined
	 */
	protected AgentCombinedRule(final RulesController<T, E> controller, final RuleSet ruleSet,
			final AgentCombinedRuleType combinationType) {
		super(controller);
		this.combinationType = combinationType;
		this.ruleSet = ruleSet;
		this.rulesToCombine = new ArrayList<>(constructRules());
	}

	/**
	 * Constructor
	 *
	 * @param ruleRest rest representation of agent rule
	 * @param ruleSet  currently executed rule set
	 */
	public AgentCombinedRule(final CombinedRuleRest ruleRest, final RuleSet ruleSet) {
		super(ruleRest);
		this.combinationType = ruleRest.getCombinedRuleType();
		this.ruleSet = ruleSet;
		this.rulesToCombine = ruleRest.getRulesToCombine().stream()
				.map(rule -> MVELRuleMapper.getRuleForType(rule, ruleSet))
				.toList();
	}

	@Override
	public void connectToController(final RulesController<?, ?> rulesController) {
		super.connectToController(rulesController);
		rulesToCombine.forEach(rule -> rule.connectToController(controller));
	}

	@Override
	public AgentRuleType getAgentRuleType() {
		return COMBINED;
	}

	@Override
	public List<AgentRule> getRules() {
		return singletonList(switch (combinationType) {
			case EXECUTE_FIRST -> constructExecuteFirstGroup();
			case EXECUTE_ALL -> constructExecuteAllGroup();
		});
	}

	/**
	 * Method returns nested combined rules
	 *
	 * @return nested rules
	 */
	public List<String> getNestedRules() {
		return rulesToCombine.stream().map(AgentRule::getSubRuleType).toList();
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		return true;
	}

	/**
	 * Method construct set of rules that are to be combined
	 */
	protected List<AgentRule> constructRules() {
		return new ArrayList<>();
	}

	private AgentExecuteFirstCombinedRule constructExecuteFirstGroup() {
		final AgentExecuteFirstCombinedRule rulesGroup =
				new AgentExecuteFirstCombinedRule(name, description, this::evaluateRule, this::executeRule);
		rulesToCombine.forEach(rulesGroup::addRule);
		return rulesGroup;
	}

	private AgentExecuteAllCombinedRule constructExecuteAllGroup() {
		final AgentExecuteAllCombinedRule rulesGroup =
				new AgentExecuteAllCombinedRule(name, description, this::evaluateRule, this::executeRule);
		rulesToCombine.forEach(rulesGroup::addRule);
		return rulesGroup;
	}

	@Setter
	class AgentExecuteAllCombinedRule extends UnitRuleGroup implements AgentRule {

		private final Predicate<RuleSetFacts> preEvaluated;
		private final Consumer<RuleSetFacts> preExecute;

		public AgentExecuteAllCombinedRule(final String name, final String description,
				final Predicate<RuleSetFacts> preEvaluated, final Consumer<RuleSetFacts> preExecute) {
			super(name, description);
			this.preEvaluated = preEvaluated;
			this.preExecute = preExecute;
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return AgentCombinedRule.super.initializeRuleDescription();
		}

		@Override
		public boolean evaluate(final Facts facts) {
			if (preEvaluated.test((RuleSetFacts) facts)) {
				preExecute.accept((RuleSetFacts) facts);
			}
			return preEvaluated.test((RuleSetFacts) facts) && super.evaluate(facts);
		}

		@Override
		public String getAgentType() {
			return AgentCombinedRule.this.getAgentType();
		}

		@Override
		public AgentRuleType getAgentRuleType() {
			return BASIC;
		}

		@Override
		public boolean evaluateRule(final RuleSetFacts facts) {
			return this.evaluate(facts);
		}

		@Override
		public String getRuleType() {
			return AgentCombinedRule.this.ruleType;
		}

		@Override
		public String getSubRuleType() {
			return AgentCombinedRule.this.subRuleType;
		}

		@Override
		public RuleStepType getStepType() {
			return null;
		}

		@Override
		public boolean isRuleStep() {
			return false;
		}
	}

	@Setter
	class AgentExecuteFirstCombinedRule extends ActivationRuleGroup implements AgentRule {

		private final Predicate<RuleSetFacts> preEvaluated;
		private final Consumer<RuleSetFacts> preExecute;

		public AgentExecuteFirstCombinedRule(final String name, final String description,
				final Predicate<RuleSetFacts> preEvaluated, final Consumer<RuleSetFacts> preExecute) {
			super(name, description);
			this.preEvaluated = preEvaluated;
			this.preExecute = preExecute;
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return AgentCombinedRule.super.initializeRuleDescription();
		}

		@Override
		public boolean evaluate(final Facts facts) {
			if (preEvaluated.test((RuleSetFacts) facts)) {
				preExecute.accept((RuleSetFacts) facts);
				return super.evaluate(facts);
			}
			return false;
		}

		@Override
		public AgentRuleType getAgentRuleType() {
			return BASIC;
		}

		@Override
		public boolean evaluateRule(final RuleSetFacts facts) {
			return this.evaluate(facts);
		}

		@Override
		public String getRuleType() {
			return AgentCombinedRule.this.ruleType;
		}

		@Override
		public String getSubRuleType() {
			return AgentCombinedRule.this.subRuleType;
		}

		@Override
		public RuleStepType getStepType() {
			return null;
		}

		@Override
		public String getAgentType() {
			return AgentCombinedRule.this.getAgentType();
		}

		@Override
		public boolean isRuleStep() {
			return false;
		}
	}
}
