package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.client.job.listening;

import static org.greencloud.commons.args.agent.EGCSAgentType.CLIENT;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_CMA_JOB_STATUS_UPDATE_TEMPLATE;

import org.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import org.greencloud.gui.agents.client.ClientNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentMessageListenerRule;
import org.jrba.rulesengine.ruleset.RuleSet;

public class ListenForCMAJobStatusUpdateRule extends AgentMessageListenerRule<ClientAgentProps, ClientNode> {

	public ListenForCMAJobStatusUpdateRule(final RulesController<ClientAgentProps, ClientNode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, LISTEN_FOR_CMA_JOB_STATUS_UPDATE_TEMPLATE, 1, JOB_STATUS_RECEIVER_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_RULE,
				"listen for job status update.",
				"triggers handlers upon job status updates.");
	}

	@Override
	public AgentRule copy() {
		return new ListenForCMAJobStatusUpdateRule(controller, getRuleSet());
	}

	@Override
	public String getAgentType() {
		return CLIENT.getName();
	}
}
