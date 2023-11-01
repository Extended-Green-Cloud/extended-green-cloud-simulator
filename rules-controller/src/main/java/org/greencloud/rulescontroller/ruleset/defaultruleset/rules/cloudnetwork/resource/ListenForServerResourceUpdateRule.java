package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.resource;

import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_SERVER_RESOURCE_UPDATE_TEMPLATE;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.agent.ServerResources;
import org.greencloud.gui.agents.cloudnetwork.CloudNetworkNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;
import org.greencloud.rulescontroller.ruleset.RuleSet;

public class ListenForServerResourceUpdateRule extends
		AgentMessageListenerRule<CloudNetworkAgentProps, CloudNetworkNode> {

	public ListenForServerResourceUpdateRule(
			final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, ServerResources.class, LISTEN_FOR_SERVER_RESOURCE_UPDATE_TEMPLATE, 1,
				"SERVER_RESOURCE_UPDATE_HANDLER_RULE");
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription("SERVER_RESOURCE_UPDATE_RULE",
				"listen for information about update of resources in one of connected servers",
				"rule run when one of the Servers sends information about update in its resources");
	}

}
