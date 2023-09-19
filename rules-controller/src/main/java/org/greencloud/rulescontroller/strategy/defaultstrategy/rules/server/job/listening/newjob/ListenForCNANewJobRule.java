package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.server.job.listening.newjob;

import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_CNA_NEW_JOB_TEMPLATE;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;
import org.greencloud.rulescontroller.strategy.Strategy;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import com.gui.agents.server.ServerNode;

public class ListenForCNANewJobRule extends AgentMessageListenerRule<ServerAgentProps, ServerNode> {

	public ListenForCNANewJobRule(final RulesController<ServerAgentProps, ServerNode> controller,
			final Strategy strategy) {
		super(controller, strategy, ClientJob.class, LISTEN_FOR_CNA_NEW_JOB_TEMPLATE, 20,
				NEW_JOB_RECEIVER_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_RULE,
				"listen for new CNA job request",
				"listening for new job sent by CNA");
	}
}
