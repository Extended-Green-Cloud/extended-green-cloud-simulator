package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.job.listening;

import static org.greencloud.commons.enums.rules.RuleType.JOB_ENERGY_PRICE_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_ENERGY_PRICE_RECEIVER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_PRICE_UPDATE_TEMPLATE;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.job.instance.JobInstanceWithPrice;
import org.greencloud.gui.agents.cloudnetwork.CloudNetworkNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;
import org.greencloud.rulescontroller.ruleset.RuleSet;

public class ListenForJobPriceUpdateRule extends AgentMessageListenerRule<CloudNetworkAgentProps, CloudNetworkNode> {

	public ListenForJobPriceUpdateRule(final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller,
			final RuleSet ruleSet) {
		super(controller, ruleSet, JobInstanceWithPrice.class, LISTEN_FOR_PRICE_UPDATE_TEMPLATE,
				10, JOB_ENERGY_PRICE_RECEIVER_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_ENERGY_PRICE_RECEIVER_RULE,
				"listen for updates regarding job execution price",
				"listening for messages received from Server informing about job execution price");
	}
}
