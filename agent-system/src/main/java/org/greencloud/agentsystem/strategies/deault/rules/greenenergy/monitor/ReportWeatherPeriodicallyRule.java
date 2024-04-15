package org.greencloud.agentsystem.strategies.deault.rules.greenenergy.monitor;

import static com.database.knowledge.domain.agent.DataType.SHORTAGES;
import static com.database.knowledge.domain.agent.DataType.WEATHER_SHORTAGES;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.REPORT_DATA_RULE;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentPeriodicRule;

import com.database.knowledge.domain.agent.greensource.Shortages;
import com.database.knowledge.domain.agent.greensource.WeatherShortages;

public class ReportWeatherPeriodicallyRule extends AgentPeriodicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	public static final Long PERIODIC_SHORTAGE_REPORT_PERIOD = 250L;

	public ReportWeatherPeriodicallyRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(REPORT_DATA_RULE,
				"report weather drops data",
				"rule reports the amount of drops in weather conditions");
	}

	@Override
	protected long specifyPeriod() {
		return PERIODIC_SHORTAGE_REPORT_PERIOD;
	}

	@Override
	protected void handleActionTrigger(final RuleSetFacts facts) {
		final int weatherShortages = agentProps.getWeatherShortagesCounter().get();
		final int accumulatedShortages = agentProps.getShortagesAccumulator().get();

		if (weatherShortages > 0) {
			agentProps.getWeatherShortagesCounter().set(0);
			agentNode.writeMonitoringData(WEATHER_SHORTAGES,
					new WeatherShortages(weatherShortages, PERIODIC_SHORTAGE_REPORT_PERIOD), agent.getName());
		}

		if (accumulatedShortages > 0) {
			agentNode.writeMonitoringData(SHORTAGES, new Shortages(accumulatedShortages), agent.getName());
		}
	}
}
