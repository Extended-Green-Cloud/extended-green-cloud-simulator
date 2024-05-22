package org.greencloud.agentsystem.strategies.deault.rules.client.job.listening;

import static org.greencloud.commons.args.agent.EGCSAgentType.CLIENT;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.jrba.rulesengine.types.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.agentsystem.strategies.deault.rules.client.job.listening.processing.ProcessCMAAcceptedJobUpdateRule;
import org.greencloud.agentsystem.strategies.deault.rules.client.job.listening.processing.ProcessCMADelayedJobUpdateRule;
import org.greencloud.agentsystem.strategies.deault.rules.client.job.listening.processing.ProcessCMAFailedJobUpdateRule;
import org.greencloud.agentsystem.strategies.deault.rules.client.job.listening.processing.ProcessCMAFinishedJobUpdateRule;
import org.greencloud.agentsystem.strategies.deault.rules.client.job.listening.processing.ProcessCMAJobExecutorUpdateRule;
import org.greencloud.agentsystem.strategies.deault.rules.client.job.listening.processing.ProcessCMAJobOnBackUpUpdateRule;
import org.greencloud.agentsystem.strategies.deault.rules.client.job.listening.processing.ProcessCMAJobOnGreenEnergyUpdateRule;
import org.greencloud.agentsystem.strategies.deault.rules.client.job.listening.processing.ProcessCMAJobOnHoldUpdateRule;
import org.greencloud.agentsystem.strategies.deault.rules.client.job.listening.processing.ProcessCMAProcessingJobUpdateRule;
import org.greencloud.agentsystem.strategies.deault.rules.client.job.listening.processing.ProcessCMAScheduledJobUpdateRule;
import org.greencloud.agentsystem.strategies.deault.rules.client.job.listening.processing.ProcessCMAStartedJobUpdateRule;
import org.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import org.greencloud.gui.agents.client.ClientNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;

public class ProcessCMAJobStatusUpdateRule extends AgentCombinedRule<ClientAgentProps, ClientNode> {

	public ProcessCMAJobStatusUpdateRule(final RulesController<ClientAgentProps, ClientNode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE,
				"handling job status update.",
				"triggers handlers upon job status updates.");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessCMAStartedJobUpdateRule(controller),
				new ProcessCMAFinishedJobUpdateRule(controller),
				new ProcessCMAFailedJobUpdateRule(controller),
				new ProcessCMAScheduledJobUpdateRule(controller),
				new ProcessCMAProcessingJobUpdateRule(controller),
				new ProcessCMADelayedJobUpdateRule(controller),
				new ProcessCMAJobOnBackUpUpdateRule(controller),
				new ProcessCMAJobOnGreenEnergyUpdateRule(controller),
				new ProcessCMAJobOnHoldUpdateRule(controller),
				new ProcessCMAJobExecutorUpdateRule(controller),
				new ProcessCMAAcceptedJobUpdateRule(controller)
		);
	}

	@Override
	public AgentRule copy() {
		return new ProcessCMAJobStatusUpdateRule(controller);
	}

	@Override
	public String getAgentType() {
		return CLIENT.getName();
	}
}
