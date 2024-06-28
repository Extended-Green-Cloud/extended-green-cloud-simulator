package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.job.proposing;

import static org.greencloud.commons.args.agent.EGCSAgentType.GREEN_ENERGY;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_SCHEDULE_POWER_SUPPLY_RULE;
import static org.jrba.rulesengine.types.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.job.proposing.processing.ProcessNotEnoughResourcesBeforePowerSupplyRule;
import org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.greenenergy.job.proposing.processing.ProcessPowerSupplyConfirmationRule;
import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;

public class ProcessProposeToServerAcceptResponseRule
		extends AgentCombinedRule<GreenEnergyAgentProps, GreenEnergyNode> {

	public ProcessProposeToServerAcceptResponseRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller, EXECUTE_FIRST);
	}

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

	@Override
	public AgentRule copy() {
		return new ProcessProposeToServerAcceptResponseRule(controller);
	}

	@Override
	public String getAgentType() {
		return GREEN_ENERGY.getName();
	}
}
