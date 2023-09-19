package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.server.adaptation.strategy;

import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_STRATEGY_UPDATE_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_STRATEGY_UPDATE_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_STRATEGY_UPDATE_REQUEST;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;
import org.greencloud.rulescontroller.strategy.Strategy;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.strategy.StrategyUpdate;
import com.gui.agents.server.ServerNode;

public class ListenForStrategyUpdateRequestRule extends AgentMessageListenerRule<ServerAgentProps, ServerNode> {

	public ListenForStrategyUpdateRequestRule(final RulesController<ServerAgentProps, ServerNode> controller,
			final Strategy strategy) {
		super(controller, strategy, StrategyUpdate.class, LISTEN_FOR_STRATEGY_UPDATE_REQUEST, 1,
				LISTEN_FOR_STRATEGY_UPDATE_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_STRATEGY_UPDATE_RULE,
				"listen for strategy update messages",
				"listening for messages from CNA asking Server to update its strategy");
	}
}
