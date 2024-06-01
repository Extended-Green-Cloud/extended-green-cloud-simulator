package org.greencloud.agentsystem.strategies.deafult.rules.greenenergy.job.listening.supplyupdate;

import static org.greencloud.commons.args.agent.EGCSAgentType.GREEN_ENERGY;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageTemplatesConstants.LISTEN_FOR_POWER_SUPPLY_UPDATE_TEMPLATE;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentMessageListenerRule;
import org.jrba.rulesengine.ruleset.RuleSet;

public class ListenForPowerSupplyStatusUpdateRule extends
		AgentMessageListenerRule<GreenEnergyAgentProps, GreenEnergyNode> {

	public ListenForPowerSupplyStatusUpdateRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller, final RuleSet ruleSet) {
		super(controller, ruleSet, LISTEN_FOR_POWER_SUPPLY_UPDATE_TEMPLATE, 30,
				JOB_STATUS_RECEIVER_HANDLER_RULE);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_RULE,
				"listen for power supply updates",
				"listening for new updates regarding provided power supply coming from Server");
	}

	@Override
	public AgentRule copy() {
		return new ListenForPowerSupplyStatusUpdateRule(controller, getRuleSet());
	}

	@Override
	public String getAgentType() {
		return GREEN_ENERGY.getName();
	}
}
