package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.client.job.listening;

import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_SCHEDULER_JOB_STATUS_UPDATE_TEMPLATE;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;
import org.greencloud.rulescontroller.strategy.Strategy;

import org.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import com.gui.agents.client.ClientNode;

public class ListenForSchedulerJobStatusUpdateRule extends AgentMessageListenerRule<ClientAgentProps, ClientNode> {

	public ListenForSchedulerJobStatusUpdateRule(final RulesController<ClientAgentProps, ClientNode> controller,
			final Strategy strategy) {
		super(controller, strategy, LISTEN_FOR_SCHEDULER_JOB_STATUS_UPDATE_TEMPLATE, 1,
				JOB_STATUS_RECEIVER_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_RULE,
				"listen for job status update",
				"triggers handlers upon job status updates");
	}
}
