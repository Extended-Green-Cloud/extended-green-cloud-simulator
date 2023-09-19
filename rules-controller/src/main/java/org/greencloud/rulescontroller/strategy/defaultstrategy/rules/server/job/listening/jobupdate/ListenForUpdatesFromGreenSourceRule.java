package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.server.job.listening.jobupdate;

import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_GREEN_SOURCE_POWER_SUPPLY_UPDATE_TEMPLATE;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.strategy.Strategy;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import com.gui.agents.server.ServerNode;

import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;

public class ListenForUpdatesFromGreenSourceRule extends AgentMessageListenerRule<ServerAgentProps, ServerNode> {

	public ListenForUpdatesFromGreenSourceRule(final RulesController<ServerAgentProps, ServerNode> controller,
			final Strategy strategy) {
		super(controller, strategy, JobInstanceIdentifier.class, LISTEN_FOR_GREEN_SOURCE_POWER_SUPPLY_UPDATE_TEMPLATE,
				20, JOB_STATUS_RECEIVER_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_RULE,
				"listen for updates regarding job execution",
				"listening for messages received from Green Source informing about changes in power supply");
	}
}
