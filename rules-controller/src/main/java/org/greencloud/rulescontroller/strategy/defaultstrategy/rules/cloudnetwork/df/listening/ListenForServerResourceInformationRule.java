package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.cloudnetwork.df.listening;

import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_SERVER_RESOURCE_INFORMATION_TEMPLATE;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;
import org.greencloud.rulescontroller.strategy.Strategy;

import com.gui.agents.cloudnetwork.CloudNetworkNode;

public class ListenForServerResourceInformationRule extends
		AgentMessageListenerRule<CloudNetworkAgentProps, CloudNetworkNode> {

	public ListenForServerResourceInformationRule(
			final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller,
			final Strategy strategy) {
		super(controller, strategy, LISTEN_FOR_SERVER_RESOURCE_INFORMATION_TEMPLATE, 1,
				"SERVER_RESOURCE_INFORMATION_HANDLER_RULE");
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription("SERVER_RESOURCE_INFORMATION_RULE",
				"listen for information about available resources from connected servers",
				"rule run when one of the Servers sends information about its resources");
	}

}
