package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.job.listening;

import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.rulescontroller.rule.combined.domain.AgentCombinedRuleType.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.combined.AgentCombinedRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.job.listening.processing.ProcessNewPowerSupplyRequestRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.job.listening.processing.ProcessRefusePowerSupplyDueToErrorRule;

public class ProcessServerNewJobCombinedRule extends AgentCombinedRule<GreenEnergyAgentProps, GreenEnergyNode> {

	public ProcessServerNewJobCombinedRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_HANDLER_RULE,
				"handles new Server power supply request",
				"handling new request for power supply coming from Server");
	}

	/**
	 * Method construct set of rules that are to be combined
	 */
	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessRefusePowerSupplyDueToErrorRule(controller),
				new ProcessNewPowerSupplyRequestRule(controller)
		);
	}
}
