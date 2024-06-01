package org.greencloud.agentsystem.strategies.deafult.rules.greenenergy.events.transfer.processing;

import static org.greencloud.commons.args.agent.EGCSAgentType.GREEN_ENERGY;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.REFUSED_TRANSFER_JOB_RULE;
import static org.jrba.rulesengine.types.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;

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

	@Override
	public AgentRule copy() {
		return new ProcessTransferRefuseCombinedRule(controller);
	}

	@Override
	public String getAgentType() {
		return GREEN_ENERGY.getName();
	}
}
