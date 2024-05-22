package org.greencloud.agentsystem.strategies.deault.rules.server.job.listening.startcheck;

import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_CHECK_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_HANDLER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_JOB_STATUS_CHECK_REQUEST_TEMPLATE;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentMessageListenerRule;
import org.jrba.rulesengine.ruleset.RuleSet;

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
				"listening for RMA message checking job start status");
	}

	@Override
	public AgentRule copy() {
		return new ListenForJobStartCheckRequestRule(controller, getRuleSet());
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
