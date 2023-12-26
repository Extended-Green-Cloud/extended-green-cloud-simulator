package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.df.listening;

import static org.greencloud.commons.enums.rules.RuleType.SERVER_STATUS_CHANGE_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.SERVER_STATUS_CHANGE_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_SERVER_STATUS_CHANGE_TEMPLATE;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.gui.agents.regionalmanager.RegionalManagerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;
import org.greencloud.rulescontroller.ruleset.RuleSet;

public class ListenForServerStatusChangeRule
		extends AgentMessageListenerRule<RegionalManagerAgentProps, RegionalManagerNode> {

	public ListenForServerStatusChangeRule(final RulesController<RegionalManagerAgentProps, RegionalManagerNode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, LISTEN_FOR_SERVER_STATUS_CHANGE_TEMPLATE, 1,
				SERVER_STATUS_CHANGE_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(SERVER_STATUS_CHANGE_RULE,
				"listen for change in status of connected servers",
				"rule run when one of the Servers connected to the RMA changes its status");
	}

}
