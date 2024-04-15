package org.greencloud.agentsystem.strategies.deault.rules.client.job.listening;

import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_SCHEDULER_JOB_STATUS_UPDATE_TEMPLATE;

import org.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import org.greencloud.gui.agents.client.ClientNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentMessageListenerRule;
import org.jrba.rulesengine.ruleset.RuleSet;

public class ListenForSchedulerJobStatusUpdateRule extends AgentMessageListenerRule<ClientAgentProps, ClientNode> {

	public ListenForSchedulerJobStatusUpdateRule(final RulesController<ClientAgentProps, ClientNode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, LISTEN_FOR_SCHEDULER_JOB_STATUS_UPDATE_TEMPLATE, 1,
				JOB_STATUS_RECEIVER_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_RULE,
				"listen for job status update",
				"triggers handlers upon job status updates");
	}
}
