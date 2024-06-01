package org.greencloud.agentsystem.strategies.deafult.rules.server.events.shortagegreensource;

import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_JOB_TRANSFER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_JOB_TRANSFER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_POWER_SHORTAGE_TRANSFER_REQUEST_TEMPLATE;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.transfer.JobPowerShortageTransfer;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentMessageListenerRule;
import org.jrba.rulesengine.ruleset.RuleSet;

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

	@Override
	public AgentRule copy() {
		return new ListenForPowerShortageTransferRequestRule(controller, getRuleSet());
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
