package org.greencloud.agentsystem.strategies.rulesets.allocation.common.resources.server;

import static java.util.stream.Collectors.toMap;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PREPARE_JOB_EXECUTION_ESTIMATION_RULE;
import static org.greencloud.commons.utils.facts.ValidatorFactsFactory.constructFactsForServerValidation;
import static org.greencloud.commons.utils.time.TimeConverter.convertToHourDuration;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;

import java.util.Map;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.agent.ImmutableServerJobsEstimation;
import org.greencloud.commons.domain.agent.ServerJobsEstimation;
import org.greencloud.commons.domain.allocation.AllocatedJobs;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.basic.PowerJob;
import org.greencloud.commons.domain.job.extended.ImmutableJobWithExecutionEstimation;
import org.greencloud.commons.domain.job.extended.JobWithExecutionEstimation;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

public class PrepareJobsExecutionEstimationRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	public PrepareJobsExecutionEstimationRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PREPARE_JOB_EXECUTION_ESTIMATION_RULE,
				"prepares data for job allocation - evaluates jobs execution",
				"rule run when Server prepares data for job allocation");
	}

	@Override
	public boolean evaluateRule(RuleSetFacts facts) {
		controller.fire(constructFactsForServerValidation(facts));
		return facts.get(RESULT);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final AllocatedJobs jobs = facts.get(JOBS);
		final Map<String, JobWithExecutionEstimation> jobsEstimation = jobs.getAllocationJobs().stream()
				.collect(toMap(PowerJob::getJobId,this::getJobEstimation));
		final ServerJobsEstimation serverEstimation = ImmutableServerJobsEstimation.builder()
				.jobsEstimation(jobsEstimation)
				.averageGreenEnergyUtilization(agentNode.getAverageJobEnergyUtilization(agentProps))
				.serverReliability(agentNode.getComponentSuccessRatio(agent.getAID().getName()))
				.build();

		facts.put(RESULT, serverEstimation);
	}

	private JobWithExecutionEstimation getJobEstimation(final ClientJob job) {
		final long executionTime = agentProps.getEstimatedEarliestJobStartTimeAndDuration(job).getValue().longValue();
		final Double price = convertToHourDuration(executionTime) * agentProps.getPricePerHour();

		return ImmutableJobWithExecutionEstimation.builder()
				.estimatedDuration(executionTime)
				.estimatedPrice(price)
				.build();
	}

	@Override
	public AgentRule copy() {
		return new PrepareJobsExecutionEstimationRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
