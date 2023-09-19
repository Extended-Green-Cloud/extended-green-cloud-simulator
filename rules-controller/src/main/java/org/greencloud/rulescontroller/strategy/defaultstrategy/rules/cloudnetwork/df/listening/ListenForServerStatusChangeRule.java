package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.cloudnetwork.df.listening;

import static org.greencloud.commons.enums.rules.RuleType.SERVER_STATUS_CHANGE_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.SERVER_STATUS_CHANGE_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_SERVER_STATUS_CHANGE_TEMPLATE;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;
import org.greencloud.rulescontroller.strategy.Strategy;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import com.gui.agents.cloudnetwork.CloudNetworkNode;

public class ListenForServerStatusChangeRule
		extends AgentMessageListenerRule<CloudNetworkAgentProps, CloudNetworkNode> {

	public ListenForServerStatusChangeRule(final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller,
			final Strategy strategy) {
		super(controller, strategy, LISTEN_FOR_SERVER_STATUS_CHANGE_TEMPLATE, 1,
				SERVER_STATUS_CHANGE_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(SERVER_STATUS_CHANGE_RULE,
				"listen for change in status of connected servers",
				"rule run when one of the Servers connected to the CNA changes its status");
	}

}
