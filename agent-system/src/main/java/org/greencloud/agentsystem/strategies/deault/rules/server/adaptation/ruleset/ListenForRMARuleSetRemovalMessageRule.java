package org.greencloud.agentsystem.strategies.deault.rules.server.adaptation.ruleset;

import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_RULE_SET_REMOVAL_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_RULE_SET_REMOVAL_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_RULE_SET_REMOVAL_REQUEST;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentMessageListenerRule;
import org.jrba.rulesengine.ruleset.RuleSet;

public class ListenForRMARuleSetRemovalMessageRule extends AgentMessageListenerRule<ServerAgentProps, ServerNode> {

	public ListenForRMARuleSetRemovalMessageRule(final RulesController<ServerAgentProps, ServerNode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, LISTEN_FOR_RULE_SET_REMOVAL_REQUEST, 1, LISTEN_FOR_RULE_SET_REMOVAL_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_RULE_SET_REMOVAL_RULE,
				"listen for rule set update messages",
				"listening for messages from RMA asking Server to remove given rule set");
	}
}
