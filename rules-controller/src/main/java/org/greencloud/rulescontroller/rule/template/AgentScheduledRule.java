package org.greencloud.rulescontroller.rule.template;

import static org.greencloud.commons.constants.FactTypeConstants.TRIGGER_TIME;
import static org.greencloud.commons.enums.rules.RuleStepType.SCHEDULED_EXECUTE_ACTION_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.SCHEDULED_SELECT_TIME_STEP;
import static java.lang.String.format;
import static org.greencloud.rulescontroller.rule.AgentRuleType.SCHEDULED;

import java.util.Date;
import java.util.List;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.AgentRuleType;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.AbstractNode;

public abstract class AgentScheduledRule<T extends AgentProps, E extends AbstractNode<?, T>>
		extends AgentBasicRule<T, E> {

	/**
	 * Constructor
	 *
	 * @param controller rules controller connected to the agent
	 */
	protected AgentScheduledRule(final RulesController<T, E> controller) {
		super(controller);
	}

	@Override
	public AgentRuleType getAgentRuleType() {
		return SCHEDULED;
	}

	@Override
	public List<AgentRule> getRules() {
		return List.of(new SpecifyExecutionTimeRule(), new HandleActionTriggerRule());
	}

	/**
	 * Method specify time at which behaviour is to be executed
	 */
	protected abstract Date specifyTime(final StrategyFacts facts);

	/**
	 * Method evaluates if the action should have effects
	 */
	protected boolean evaluateBeforeTrigger(final StrategyFacts facts) {
		return true;
	}

	/**
	 * Method executed when specific time of behaviour execution is reached
	 */
	protected abstract void handleActionTrigger(final StrategyFacts facts);

	// RULE EXECUTED WHEN EXECUTION TIME IS TO BE SELECTED
	class SpecifyExecutionTimeRule extends AgentBasicRule<T, E> {

		public SpecifyExecutionTimeRule() {
			super(AgentScheduledRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public void executeRule(final StrategyFacts facts) {
			final Date period = specifyTime(facts);
			facts.put(TRIGGER_TIME, period);
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentScheduledRule.this.ruleType, SCHEDULED_SELECT_TIME_STEP,
					format("%s - specify action execution time", AgentScheduledRule.this.name),
					"rule performed when behaviour execution time is to be selected");
		}
	}

	// RULE EXECUTED WHEN BEHAVIOUR ACTION IS EXECUTED
	class HandleActionTriggerRule extends AgentBasicRule<T, E> {

		public HandleActionTriggerRule() {
			super(AgentScheduledRule.this.controller);
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
			return new AgentRuleDescription(AgentScheduledRule.this.ruleType, SCHEDULED_EXECUTE_ACTION_STEP,
					format("%s - execute action", AgentScheduledRule.this.name),
					"rule that executes action at specific time");
		}
	}

}
