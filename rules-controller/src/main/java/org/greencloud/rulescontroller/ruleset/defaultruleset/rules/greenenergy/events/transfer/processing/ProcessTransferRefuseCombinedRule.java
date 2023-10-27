package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.events.transfer.processing;

import static org.greencloud.commons.enums.rules.RuleType.REFUSED_TRANSFER_JOB_RULE;
import static org.greencloud.rulescontroller.rule.combined.domain.AgentCombinedRuleType.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.combined.AgentCombinedRule;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import com.gui.agents.greenenergy.GreenEnergyNode;

public class ProcessTransferRefuseCombinedRule extends AgentCombinedRule<GreenEnergyAgentProps, GreenEnergyNode> {

	public ProcessTransferRefuseCombinedRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(REFUSED_TRANSFER_JOB_RULE,
				"process refused job transfer request",
				"rule processes refusal of job transfer request in Server");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessTransferRefuseJobNotFoundRule(controller),
				new ProcessTransferRefuseJobAlreadyFinishedRule(controller),
				new ProcessTransferRefuseExistingJobRule(controller)
		);
	}
}
