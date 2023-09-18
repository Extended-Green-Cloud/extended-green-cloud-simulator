package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.client.job.listening;

import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.rulescontroller.rule.combined.domain.AgentCombinedRuleType.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.combined.AgentCombinedRule;
import org.greencloud.rulescontroller.strategies.defaultstrategy.rules.client.job.listening.processing.ProcessSchedulerDelayedJobUpdateRule;
import org.greencloud.rulescontroller.strategies.defaultstrategy.rules.client.job.listening.processing.ProcessSchedulerFailedJobUpdateRule;
import org.greencloud.rulescontroller.strategies.defaultstrategy.rules.client.job.listening.processing.ProcessSchedulerFinishedJobUpdateRule;
import org.greencloud.rulescontroller.strategies.defaultstrategy.rules.client.job.listening.processing.ProcessSchedulerJobOnBackUpUpdateRule;
import org.greencloud.rulescontroller.strategies.defaultstrategy.rules.client.job.listening.processing.ProcessSchedulerJobOnGreenEnergyUpdateRule;
import org.greencloud.rulescontroller.strategies.defaultstrategy.rules.client.job.listening.processing.ProcessSchedulerJobOnHoldUpdateRule;
import org.greencloud.rulescontroller.strategies.defaultstrategy.rules.client.job.listening.processing.ProcessSchedulerPostponeJobUpdateRule;
import org.greencloud.rulescontroller.strategies.defaultstrategy.rules.client.job.listening.processing.ProcessSchedulerProcessingJobUpdateRule;
import org.greencloud.rulescontroller.strategies.defaultstrategy.rules.client.job.listening.processing.ProcessSchedulerReScheduleJobUpdateRule;
import org.greencloud.rulescontroller.strategies.defaultstrategy.rules.client.job.listening.processing.ProcessSchedulerScheduledJobUpdateRule;
import org.greencloud.rulescontroller.strategies.defaultstrategy.rules.client.job.listening.processing.ProcessSchedulerStartedJobUpdateRule;

import org.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import com.gui.agents.client.ClientNode;

public class ProcessSchedulerJobStatusUpdateRule extends AgentCombinedRule<ClientAgentProps, ClientNode> {

	public ProcessSchedulerJobStatusUpdateRule(final RulesController<ClientAgentProps, ClientNode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE,
				"handling job status update",
				"triggers handlers upon job status updates");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessSchedulerStartedJobUpdateRule(controller),
				new ProcessSchedulerFinishedJobUpdateRule(controller),
				new ProcessSchedulerFailedJobUpdateRule(controller),
				new ProcessSchedulerPostponeJobUpdateRule(controller),
				new ProcessSchedulerScheduledJobUpdateRule(controller),
				new ProcessSchedulerProcessingJobUpdateRule(controller),
				new ProcessSchedulerDelayedJobUpdateRule(controller),
				new ProcessSchedulerJobOnBackUpUpdateRule(controller),
				new ProcessSchedulerJobOnGreenEnergyUpdateRule(controller),
				new ProcessSchedulerJobOnHoldUpdateRule(controller),
				new ProcessSchedulerReScheduleJobUpdateRule(controller)
		);
	}
}
