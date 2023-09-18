package org.greencloud.rulescontroller.strategies.weatherdropstrategy;

import static org.greencloud.commons.enums.strategy.StrategyType.WEATHER_DROP_STRATEGY;

import java.util.List;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.strategies.weatherdropstrategy.rules.scheduler.execution.HandleFinishJobExecutionInCloudRule;
import org.greencloud.rulescontroller.strategies.weatherdropstrategy.rules.scheduler.execution.HandleStartJobExecutionInCloudRule;
import org.greencloud.rulescontroller.strategies.weatherdropstrategy.rules.scheduler.status.ProcessCNAJobStatusUpdateFailedJobWeatherDropRule;
import org.greencloud.rulescontroller.strategies.weatherdropstrategy.rules.scheduler.status.ProcessLookForCNAForJobExecutionFailureWeatherDropRule;
import org.greencloud.rulescontroller.strategies.weatherdropstrategy.rules.server.newjob.ProcessCNANewJobWeatherDropRule;
import org.greencloud.rulescontroller.strategies.weatherdropstrategy.rules.server.newjob.execution.ProcessJobFinishWeatherDropRule;
import org.greencloud.rulescontroller.strategies.weatherdropstrategy.rules.server.newjob.execution.ProcessJobStartWeatherDropRule;
import org.greencloud.rulescontroller.strategies.weatherdropstrategy.rules.server.newjob.price.CalculateServerPriceWeatherDropRule;
import org.greencloud.rulescontroller.strategies.weatherdropstrategy.rules.server.newjob.propose.ProposeToCNAWeatherDropRule;
import org.greencloud.rulescontroller.strategy.Strategy;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import com.gui.agents.AbstractNode;
import com.gui.agents.scheduler.SchedulerNode;
import com.gui.agents.server.ServerNode;

import jade.core.Agent;

/**
 * Strategy applied in case of weather drop in selected region
 */
public class WeatherDropStrategy extends Strategy {

	public WeatherDropStrategy(final RulesController<?, ?> controller) {
		super(WEATHER_DROP_STRATEGY.name(), controller);
	}

	@Override
	protected List<AgentRule> getServerRules(final RulesController<ServerAgentProps, ServerNode> rulesController) {
		return List.of(
				new ProcessJobFinishWeatherDropRule(rulesController),
				new ProcessJobStartWeatherDropRule(rulesController),
				new CalculateServerPriceWeatherDropRule(rulesController),
				new ProposeToCNAWeatherDropRule(rulesController),
				new ProcessCNANewJobWeatherDropRule(rulesController)
		);
	}

	@Override
	protected List<AgentRule> getSchedulerRules(
			final RulesController<SchedulerAgentProps, SchedulerNode> rulesController) {
		return List.of(
				new ProcessCNAJobStatusUpdateFailedJobWeatherDropRule(rulesController),
				new HandleFinishJobExecutionInCloudRule(rulesController),
				new HandleStartJobExecutionInCloudRule(rulesController),
				new ProcessLookForCNAForJobExecutionFailureWeatherDropRule(rulesController)
		);
	}
}
