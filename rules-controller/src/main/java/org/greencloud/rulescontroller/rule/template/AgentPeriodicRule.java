package org.greencloud.rulescontroller.rule.template;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.FactTypeConstants.TRIGGER_PERIOD;
import static org.greencloud.commons.enums.rules.RuleStepType.PERIODIC_EXECUTE_ACTION_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.PERIODIC_SELECT_PERIOD_STEP;
import static org.greencloud.rulescontroller.rule.AgentRuleType.PERIODIC;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.gui.agents.AgentNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rest.domain.PeriodicRuleRest;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.AgentRuleType;
import org.mvel2.MVEL;

/**
 * Abstract class defining structure of a rule which handles default periodic behaviour
 */
public class AgentPeriodicRule<T extends AgentProps, E extends AgentNode<T>> extends AgentBasicRule<T, E> {

	private List<AgentRule> stepRules;
	private Serializable expressionSpecifyPeriod;
	private Serializable expressionHandleActionTrigger;
	private Serializable expressionEvaluateBeforeTrigger;

	/**
	 * Constructor
	 *
	 * @param controller rules controller connected to the agent
	 */
	protected AgentPeriodicRule(final RulesController<T, E> controller) {
		super(controller);
		initializeSteps();
	}

	/**
	 * Constructor
	 *
	 * @param ruleRest rest representation of agent rule
	 */
	public AgentPeriodicRule(final PeriodicRuleRest ruleRest) {
		super(ruleRest);
		if (nonNull(ruleRest.getEvaluateBeforeTrigger())) {
			this.expressionEvaluateBeforeTrigger = MVEL.compileExpression(
					imports + " " + ruleRest.getEvaluateBeforeTrigger());
		}
		if (nonNull(ruleRest.getSpecifyPeriod())) {
			this.expressionSpecifyPeriod = MVEL.compileExpression(imports + " " + ruleRest.getSpecifyPeriod());
		}
		if (nonNull(ruleRest.getHandleActionTrigger())) {
			this.expressionHandleActionTrigger = MVEL.compileExpression(
					imports + " " + ruleRest.getHandleActionTrigger());
		}
		initializeSteps();
	}

	/**
	 * Method evaluates if the action should have effects
	 */
	protected boolean evaluateBeforeTrigger(final RuleSetFacts facts) {
		return true;
	}

	@Override
	public AgentRuleType getAgentRuleType() {
		return PERIODIC;
	}

	public void initializeSteps() {
		stepRules = new ArrayList<>(List.of(new SpecifyPeriodRule(), new HandleActionTriggerRule()));
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

	/**
	 * Method specify period after which behaviour is to be executed
	 */
	protected long specifyPeriod() {
		return 0;
	}

	/**
	 * Method executed when time after which action is to be triggerred has passed
	 */
	protected void handleActionTrigger(final RuleSetFacts facts) {

	}

	// RULE EXECUTED WHEN PERIOD IS TO BE SELECTED
	class SpecifyPeriodRule extends AgentBasicRule<T, E> {

		public SpecifyPeriodRule() {
			super(AgentPeriodicRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public void executeRule(final RuleSetFacts facts) {
			if (nonNull(AgentPeriodicRule.this.initialParameters)) {
				AgentPeriodicRule.this.initialParameters.replace("facts", facts);
			}

			final long period = isNull(expressionSpecifyPeriod)
					? specifyPeriod()
					: (long) MVEL.executeExpression(expressionSpecifyPeriod, AgentPeriodicRule.this.initialParameters);
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
		public boolean evaluateRule(final RuleSetFacts facts) {
			if (nonNull(AgentPeriodicRule.this.initialParameters)) {
				AgentPeriodicRule.this.initialParameters.replace("facts", facts);
			}

			return isNull(expressionEvaluateBeforeTrigger) ?
					evaluateBeforeTrigger(facts) :
					(boolean) MVEL.executeExpression(expressionEvaluateBeforeTrigger,
							AgentPeriodicRule.this.initialParameters);
		}

		@Override
		public void executeRule(final RuleSetFacts facts) {
			if (nonNull(AgentPeriodicRule.this.initialParameters)) {
				AgentPeriodicRule.this.initialParameters.replace("facts", facts);
			}

			if (isNull(expressionHandleActionTrigger)) {
				handleActionTrigger(facts);
			} else {
				MVEL.executeExpression(expressionHandleActionTrigger, AgentPeriodicRule.this.initialParameters);
			}
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentPeriodicRule.this.ruleType, PERIODIC_EXECUTE_ACTION_STEP,
					format("%s - execute action", AgentPeriodicRule.this.name),
					"rule that executes action after specified period of time has passed");
		}
	}

}
