package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.greenenergy.monitor;

import static com.database.knowledge.domain.agent.DataType.SHORTAGES;
import static com.database.knowledge.domain.agent.DataType.WEATHER_SHORTAGES;
import static org.greencloud.commons.enums.rules.RuleType.REPORT_DATA_RULE;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentPeriodicRule;
import org.greencloud.commons.domain.facts.StrategyFacts;

import com.database.knowledge.domain.agent.greensource.Shortages;
import com.database.knowledge.domain.agent.greensource.WeatherShortages;
import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import com.gui.agents.greenenergy.GreenEnergyNode;

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
	protected void handleActionTrigger(final StrategyFacts facts) {
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
