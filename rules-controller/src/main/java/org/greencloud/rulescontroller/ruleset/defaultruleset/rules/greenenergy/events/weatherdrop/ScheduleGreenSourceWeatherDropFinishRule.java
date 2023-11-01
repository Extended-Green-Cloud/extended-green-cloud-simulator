package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.events.weatherdrop;

import static org.greencloud.commons.constants.FactTypeConstants.EVENT_DURATION;
import static org.greencloud.commons.constants.FactTypeConstants.EVENT_TIME;
import static org.greencloud.commons.enums.rules.RuleType.HANDLE_WEATHER_DROP_FINISH_RULE;
import static org.greencloud.commons.utils.time.TimeScheduler.alignStartTimeToCurrentTime;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.Date;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentScheduledRule;
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
}
