package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.startcheck;

import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_CHECK_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_HANDLER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_JOB_STATUS_CHECK_REQUEST_TEMPLATE;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.gui.agents.server.ServerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;
import org.greencloud.rulescontroller.ruleset.RuleSet;

public class ListenForJobStartCheckRequestRule extends AgentMessageListenerRule<ServerAgentProps, ServerNode> {

	public ListenForJobStartCheckRequestRule(final RulesController<ServerAgentProps, ServerNode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, null, LISTEN_FOR_JOB_STATUS_CHECK_REQUEST_TEMPLATE, 20,
				JOB_STATUS_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_CHECK_RULE,
				"listen for start check request",
				"listening for CNA message checking job start status");
	}
}
