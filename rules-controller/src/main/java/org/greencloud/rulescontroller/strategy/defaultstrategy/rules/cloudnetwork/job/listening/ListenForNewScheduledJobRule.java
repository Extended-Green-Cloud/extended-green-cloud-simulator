package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.cloudnetwork.job.listening;

import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_NEW_SCHEDULED_JOB_TEMPLATE;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;
import org.greencloud.rulescontroller.strategy.Strategy;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import com.gui.agents.cloudnetwork.CloudNetworkNode;

public class ListenForNewScheduledJobRule extends AgentMessageListenerRule<CloudNetworkAgentProps, CloudNetworkNode> {

	public ListenForNewScheduledJobRule(final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller,
			final Strategy strategy) {
		super(controller, strategy, ClientJob.class, LISTEN_FOR_NEW_SCHEDULED_JOB_TEMPLATE, 50,
				NEW_JOB_RECEIVER_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_RULE,
				"listen for new scheduled jobs",
				"rule run when Scheduler sends new job to CNA");
	}
}
