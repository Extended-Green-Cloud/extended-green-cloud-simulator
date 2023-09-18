package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.greenenergy.adaptation.strategy;

import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_STRATEGY_UPDATE_HANDLER_RULE;
import static org.greencloud.commons.utils.messaging.factory.StrategyAdaptationMessageFactory.prepareStrategyRequestReply;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.enums.strategy.StrategyType;
import org.greencloud.commons.domain.strategy.StrategyUpdate;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.greenenergy.GreenEnergyNode;

public class ProcessServersStrategyUpdateRequestRule extends AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ProcessServersStrategyUpdateRequestRule.class);

	public ProcessServersStrategyUpdateRequestRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_STRATEGY_UPDATE_HANDLER_RULE,
				"handling strategy update messages",
				"handling messages from Server asking Green Source to update its strategy");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		return true;
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final StrategyUpdate strategyUpdate = facts.get(MESSAGE_CONTENT);
		final int newStrategyIdx = strategyUpdate.getStrategyIdx();
		final StrategyType strategyType = strategyUpdate.getStrategyType();

		logger.info("Server asked Green Source to update its strategy to {}! Updating strategy to index: {}.",
				strategyType.name(), newStrategyIdx);

		controller.addNewStrategy(strategyType, newStrategyIdx);
		agent.send(prepareStrategyRequestReply(facts.get(MESSAGE), newStrategyIdx));
	}
}
