package org.greencloud.rulescontroller.rule.template;

import static org.greencloud.commons.constants.FactTypeConstants.TRIGGER_PERIOD;
import static org.greencloud.commons.enums.rules.RuleStepType.PERIODIC_EXECUTE_ACTION_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.PERIODIC_SELECT_PERIOD_STEP;
import static java.lang.String.format;
import static org.greencloud.rulescontroller.rule.AgentRuleType.PERIODIC;

import java.util.List;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.AgentRuleType;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.AbstractNode;

/**
 * Abstract class defining structure of a rule which handles default periodic behaviour
 */
public abstract class AgentPeriodicRule<T extends AgentProps, E extends AbstractNode<?, T>>
		extends AgentBasicRule<T, E> {

	/**
	 * Constructor
	 *
	 * @param controller rules controller connected to the agent
	 */
	protected AgentPeriodicRule(final RulesController<T, E> controller) {
		super(controller);
	}

	@Override
	public AgentRuleType getAgentRuleType() {
		return PERIODIC;
	}

	@Override
	public List<AgentRule> getRules() {
		return List.of(new SpecifyPeriodRule(), new HandleActionTriggerRule());
	}

	/**
	 * Method specify period after which behaviour is to be executed
	 */
	protected abstract long specifyPeriod();

	/**
	 * Method evaluates if the action should have effects
	 */
	protected boolean evaluateBeforeTrigger(final StrategyFacts facts) {
		return true;
	}

	/**
	 * Method executed when time after which action is to be triggerred has passed
	 */
	protected abstract void handleActionTrigger(final StrategyFacts facts);

	// RULE EXECUTED WHEN PERIOD IS TO BE SELECTED
	class SpecifyPeriodRule extends AgentBasicRule<T, E> {

		public SpecifyPeriodRule() {
			super(AgentPeriodicRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public void executeRule(final StrategyFacts facts) {
			final long period = specifyPeriod();
			facts.put(TRIGGER_PERIOD, period);
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentPeriodicRule.this.ruleType, PERIODIC_SELECT_PERIOD_STEP,
					format("%s - specify action period", AgentPeriodicRule.this.name),
					"rule performed when behaviour period is to be selected");
		}
	}

	// RULE EXECUTED WHEN BEHAVIOUR ACTION IS EXECUTED
	class HandleActionTriggerRule extends AgentBasicRule<T, E> {

		public HandleActionTriggerRule() {
			super(AgentPeriodicRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public boolean evaluateRule(final StrategyFacts facts) {
			return evaluateBeforeTrigger(facts);
		}

		@Override
		public void executeRule(final StrategyFacts facts) {
			handleActionTrigger(facts);
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentPeriodicRule.this.ruleType, PERIODIC_EXECUTE_ACTION_STEP,
					format("%s - execute action", AgentPeriodicRule.this.name),
					"rule that executes action after specified period of time has passed");
		}
	}

}
