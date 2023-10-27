package org.greencloud.rulescontroller.behaviour.schedule;

import static org.greencloud.commons.constants.FactTypeConstants.RULE_STEP;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.TRIGGER_PERIOD;
import static org.greencloud.commons.enums.rules.RuleStepType.PERIODIC_EXECUTE_ACTION_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.PERIODIC_SELECT_PERIOD_STEP;

import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.mapper.FactsMapper;
import org.greencloud.rulescontroller.RulesController;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

/**
 * Abstract behaviour providing template to handle periodical behaviour of an agent
 */
public class SchedulePeriodically extends TickerBehaviour {

	private final String ruleType;
	protected RulesController<?, ?> controller;

	protected SchedulePeriodically(final Agent agent, final RuleSetFacts facts,
			final RulesController<?, ?> controller) {
		super(agent, facts.get(TRIGGER_PERIOD));

		this.ruleType = facts.get(RULE_TYPE);
		this.controller = controller;
	}

	/**
	 * Method creates behaviour
	 *
	 * @param agent      agent executing the behaviour
	 * @param facts      facts under which behaviour is executed
	 * @param ruleType   type of the rule that handles execution
	 * @param controller rules controller
	 * @return SchedulePeriodically
	 */
	public static SchedulePeriodically create(final Agent agent, final RuleSetFacts facts, final String ruleType,
			final RulesController<?, ?> controller) {
		final RuleSetFacts methodFacts = FactsMapper.mapToRuleSetFacts(facts);
		methodFacts.put(RULE_TYPE, ruleType);
		methodFacts.put(RULE_STEP, PERIODIC_SELECT_PERIOD_STEP);
		controller.fire(methodFacts);

		return new SchedulePeriodically(agent, methodFacts, controller);
	}

	/**
	 * Method performs periodic action
	 */
	@Override
	protected void onTick() {
		final RuleSetFacts newFacts = new RuleSetFacts(controller.getLatestRuleSet().get());
		newFacts.put(RULE_TYPE, ruleType);
		newFacts.put(RULE_STEP, PERIODIC_EXECUTE_ACTION_STEP);
		controller.fire(newFacts);
		postProcessPeriodicAction(newFacts);
	}

	/**
	 * Method can be optionally overridden in order to perform facts-based actions at the end of behaviour
	 */
	protected void postProcessPeriodicAction(final RuleSetFacts facts) {
		// to be overridden if necessary
	}
}
