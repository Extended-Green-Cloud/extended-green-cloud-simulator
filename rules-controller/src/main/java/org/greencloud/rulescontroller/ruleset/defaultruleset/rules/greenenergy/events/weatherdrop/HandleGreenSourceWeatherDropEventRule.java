package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.events.weatherdrop;

import static org.greencloud.commons.constants.FactTypeConstants.EVENT_DURATION;
import static org.greencloud.commons.constants.FactTypeConstants.EVENT_TIME;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.enums.rules.RuleType.HANDLE_WEATHER_DROP_FINISH_RULE;
import static org.greencloud.commons.enums.rules.RuleType.HANDLE_WEATHER_DROP_START_RULE;
import static org.greencloud.commons.enums.rules.RuleType.WEATHER_DROP_ERROR_RULE;
import static org.greencloud.rulescontroller.ruleset.RuleSetSelector.SELECT_LATEST;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.schedule.ScheduleOnce;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

public class HandleGreenSourceWeatherDropEventRule extends AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(HandleGreenSourceWeatherDropEventRule.class);

	public HandleGreenSourceWeatherDropEventRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(WEATHER_DROP_ERROR_RULE,
				"handle worsening weather conditions event",
				"rule schedules behaviours handling worsening of weather conditions");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		logger.info("Weather drop event detected!");

		final RuleSetFacts handlersFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
		handlersFacts.put(EVENT_TIME, facts.get(EVENT_TIME));
		handlersFacts.put(EVENT_DURATION, facts.get(EVENT_DURATION));

		agent.addBehaviour(
				ScheduleOnce.create(agent, handlersFacts, HANDLE_WEATHER_DROP_START_RULE, controller, SELECT_LATEST));
		agent.addBehaviour(
				ScheduleOnce.create(agent, handlersFacts, HANDLE_WEATHER_DROP_FINISH_RULE, controller, SELECT_LATEST));
	}
}