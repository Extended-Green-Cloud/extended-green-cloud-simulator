package org.greencloud.agentsystem.strategies.rulesets.allocation.prioritystadardonestep.rules.server.job.listening.allocation;

import static java.lang.String.valueOf;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PREPARE_DATA_FOR_JOB_ALLOCATION_RULE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareRefuseReply;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareReply;
import static org.greencloud.commons.utils.time.TimeConverter.convertToHourDuration;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

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
import org.slf4j.Logger;
import org.slf4j.MDC;

public class PrepareServerPriceEstimationDataRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(PrepareServerPriceEstimationDataRule.class);

	public PrepareServerPriceEstimationDataRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PREPARE_DATA_FOR_JOB_ALLOCATION_RULE,
				"prepares data for job allocation - evaluates job prices",
				"rule run when RMA prepares data for job allocation");
	}

	@Override
	public boolean evaluateRule(RuleSetFacts facts) {
		if (agentProps.isHasError()) {
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
			logger.info("Server has an ongoing error. Refusing to consider jobs for execution.");

			agent.send(prepareRefuseReply(facts.get(MESSAGE)));
			return false;
		}
		return true;
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final AllocatedJobs jobs = facts.get(JOBS);

		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Estimated jobs prices in response to scheduled allocation.");

		final Map<String, Double> jobsPrices = jobs.getAllocationJobs().stream()
				.collect(toMap(PowerJob::getJobId, this::estimatePrice));
		final ServerPriceEstimation serverPriceEstimation = ImmutableServerPriceEstimation.builder()
				.jobsPrices(jobsPrices)
				.averageGreenEnergyUtilization(agentNode.getAverageJobEnergyUtilization(agentProps))
				.build();

		agent.send(prepareReply(facts.get(MESSAGE), serverPriceEstimation, facts.get(MESSAGE_TYPE)));
	}

	private Double estimatePrice(final ClientJob job) {
		return convertToHourDuration(job.getDuration()) * agentProps.getPricePerHour();
	}

	@Override
	public AgentRule copy() {
		return new PrepareServerPriceEstimationDataRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
