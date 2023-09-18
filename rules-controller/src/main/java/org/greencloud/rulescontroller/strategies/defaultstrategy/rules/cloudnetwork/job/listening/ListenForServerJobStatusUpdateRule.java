package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.cloudnetwork.job.listening;

import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_SERVER_JOB_STATUS_UPDATE_TEMPLATE;

import org.greencloud.rulescontroller.strategy.Strategy;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import com.gui.agents.cloudnetwork.CloudNetworkNode;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;

public class ListenForServerJobStatusUpdateRule extends AgentMessageListenerRule<CloudNetworkAgentProps, CloudNetworkNode> {

	public ListenForServerJobStatusUpdateRule(final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller,
			final Strategy strategy) {
		super(controller, strategy, JobWithStatus.class, LISTEN_FOR_SERVER_JOB_STATUS_UPDATE_TEMPLATE, 50,
				JOB_STATUS_RECEIVER_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_RULE,
				"listen for updates regarding execution of the job in server",
				"rule run when Server sends update regarding job execution status");
	}
}
