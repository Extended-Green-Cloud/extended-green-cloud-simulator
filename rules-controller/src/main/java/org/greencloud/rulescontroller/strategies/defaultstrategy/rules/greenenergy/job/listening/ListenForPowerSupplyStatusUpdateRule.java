package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.greenenergy.job.listening;

import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_POWER_SUPPLY_UPDATE_TEMPLATE;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentMessageListenerRule;
import org.greencloud.rulescontroller.strategy.Strategy;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import com.gui.agents.greenenergy.GreenEnergyNode;

public class ListenForPowerSupplyStatusUpdateRule extends
		AgentMessageListenerRule<GreenEnergyAgentProps, GreenEnergyNode> {

	public ListenForPowerSupplyStatusUpdateRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller, final Strategy strategy) {
		super(controller, strategy, JobWithStatus.class, LISTEN_FOR_POWER_SUPPLY_UPDATE_TEMPLATE, 30,
				JOB_STATUS_RECEIVER_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_RULE,
				"listen for power supply updates",
				"listening for new updates regarding provided power supply coming from Server");
	}
}