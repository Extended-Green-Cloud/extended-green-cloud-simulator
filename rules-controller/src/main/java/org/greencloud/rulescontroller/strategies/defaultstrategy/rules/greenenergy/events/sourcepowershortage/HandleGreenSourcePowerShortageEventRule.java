package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.greenenergy.events.sourcepowershortage;

import static org.greencloud.commons.enums.rules.RuleType.POWER_SHORTAGE_ERROR_RULE;
import static org.greencloud.rulescontroller.rule.combined.domain.AgentCombinedRuleType.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.combined.AgentCombinedRule;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import com.gui.agents.greenenergy.GreenEnergyNode;

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
}