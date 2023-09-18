package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.scheduler.adaptation;

import static org.greencloud.commons.enums.rules.RuleType.WEATHER_DROP_ERROR_RULE;
import static org.greencloud.commons.enums.strategy.StrategyType.WEATHER_DROP_STRATEGY;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.scheduler.SchedulerNode;

public class UpdateStrategyInSchedulerForWeatherDropRule
		extends AgentBasicRule<SchedulerAgentProps, SchedulerNode> {

	private static final Logger logger = getLogger(UpdateStrategyInSchedulerForWeatherDropRule.class);

	public UpdateStrategyInSchedulerForWeatherDropRule(
			final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(WEATHER_DROP_ERROR_RULE,
				"updates strategy for weather alert event",
				"rule updates strategy to 'WeatherDrop' strategy");
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		logger.info("Updating Scheduler strategy to {}!", WEATHER_DROP_STRATEGY);
		controller.addNewStrategy(WEATHER_DROP_STRATEGY, controller.getLatestStrategy().get() + 1);
	}
}
