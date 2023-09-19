package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.cloudnetwork.errorhandling.weatherdrop;

import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_TYPE;
import static org.greencloud.commons.enums.rules.RuleType.REQUEST_STRATEGY_UPDATE_RULE;
import static org.greencloud.commons.enums.rules.RuleType.WEATHER_DROP_ERROR_RULE;
import static org.greencloud.commons.enums.strategy.StrategyType.WEATHER_DROP_STRATEGY;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateRequest;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.cloudnetwork.CloudNetworkNode;

public class HandleCNAWeatherDropEventRule extends AgentBasicRule<CloudNetworkAgentProps, CloudNetworkNode> {

	private static final Logger logger = getLogger(HandleCNAWeatherDropEventRule.class);

	public HandleCNAWeatherDropEventRule(
			final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(WEATHER_DROP_ERROR_RULE,
				"handle worsening weather conditions event",
				"rule updates current system strategy");
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		logger.info("Weather drop event detected! Updating strategy of system components!");

		final StrategyFacts handlerFacts = new StrategyFacts(facts.get(STRATEGY_IDX));
		handlerFacts.put(STRATEGY_TYPE, WEATHER_DROP_STRATEGY.name());
		agent.addBehaviour(InitiateRequest.create(agent, handlerFacts, REQUEST_STRATEGY_UPDATE_RULE, controller));
	}
}