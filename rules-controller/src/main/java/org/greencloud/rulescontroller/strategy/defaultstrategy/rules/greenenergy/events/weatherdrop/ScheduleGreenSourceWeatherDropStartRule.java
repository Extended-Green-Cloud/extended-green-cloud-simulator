package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.greenenergy.events.weatherdrop;

import static org.greencloud.commons.constants.FactTypeConstants.EVENT_TIME;
import static org.greencloud.commons.enums.rules.RuleType.HANDLE_WEATHER_DROP_START_RULE;
import static org.greencloud.commons.utils.time.TimeScheduler.alignStartTimeToCurrentTime;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.Date;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentScheduledRule;
import org.slf4j.Logger;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.greenenergy.GreenEnergyNode;

public class ScheduleGreenSourceWeatherDropStartRule
		extends AgentScheduledRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ScheduleGreenSourceWeatherDropStartRule.class);

	public ScheduleGreenSourceWeatherDropStartRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(HANDLE_WEATHER_DROP_START_RULE,
				"handle Green Source weather drop start",
				"rule performs actions upon Green Source weather drop start");
	}

	@Override
	protected Date specifyTime(final StrategyFacts facts) {
		final Instant start = facts.get(EVENT_TIME);
		final Instant startTime = alignStartTimeToCurrentTime(start);
		return Date.from(startTime);
	}

	@Override
	protected void handleActionTrigger(final StrategyFacts facts) {
		logger.info("Weather drop has started! Setting available energy to 0");
		agentProps.setHasError(true);
	}
}
