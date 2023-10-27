package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.shortagegreensource;

import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_JOB_TRANSFER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_JOB_TRANSFER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_POWER_SHORTAGE_TRANSFER_REQUEST_TEMPLATE;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;
import org.greencloud.rulescontroller.ruleset.RuleSet;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.transfer.JobPowerShortageTransfer;
import com.gui.agents.server.ServerNode;

public class ListenForPowerShortageTransferRequestRule extends AgentMessageListenerRule<ServerAgentProps, ServerNode> {

	public ListenForPowerShortageTransferRequestRule(
			final RulesController<ServerAgentProps, ServerNode> controller, final RuleSet ruleSet) {
		super(controller, ruleSet, JobPowerShortageTransfer.class, LISTEN_FOR_POWER_SHORTAGE_TRANSFER_REQUEST_TEMPLATE,
				20, LISTEN_FOR_JOB_TRANSFER_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_JOB_TRANSFER_RULE,
				"listen for job transfer Green Source request",
				"rule listens for the transfer request coming from Green Source affected by power shortage");
	}
}
