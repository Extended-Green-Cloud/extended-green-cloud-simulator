package org.greencloud.agentsystem.strategies.deafult.rules.greenenergy.job.listening.newjob;

import static org.greencloud.commons.args.agent.EGCSAgentType.GREEN_ENERGY;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.jrba.rulesengine.types.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.agentsystem.strategies.deafult.rules.greenenergy.job.listening.newjob.processing.ProcessNewPowerSupplyRequestRule;
import org.greencloud.agentsystem.strategies.deafult.rules.greenenergy.job.listening.newjob.processing.ProcessRefusePowerSupplyDueToErrorRule;
import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;

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

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessRefusePowerSupplyDueToErrorRule(controller),
				new ProcessNewPowerSupplyRequestRule(controller)
		);
	}

	@Override
	public AgentRule copy() {
		return new ProcessServerNewJobCombinedRule(controller);
	}

	@Override
	public String getAgentType() {
		return GREEN_ENERGY.getName();
	}
}
