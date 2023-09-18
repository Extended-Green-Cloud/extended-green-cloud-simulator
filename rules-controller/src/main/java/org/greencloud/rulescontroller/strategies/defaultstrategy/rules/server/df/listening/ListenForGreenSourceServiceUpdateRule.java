package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.server.df.listening;

import static org.greencloud.commons.enums.rules.RuleType.GREEN_SOURCE_STATUS_CHANGE_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.GREEN_SOURCE_STATUS_CHANGE_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_GREEN_SOURCE_UPDATE_TEMPLATE;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;
import org.greencloud.rulescontroller.strategy.Strategy;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import com.gui.agents.server.ServerNode;

public class ListenForGreenSourceServiceUpdateRule extends AgentMessageListenerRule<ServerAgentProps, ServerNode> {

	public ListenForGreenSourceServiceUpdateRule(final RulesController<ServerAgentProps, ServerNode> controller,
			final Strategy strategy) {
		super(controller, strategy, LISTEN_FOR_GREEN_SOURCE_UPDATE_TEMPLATE, 1,
				GREEN_SOURCE_STATUS_CHANGE_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(GREEN_SOURCE_STATUS_CHANGE_RULE,
				"listen for updates in green source connection state",
				"updating connection state between server and green source");
	}
}
