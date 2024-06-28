package org.greencloud.agentsystem.strategies.rulesets.allocation.budgetdeadlineonestep.rules.server.job.listening.allocation;

import static java.lang.String.valueOf;
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
import org.slf4j.Logger;
import org.slf4j.MDC;

public class PrepareServerJobEstimationDataRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(PrepareServerJobEstimationDataRule.class);

	public PrepareServerJobEstimationDataRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PREPARE_DATA_FOR_JOB_ALLOCATION_RULE,
				"prepares data estimating job execution",
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
		final ClientJob job = jobs.getAllocationJobs().getFirst();

		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Estimated job {} execution in response to scheduled allocation.", job.getJobId());

		final Pair<Instant, Double> execution = agentProps.getEstimatedEarliestJobStartTimeAndDuration(job);
		final Double price = convertToHourDuration(execution.getValue().longValue()) * agentProps.getPricePerHour();

		final JobWithExecutionEstimation jobsEstimation = ImmutableJobWithExecutionEstimation.builder()
				.estimatedDuration(execution.getValue().longValue())
				.estimatedPrice(price)
				.earliestStartTime(execution.getKey())
				.build();

		agent.send(prepareReply(facts.get(MESSAGE), jobsEstimation, facts.get(MESSAGE_TYPE)));
	}

	@Override
	public AgentRule copy() {
		return new PrepareServerJobEstimationDataRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
