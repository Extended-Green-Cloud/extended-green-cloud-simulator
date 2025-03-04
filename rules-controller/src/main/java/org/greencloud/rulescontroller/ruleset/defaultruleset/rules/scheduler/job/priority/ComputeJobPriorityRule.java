package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.priority;

import static java.time.Duration.between;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.CPU;
import static org.greencloud.commons.enums.rules.RuleType.COMPUTE_JOB_PRIORITY_RULE;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.scheduler.SchedulerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;

public class ComputeJobPriorityRule extends AgentBasicRule<SchedulerAgentProps, SchedulerNode> {

	public ComputeJobPriorityRule(final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
		super(controller);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);

		final double timeToDeadline = between(job.getEndTime(), job.getDeadline()).toMillis();
		final double result = agentProps.getDeadlinePercentage() * timeToDeadline
				+ agentProps.getCPUPercentage() * job.getRequiredResources().get(CPU).getAmountInCommonUnit();

		facts.put(RESULT, result);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(COMPUTE_JOB_PRIORITY_RULE,
				"compute priority of client job",
				"when Scheduler receives new job, it computes its priority based on CPU and deadline");
	}
}
