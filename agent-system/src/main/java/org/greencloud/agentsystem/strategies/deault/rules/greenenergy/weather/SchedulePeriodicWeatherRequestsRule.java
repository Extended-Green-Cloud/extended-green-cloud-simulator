package org.greencloud.agentsystem.strategies.deault.rules.greenenergy.weather;

import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.CHECK_WEATHER_PERIODICALLY_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SCHEDULE_CHECK_WEATHER_PERIODICALLY_RULE;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.initiate.InitiateRequest;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentPeriodicRule;

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
	protected void handleActionTrigger(final RuleSetFacts facts) {
		agent.addBehaviour(InitiateRequest.create(agent, new RuleSetFacts(facts.get(RULE_SET_IDX)),
				CHECK_WEATHER_PERIODICALLY_RULE, controller));
	}
}
