package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.server.job.listening.manualfinish;

import static org.greencloud.commons.enums.rules.RuleType.JOB_MANUAL_FINISH_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_MANUAL_FINISH_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_MANUAL_FINISH_REQUEST_TEMPLATE;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;
import org.greencloud.rulescontroller.strategy.Strategy;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import com.gui.agents.server.ServerNode;

public class ListenForJobManualFinishRule extends AgentMessageListenerRule<ServerAgentProps, ServerNode> {

	public ListenForJobManualFinishRule(final RulesController<ServerAgentProps, ServerNode> controller,
			final Strategy strategy) {
		super(controller, strategy, JobInstanceIdentifier.class, LISTEN_FOR_MANUAL_FINISH_REQUEST_TEMPLATE, 20,
				JOB_MANUAL_FINISH_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_MANUAL_FINISH_RULE,
				"listen for job manual finish",
				"listening for message about Job manual finish sent by Green Source");
	}
}
