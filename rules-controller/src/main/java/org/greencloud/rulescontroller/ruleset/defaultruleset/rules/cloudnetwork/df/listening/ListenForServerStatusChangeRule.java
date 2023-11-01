package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.df.listening;

import static org.greencloud.commons.enums.rules.RuleType.SERVER_STATUS_CHANGE_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.SERVER_STATUS_CHANGE_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_SERVER_STATUS_CHANGE_TEMPLATE;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.gui.agents.cloudnetwork.CloudNetworkNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;
import org.greencloud.rulescontroller.ruleset.RuleSet;

public class ListenForServerStatusChangeRule
		extends AgentMessageListenerRule<CloudNetworkAgentProps, CloudNetworkNode> {

	public ListenForServerStatusChangeRule(final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, LISTEN_FOR_SERVER_STATUS_CHANGE_TEMPLATE, 1,
				SERVER_STATUS_CHANGE_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(SERVER_STATUS_CHANGE_RULE,
				"listen for change in status of connected servers",
				"rule run when one of the Servers connected to the CNA changes its status");
	}

}
