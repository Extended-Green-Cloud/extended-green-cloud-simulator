package org.greencloud.agentsystem.strategies.deafult.rules.server.df.listening;

import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.GREEN_SOURCE_STATUS_CHANGE_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.GREEN_SOURCE_STATUS_CHANGE_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_GREEN_SOURCE_UPDATE_TEMPLATE;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentMessageListenerRule;
import org.jrba.rulesengine.ruleset.RuleSet;

public class ListenForGreenSourceServiceUpdateRule extends AgentMessageListenerRule<ServerAgentProps, ServerNode> {

	public ListenForGreenSourceServiceUpdateRule(final RulesController<ServerAgentProps, ServerNode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, LISTEN_FOR_GREEN_SOURCE_UPDATE_TEMPLATE, 1,
				GREEN_SOURCE_STATUS_CHANGE_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(GREEN_SOURCE_STATUS_CHANGE_RULE,
				"listen for updates in green source connection state",
				"updating connection state between server and green source");
	}

	@Override
	public AgentRule copy() {
		return new ListenForGreenSourceServiceUpdateRule(controller, getRuleSet());
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
