package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.job.proposing;

import static org.greencloud.commons.enums.rules.RuleType.PROCESS_SCHEDULE_POWER_SUPPLY_RULE;
import static org.greencloud.rulescontroller.rule.combined.domain.AgentCombinedRuleType.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.job.proposing.processing.ProcessNotEnoughResourcesBeforePowerSupplyRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.job.proposing.processing.ProcessPowerSupplyConfirmationRule;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.combined.AgentCombinedRule;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;

import com.gui.agents.greenenergy.GreenEnergyNode;

public class ProcessProposeToServerAcceptResponseRule
		extends AgentCombinedRule<GreenEnergyAgentProps, GreenEnergyNode> {

	public ProcessProposeToServerAcceptResponseRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	/**
	 * Method initialize default rule metadata
	 *
	 * @return rule description
	 */
	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROCESS_SCHEDULE_POWER_SUPPLY_RULE,
				"handle accept propose from Server",
				"rule handlers Accept Proposal message to given power supply offer");
	}

	/**
	 * Method construct set of rules that are to be combined
	 */
	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessNotEnoughResourcesBeforePowerSupplyRule(controller),
				new ProcessPowerSupplyConfirmationRule(controller)
		);
	}
}
