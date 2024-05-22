package org.greencloud.agentsystem.strategies.deault.rules.greenenergy.events.weatherdrop;

import static org.greencloud.commons.args.agent.EGCSAgentType.GREEN_ENERGY;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.HANDLE_WEATHER_DROP_START_RULE;
import static org.greencloud.commons.utils.time.TimeScheduler.alignStartTimeToCurrentTime;
import static org.jrba.rulesengine.constants.FactTypeConstants.EVENT_TIME;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.Date;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentScheduledRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

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
	protected Date specifyTime(final RuleSetFacts facts) {
		final Instant start = facts.get(EVENT_TIME);
		final Instant startTime = alignStartTimeToCurrentTime(start);
		return Date.from(startTime);
	}

	@Override
	protected void handleActionTrigger(final RuleSetFacts facts) {
		logger.info("Weather drop has started! Setting available energy to 0");
		agentProps.setHasError(true);
	}

	@Override
	public AgentRule copy() {
		return new ScheduleGreenSourceWeatherDropStartRule(controller);
	}

	@Override
	public String getAgentType() {
		return GREEN_ENERGY.getName();
	}
}
