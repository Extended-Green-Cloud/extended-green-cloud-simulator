package org.greencloud.agentsystem.strategies.rulesets.allocation.common.resources.server;

import static java.util.stream.Collectors.toMap;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PREPARE_JOB_PRICE_ESTIMATION_RULE;
import static org.greencloud.commons.utils.facts.ValidatorFactsFactory.constructFactsForServerValidation;
import static org.greencloud.commons.utils.time.TimeConverter.convertToHourDuration;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;

import java.util.Map;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.agent.ImmutableServerPriceEstimation;
import org.greencloud.commons.domain.agent.ServerPriceEstimation;
import org.greencloud.commons.domain.allocation.AllocatedJobs;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.basic.PowerJob;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

public class PrepareJobPriceEstimationRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	public PrepareJobPriceEstimationRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PREPARE_JOB_PRICE_ESTIMATION_RULE,
				"prepares data for job allocation - evaluates job prices",
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
		final Map<String, Double> jobsPrices = jobs.getAllocationJobs().stream()
				.collect(toMap(PowerJob::getJobId, this::estimatePrice));
		final ServerPriceEstimation serverPriceEstimation = ImmutableServerPriceEstimation.builder()
				.jobsPrices(jobsPrices)
				.averageGreenEnergyUtilization(agentNode.getAverageJobEnergyUtilization(agentProps))
				.build();

		facts.put(RESULT, serverPriceEstimation);
	}

	private Double estimatePrice(final ClientJob job) {
		return convertToHourDuration(job.getDuration()) * agentProps.getPricePerHour();
	}

	@Override
	public AgentRule copy() {
		return new PrepareJobPriceEstimationRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
