package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.errorhandling.listening;

import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_JOB_TRANSFER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_JOB_TRANSFER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_SERVER_TRANSFER_REQUEST_TEMPLATE;

import org.greencloud.rulescontroller.ruleset.RuleSet;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.job.transfer.JobPowerShortageTransfer;
import com.gui.agents.cloudnetwork.CloudNetworkNode;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;

public class ListenForTransferRequestRule extends AgentMessageListenerRule<CloudNetworkAgentProps, CloudNetworkNode> {

	public ListenForTransferRequestRule(final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, JobPowerShortageTransfer.class, LISTEN_FOR_SERVER_TRANSFER_REQUEST_TEMPLATE, 50,
				LISTEN_FOR_JOB_TRANSFER_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_JOB_TRANSFER_RULE,
				"transfer job listener",
				"listens for request to transfer job from one Server to another");
	}
}
