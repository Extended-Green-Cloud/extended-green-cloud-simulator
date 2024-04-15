package org.greencloud.agentsystem.strategies.deault.rules.scheduler.job.priority;

import static java.time.Duration.between;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.CPU;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.COMPUTE_JOB_PRIORITY_RULE;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.scheduler.SchedulerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.AgentBasicRule;

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
