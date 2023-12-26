package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.df.listening;

import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_RMA_RESOURCE_REQUEST_TEMPLATE;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.gui.agents.server.ServerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;
import org.greencloud.rulescontroller.ruleset.RuleSet;

public class ListenForRMAResourceInformationRequestRule extends
		AgentMessageListenerRule<ServerAgentProps, ServerNode> {

	public ListenForRMAResourceInformationRequestRule(final RulesController<ServerAgentProps, ServerNode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, LISTEN_FOR_RMA_RESOURCE_REQUEST_TEMPLATE, 1,
				"RMA_RESOURCE_REQUEST_HANDLER_RULE");
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription("RMA_RESOURCE_REQUEST_RULE",
				"listen for RMA request about server resources",
				"sends information about server resources to RMA");
	}
}

