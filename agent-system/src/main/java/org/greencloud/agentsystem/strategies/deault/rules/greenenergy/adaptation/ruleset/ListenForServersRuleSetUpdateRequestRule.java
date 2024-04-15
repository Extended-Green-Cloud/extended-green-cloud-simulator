package org.greencloud.agentsystem.strategies.deault.rules.greenenergy.adaptation.ruleset;

import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_RULE_SET_UPDATE_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_RULE_SET_UPDATE_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_RULE_SET_UPDATE_REQUEST;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentMessageListenerRule;
import org.jrba.rulesengine.ruleset.RuleSet;
import org.jrba.rulesengine.ruleset.domain.RuleSetUpdate;

public class ListenForServersRuleSetUpdateRequestRule
		extends AgentMessageListenerRule<GreenEnergyAgentProps, GreenEnergyNode> {

	public ListenForServersRuleSetUpdateRequestRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, RuleSetUpdate.class, LISTEN_FOR_RULE_SET_UPDATE_REQUEST, 1,
				LISTEN_FOR_RULE_SET_UPDATE_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_RULE_SET_UPDATE_RULE,
				"listen for rule set update messages",
				"listening for messages from Server asking Green Source to update its rule set");
	}
}
