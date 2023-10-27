package org.greencloud.rulescontroller.behaviour.schedule;

import static java.util.Objects.isNull;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_STEP;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.FactTypeConstants.TRIGGER_TIME;
import static org.greencloud.commons.enums.rules.RuleStepType.SCHEDULED_EXECUTE_ACTION_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.SCHEDULED_SELECT_TIME_STEP;
import static org.greencloud.rulescontroller.ruleset.RuleSetSelector.selectRuleSetIndex;

import java.util.function.ToIntFunction;

import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.mapper.FactsMapper;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.ruleset.RuleSetSelector;
import org.jeasy.rules.api.Facts;

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;

/**
 * Abstract behaviour providing template to handle execution of scheduled behaviour
 */
public class ScheduleOnce extends WakerBehaviour {

	protected final ToIntFunction<Facts> selectRuleSet;
	final RuleSetFacts facts;
	protected RulesController<?, ?> controller;

	/**
	 * Constructor
	 *
	 * @param agent agent executing the behaviour
	 * @param facts facts under which CFP is to be sent
	 */
	protected ScheduleOnce(final Agent agent, final RuleSetFacts facts, final RulesController<?, ?> controller,
			final ToIntFunction<Facts> selectRuleSet) {
		super(agent, facts.get(TRIGGER_TIME));
		this.facts = facts;
		this.controller = controller;
		this.selectRuleSet = isNull(selectRuleSet) ? o -> controller.getLatestRuleSet().get() : selectRuleSet;
	}

	/**
	 * Method creates behaviour
	 *
	 * @param agent      agent executing the behaviour
	 * @param facts      facts under which behaviour is executed
	 * @param ruleType   type of the rule that handles execution
	 * @param controller rules controller
	 * @return ScheduleOnce
	 */
	public static ScheduleOnce create(final Agent agent, final RuleSetFacts facts, final String ruleType,
			final RulesController<?, ?> controller, final RuleSetSelector selector) {
		final RuleSetFacts methodFacts = FactsMapper.mapToRuleSetFacts(facts);
		methodFacts.put(RULE_TYPE, ruleType);
		methodFacts.put(RULE_STEP, SCHEDULED_SELECT_TIME_STEP);
		controller.fire(methodFacts);

		return new ScheduleOnce(agent, methodFacts, controller, selectRuleSetIndex(selector, controller));
	}

	/**
	 * Method performs scheduled action
	 */
	@Override
	protected void onWake() {
		final int ruleSetIdx = selectRuleSet.applyAsInt(facts);
		facts.put(RULE_SET_IDX, ruleSetIdx);
		facts.put(RULE_STEP, SCHEDULED_EXECUTE_ACTION_STEP);
		controller.fire(facts);
		postProcessScheduledAction(facts);
	}

	/**
	 * Method can be optionally overridden in order to perform facts-based actions at the end of behaviour
	 */
	protected void postProcessScheduledAction(final RuleSetFacts facts) {
		// to be overridden if necessary
	}
}
