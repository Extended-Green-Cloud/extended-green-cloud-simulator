package org.greencloud.agentsystem.strategies.deafult.rules.greenenergy.events.weatherdrop;

import static org.greencloud.commons.args.agent.EGCSAgentType.GREEN_ENERGY;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.HANDLE_WEATHER_DROP_FINISH_RULE;
import static org.greencloud.commons.utils.time.TimeScheduler.alignStartTimeToCurrentTime;
import static org.jrba.rulesengine.constants.FactTypeConstants.EVENT_DURATION;
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

public class ScheduleGreenSourceWeatherDropFinishRule
		extends AgentScheduledRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ScheduleGreenSourceWeatherDropFinishRule.class);

	public ScheduleGreenSourceWeatherDropFinishRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(HANDLE_WEATHER_DROP_FINISH_RULE,
				"handle Green Source weather drop finish",
				"rule performs actions upon Green Source weather drop finish");
	}

	@Override
	protected Date specifyTime(final RuleSetFacts facts) {
		final Instant start = facts.get(EVENT_TIME);
		final long duration = facts.get(EVENT_DURATION);
		final Instant finishTime = alignStartTimeToCurrentTime(start.plusSeconds(duration));
		return Date.from(finishTime);
	}

	@Override
	protected void handleActionTrigger(final RuleSetFacts facts) {
		logger.info("Weather drop has finished! Regaining available energy.");
		agentProps.setHasError(false);
	}

	@Override
	public AgentRule copy() {
		return new ScheduleGreenSourceWeatherDropFinishRule(controller);
	}

	@Override
	public String getAgentType() {
		return GREEN_ENERGY.getName();
	}
}
