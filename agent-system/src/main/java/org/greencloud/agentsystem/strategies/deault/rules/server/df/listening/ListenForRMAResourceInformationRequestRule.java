package org.greencloud.agentsystem.strategies.deault.rules.server.df.listening;

import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.RMA_RESOURCE_REQUEST_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.RMA_RESOURCE_REQUEST_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_RMA_RESOURCE_REQUEST_TEMPLATE;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentMessageListenerRule;
import org.jrba.rulesengine.ruleset.RuleSet;

public class ListenForRMAResourceInformationRequestRule extends
		AgentMessageListenerRule<ServerAgentProps, ServerNode> {

	public ListenForRMAResourceInformationRequestRule(final RulesController<ServerAgentProps, ServerNode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, LISTEN_FOR_RMA_RESOURCE_REQUEST_TEMPLATE, 1, RMA_RESOURCE_REQUEST_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(RMA_RESOURCE_REQUEST_RULE,
				"listen for RMA request about server resources",
				"sends information about server resources to RMA");
	}

	@Override
	public AgentRule copy() {
		return new ListenForRMAResourceInformationRequestRule(controller, getRuleSet());
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}

