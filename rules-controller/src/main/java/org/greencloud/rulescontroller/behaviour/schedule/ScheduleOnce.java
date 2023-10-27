package org.greencloud.rulescontroller.behaviour.schedule;

import static java.util.Objects.isNull;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_STEP;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.constants.FactTypeConstants.TRIGGER_TIME;
import static org.greencloud.commons.enums.rules.RuleStepType.SCHEDULED_EXECUTE_ACTION_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.SCHEDULED_SELECT_TIME_STEP;
import static org.greencloud.rulescontroller.strategy.StrategySelector.selectStrategyIndex;

import java.util.function.ToIntFunction;

import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.commons.mapper.FactsMapper;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.strategy.StrategySelector;
import org.jeasy.rules.api.Facts;

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;

/**
 * Abstract behaviour providing template to handle execution of scheduled behaviour
 */
public class ScheduleOnce extends WakerBehaviour {

	protected final ToIntFunction<Facts> selectStrategy;
	final StrategyFacts facts;
	protected RulesController<?, ?> controller;

	/**
	 * Constructor
	 *
	 * @param agent agent executing the behaviour
	 * @param facts facts under which CFP is to be sent
	 */
	protected ScheduleOnce(final Agent agent, final StrategyFacts facts, final RulesController<?, ?> controller,
			final ToIntFunction<Facts> selectStrategy) {
		super(agent, facts.get(TRIGGER_TIME));
		this.facts = facts;
		this.controller = controller;
		this.selectStrategy = isNull(selectStrategy) ? o -> controller.getLatestStrategy().get() : selectStrategy;
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
	public static ScheduleOnce create(final Agent agent, final StrategyFacts facts, final String ruleType,
			final RulesController<?, ?> controller, final StrategySelector selector) {
		final StrategyFacts methodFacts = FactsMapper.mapToStrategyFacts(facts);
		methodFacts.put(RULE_TYPE, ruleType);
		methodFacts.put(RULE_STEP, SCHEDULED_SELECT_TIME_STEP);
		controller.fire(methodFacts);

		return new ScheduleOnce(agent, methodFacts, controller, selectStrategyIndex(selector, controller));
	}

	/**
	 * Method performs scheduled action
	 */
	@Override
	protected void onWake() {
		final int strategyIdx = selectStrategy.applyAsInt(facts);
		facts.put(STRATEGY_IDX, strategyIdx);
		facts.put(RULE_STEP, SCHEDULED_EXECUTE_ACTION_STEP);
		controller.fire(facts);
		postProcessScheduledAction(facts);
	}

	/**
	 * Method can be optionally overridden in order to perform facts-based actions at the end of behaviour
	 */
	protected void postProcessScheduledAction(final StrategyFacts facts) {
		// to be overridden if necessary
	}
}
