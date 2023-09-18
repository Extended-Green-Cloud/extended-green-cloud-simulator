package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.greenenergy.weather;

import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.CHECK_WEATHER_PERIODICALLY_RULE;
import static org.greencloud.commons.enums.rules.RuleType.SCHEDULE_CHECK_WEATHER_PERIODICALLY_RULE;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateRequest;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentPeriodicRule;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.greenenergy.GreenEnergyNode;

public class SchedulePeriodicWeatherRequestsRule extends AgentPeriodicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Long PERIODIC_WEATHER_CHECK_TIMEOUT = 1000L;

	public SchedulePeriodicWeatherRequestsRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(SCHEDULE_CHECK_WEATHER_PERIODICALLY_RULE,
				"schedule weather periodical check",
				"rule initiates request for current weather conditions");
	}

	@Override
	protected long specifyPeriod() {
		return PERIODIC_WEATHER_CHECK_TIMEOUT;
	}

	@Override
	protected void handleActionTrigger(final StrategyFacts facts) {
		agent.addBehaviour(InitiateRequest.create(agent, new StrategyFacts(facts.get(STRATEGY_IDX)),
				CHECK_WEATHER_PERIODICALLY_RULE, controller));
	}
}
