package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.resource;

import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SERVER_RESOURCE_UPDATE_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_SERVER_RESOURCE_UPDATE_TEMPLATE;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.agent.ServerResources;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentMessageListenerRule;
import org.jrba.rulesengine.ruleset.RuleSet;

public class ListenForServerResourceUpdateRule extends
		AgentMessageListenerRule<RegionalManagerAgentProps, RMANode> {

	public ListenForServerResourceUpdateRule(
			final RulesController<RegionalManagerAgentProps, RMANode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, ServerResources.class, LISTEN_FOR_SERVER_RESOURCE_UPDATE_TEMPLATE, 1,
				"SERVER_RESOURCE_UPDATE_HANDLER_RULE");
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(SERVER_RESOURCE_UPDATE_RULE,
				"listen for information about update of resources in one of connected servers",
				"rule run when one of the Servers sends information about update in its resources");
	}

	@Override
	public AgentRule copy() {
		return new ListenForServerResourceUpdateRule(controller, getRuleSet());
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
