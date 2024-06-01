package org.greencloud.agentsystem.strategies.intentstandardonestep.server;

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
import org.slf4j.Logger;
import org.slf4j.MDC;

public class PrepareServerAllocationDataRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(PrepareServerAllocationDataRule.class);

	public PrepareServerAllocationDataRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PREPARE_DATA_FOR_JOB_ALLOCATION_RULE,
				"prepares data for job allocation",
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
		logger.info("Estimated jobs execution in response to scheduled allocation.");

		final Map<String, JobWithExecutionEstimation> jobsEstimation = jobs.getAllocationJobs().stream()
				.collect(toMap(PowerJob::getJobId,this::getJobEstimation));
		final ServerJobsEstimation serverEstimation = ImmutableServerJobsEstimation.builder()
				.jobsEstimation(jobsEstimation)
				.serverReliability(agentNode.getComponentSuccessRatio(agent.getAID().getName()))
				.build();

		agent.send(prepareReply(facts.get(MESSAGE), serverEstimation, facts.get(MESSAGE_TYPE)));
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
		return new PrepareServerAllocationDataRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
