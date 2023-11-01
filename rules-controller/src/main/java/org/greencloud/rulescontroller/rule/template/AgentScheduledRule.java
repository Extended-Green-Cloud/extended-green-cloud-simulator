package org.greencloud.rulescontroller.rule.template;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.FactTypeConstants.TRIGGER_TIME;
import static org.greencloud.commons.enums.rules.RuleStepType.SCHEDULED_EXECUTE_ACTION_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.SCHEDULED_SELECT_TIME_STEP;
import static org.greencloud.rulescontroller.rule.AgentRuleType.SCHEDULED;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.gui.agents.AgentNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rest.domain.ScheduledRuleRest;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.AgentRuleType;
import org.mvel2.MVEL;

public class AgentScheduledRule<T extends AgentProps, E extends AgentNode<T>> extends AgentBasicRule<T, E> {

	private List<AgentRule> stepRules;
	private Serializable expressionSpecifyTime;
	private Serializable expressionHandleActionTrigger;
	private Serializable expressionEvaluateBeforeTrigger;

	/**
	 * Constructor
	 *
	 * @param controller rules controller connected to the agent
	 */
	protected AgentScheduledRule(final RulesController<T, E> controller) {
		super(controller);
		initializeSteps();
	}

	/**
	 * Constructor
	 *
	 * @param ruleRest rest representation of agent rule
	 */
	public AgentScheduledRule(final ScheduledRuleRest ruleRest) {
		super(ruleRest);
		if (nonNull(ruleRest.getEvaluateBeforeTrigger())) {
			this.expressionEvaluateBeforeTrigger = MVEL.compileExpression(
					imports + " " + ruleRest.getEvaluateBeforeTrigger());
		}
		if (nonNull(ruleRest.getSpecifyTime())) {
			this.expressionSpecifyTime = MVEL.compileExpression(imports + " " + ruleRest.getSpecifyTime());
		}
		if (nonNull(ruleRest.getHandleActionTrigger())) {
			this.expressionHandleActionTrigger = MVEL.compileExpression(
					imports + " " + ruleRest.getHandleActionTrigger());
		}
		initializeSteps();
	}

	public void initializeSteps() {
		stepRules = new ArrayList<>(List.of(new SpecifyExecutionTimeRule(), new HandleActionTriggerRule()));
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
		return SCHEDULED;
	}

	/**
	 * Method specify time at which behaviour is to be executed
	 */
	protected Date specifyTime(final RuleSetFacts facts) {
		return null;
	}

	/**
	 * Method evaluates if the action should have effects
	 */
	protected boolean evaluateBeforeTrigger(final RuleSetFacts facts) {
		return true;
	}

	/**
	 * Method executed when specific time of behaviour execution is reached
	 */
	protected void handleActionTrigger(final RuleSetFacts facts) {
	}

	// RULE EXECUTED WHEN EXECUTION TIME IS TO BE SELECTED
	class SpecifyExecutionTimeRule extends AgentBasicRule<T, E> {

		public SpecifyExecutionTimeRule() {
			super(AgentScheduledRule.this.controller);
			this.isRuleStep = true;
		}

		@Override
		public void executeRule(final RuleSetFacts facts) {
			if (nonNull(AgentScheduledRule.this.initialParameters)) {
				AgentScheduledRule.this.initialParameters.replace("facts", facts);
			}
			final Date period = isNull(expressionSpecifyTime) ?
					specifyTime(facts) :
					(Date) MVEL.executeExpression(expressionSpecifyTime, AgentScheduledRule.this.initialParameters);
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
		public boolean evaluateRule(final RuleSetFacts facts) {
			if (nonNull(AgentScheduledRule.this.initialParameters)) {
				AgentScheduledRule.this.initialParameters.replace("facts", facts);
			}
			return isNull(expressionEvaluateBeforeTrigger) ?
					evaluateBeforeTrigger(facts) :
					(boolean) MVEL.executeExpression(expressionEvaluateBeforeTrigger,
							AgentScheduledRule.this.initialParameters);
		}

		@Override
		public void executeRule(final RuleSetFacts facts) {
			if (nonNull(AgentScheduledRule.this.initialParameters)) {
				AgentScheduledRule.this.initialParameters.replace("facts", facts);
			}
			if (isNull(expressionHandleActionTrigger)) {
				handleActionTrigger(facts);
			} else {
				MVEL.executeExpression(expressionHandleActionTrigger, AgentScheduledRule.this.initialParameters);
			}
		}

		@Override
		public AgentRuleDescription initializeRuleDescription() {
			return new AgentRuleDescription(AgentScheduledRule.this.ruleType, SCHEDULED_EXECUTE_ACTION_STEP,
					format("%s - execute action", AgentScheduledRule.this.name),
					"rule that executes action at specific time");
		}
	}

}
