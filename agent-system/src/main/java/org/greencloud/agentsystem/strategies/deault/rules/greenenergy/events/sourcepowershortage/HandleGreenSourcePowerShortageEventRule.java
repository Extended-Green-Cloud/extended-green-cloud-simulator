package org.greencloud.agentsystem.strategies.deault.rules.greenenergy.events.sourcepowershortage;

import static org.greencloud.commons.args.agent.EGCSAgentType.GREEN_ENERGY;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.POWER_SHORTAGE_ERROR_RULE;
import static org.jrba.rulesengine.types.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.agentsystem.strategies.deault.rules.greenenergy.events.sourcepowershortage.processing.ProcessGreenSourcePowerShortageFinishEventRule;
import org.greencloud.agentsystem.strategies.deault.rules.greenenergy.events.sourcepowershortage.processing.ProcessGreenSourcePowerShortageStartEventRule;
import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;

public class HandleGreenSourcePowerShortageEventRule extends AgentCombinedRule<GreenEnergyAgentProps, GreenEnergyNode> {

	public HandleGreenSourcePowerShortageEventRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(POWER_SHORTAGE_ERROR_RULE,
				"handle power shortage event",
				"rule handles different cases of power shortage event");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessGreenSourcePowerShortageFinishEventRule(controller),
				new ProcessGreenSourcePowerShortageStartEventRule(controller)
		);
	}

	@Override
	public AgentRule copy() {
		return new HandleGreenSourcePowerShortageEventRule(controller);
	}

	@Override
	public String getAgentType() {
		return GREEN_ENERGY.getName();
	}
}
