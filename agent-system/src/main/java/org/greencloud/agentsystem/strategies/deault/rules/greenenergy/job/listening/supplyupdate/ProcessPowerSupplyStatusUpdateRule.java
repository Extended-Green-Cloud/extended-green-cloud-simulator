package org.greencloud.agentsystem.strategies.deault.rules.greenenergy.job.listening.supplyupdate;

import static org.greencloud.commons.args.agent.EGCSAgentType.GREEN_ENERGY;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.jrba.rulesengine.types.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.agentsystem.strategies.deault.rules.greenenergy.job.listening.supplyupdate.processing.ProcessPowerSupplyFinishRule;
import org.greencloud.agentsystem.strategies.deault.rules.greenenergy.job.listening.supplyupdate.processing.ProcessPowerSupplyStartRule;
import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;

public class ProcessPowerSupplyStatusUpdateRule extends AgentCombinedRule<GreenEnergyAgentProps, GreenEnergyNode> {

	public ProcessPowerSupplyStatusUpdateRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE,
				"handles power supply updates",
				"handling new updates regarding provided power supply coming from Server");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessPowerSupplyFinishRule(controller),
				new ProcessPowerSupplyStartRule(controller)
		);
	}

	@Override
	public AgentRule copy() {
		return new ProcessPowerSupplyStatusUpdateRule(controller);
	}

	@Override
	public String getAgentType() {
		return GREEN_ENERGY.getName();
	}
}
