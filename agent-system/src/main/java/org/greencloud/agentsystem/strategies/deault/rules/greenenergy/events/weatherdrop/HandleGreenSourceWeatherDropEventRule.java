package org.greencloud.agentsystem.strategies.deault.rules.greenenergy.events.weatherdrop;

import static org.greencloud.commons.args.agent.EGCSAgentType.GREEN_ENERGY;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.HANDLE_WEATHER_DROP_FINISH_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.HANDLE_WEATHER_DROP_START_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.WEATHER_DROP_ERROR_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.EVENT_DURATION;
import static org.jrba.rulesengine.constants.FactTypeConstants.EVENT_TIME;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.utils.rules.RuleSetSelector.SELECT_LATEST;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.schedule.ScheduleOnce;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
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

	@Override
	public AgentRule copy() {
		return new HandleGreenSourceWeatherDropEventRule(controller);
	}

	@Override
	public String getAgentType() {
		return GREEN_ENERGY.getName();
	}
}
