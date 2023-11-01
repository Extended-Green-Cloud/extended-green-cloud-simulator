package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.adaptation.ruleset;

import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_RULE_SET_REMOVAL_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_RULE_SET_REMOVAL_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_RULE_SET_REMOVAL_REQUEST;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.gui.agents.server.ServerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;
import org.greencloud.rulescontroller.ruleset.RuleSet;

public class ListenForCNARuleSetRemovalMessageRule extends AgentMessageListenerRule<ServerAgentProps, ServerNode> {

	public ListenForCNARuleSetRemovalMessageRule(final RulesController<ServerAgentProps, ServerNode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, LISTEN_FOR_RULE_SET_REMOVAL_REQUEST, 1, LISTEN_FOR_RULE_SET_REMOVAL_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_RULE_SET_REMOVAL_RULE,
				"listen for rule set update messages",
				"listening for messages from CNA asking Server to remove given rule set");
	}
}
