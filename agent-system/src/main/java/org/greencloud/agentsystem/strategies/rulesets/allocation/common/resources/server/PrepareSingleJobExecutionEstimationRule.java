package org.greencloud.agentsystem.strategies.rulesets.allocation.common.resources.server;

import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PREPARE_JOB_EXECUTION_ESTIMATION_RULE;
import static org.greencloud.commons.utils.facts.ValidatorFactsFactory.constructFactsForServerValidation;
import static org.greencloud.commons.utils.time.TimeConverter.convertToHourDuration;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;

import java.time.Instant;

import org.apache.commons.lang3.tuple.Pair;
import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.allocation.AllocatedJobs;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.ImmutableJobWithExecutionEstimation;
import org.greencloud.commons.domain.job.extended.JobWithExecutionEstimation;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

public class PrepareSingleJobExecutionEstimationRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	public PrepareSingleJobExecutionEstimationRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PREPARE_JOB_EXECUTION_ESTIMATION_RULE,
				"prepares data for job allocation - evaluates single job execution",
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
		final ClientJob job = jobs.getAllocationJobs().getFirst();

		final Pair<Instant, Double> execution = agentProps.getEstimatedEarliestJobStartTimeAndDuration(job);
		final Double price = convertToHourDuration(execution.getValue().longValue()) * agentProps.getPricePerHour();

		final JobWithExecutionEstimation jobsEstimation = ImmutableJobWithExecutionEstimation.builder()
				.estimatedDuration(execution.getValue().longValue())
				.estimatedPrice(price)
				.earliestStartTime(execution.getKey())
				.build();

		facts.put(RESULT, jobsEstimation);
	}

	@Override
	public AgentRule copy() {
		return new PrepareSingleJobExecutionEstimationRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
