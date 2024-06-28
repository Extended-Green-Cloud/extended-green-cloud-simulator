package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.adaptation.ruleset;

import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_RULE_SET_UPDATE_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_RULE_SET_UPDATE_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_RULE_SET_UPDATE_REQUEST;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentMessageListenerRule;
import org.jrba.rulesengine.ruleset.RuleSet;
import org.jrba.rulesengine.ruleset.domain.RuleSetUpdate;

public class ListenForRuleSetUpdateRequestRule extends AgentMessageListenerRule<ServerAgentProps, ServerNode> {

	public ListenForRuleSetUpdateRequestRule(final RulesController<ServerAgentProps, ServerNode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, RuleSetUpdate.class, LISTEN_FOR_RULE_SET_UPDATE_REQUEST, 1,
				LISTEN_FOR_RULE_SET_UPDATE_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_RULE_SET_UPDATE_RULE,
				"listen for rule set update messages",
				"listening for messages from RMA asking Server to update its rule set");
	}

	@Override
	public AgentRule copy() {
		return new ListenForRuleSetUpdateRequestRule(controller, getRuleSet());
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
