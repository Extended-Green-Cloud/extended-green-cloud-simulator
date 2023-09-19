package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.greenenergy.adaptation.strategy;

import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_STRATEGY_REMOVAL_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_STRATEGY_REMOVAL_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_STRATEGY_REMOVAL_REQUEST;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;
import org.greencloud.rulescontroller.strategy.Strategy;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import com.gui.agents.greenenergy.GreenEnergyNode;

public class ListenForServerStrategyRemovalMessageRule
		extends AgentMessageListenerRule<GreenEnergyAgentProps, GreenEnergyNode> {

	public ListenForServerStrategyRemovalMessageRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller, final Strategy strategy) {
		super(controller, strategy, LISTEN_FOR_STRATEGY_REMOVAL_REQUEST, 1, LISTEN_FOR_STRATEGY_REMOVAL_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_STRATEGY_REMOVAL_RULE,
				"listen for strategy update messages",
				"listening for messages from CNA asking Server to remove given strategy");
	}
}
