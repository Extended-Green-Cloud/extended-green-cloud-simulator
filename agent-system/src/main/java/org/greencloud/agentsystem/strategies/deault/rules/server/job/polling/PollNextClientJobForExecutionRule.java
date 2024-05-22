package org.greencloud.agentsystem.strategies.deault.rules.server.job.polling;

import static java.lang.String.valueOf;
import static java.util.Objects.requireNonNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED_BY_SERVER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_POLLING_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.POLL_NEXT_JOB_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToJobInstanceId;
import static org.greencloud.commons.utils.job.JobUtils.updateJobStartAndExecutionTime;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.FAILED_JOB_ID;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForRMA;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESOURCES;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentPeriodicRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class PollNextClientJobForExecutionRule extends AgentPeriodicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(PollNextClientJobForExecutionRule.class);
	private static final int POLL_NEXT_JOB_TIMEOUT = 1000;
	private static final int JOB_START_ERROR = 1000;

	public PollNextClientJobForExecutionRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(POLL_NEXT_JOB_RULE,
				"trigger next job for execution polling",
				"rule executed periodically which triggers polling next job for execution");
	}

	@Override
	protected long specifyPeriod() {
		return POLL_NEXT_JOB_TIMEOUT;
	}

	@Override
	protected boolean evaluateBeforeTrigger(final RuleSetFacts facts) {
		return !agentProps.getJobsForExecutionQueue().isEmpty();
	}

	@Override
	protected void handleActionTrigger(final RuleSetFacts facts) {
		final ClientJob nextJob = requireNonNull(agentProps.getJobsForExecutionQueue().poll());

		if (!nextJob.getDeadline().isAfter(getCurrentTime())) {
			handleJobFailure(nextJob, facts);
		}
		agentProps.getServerJobs().replace(nextJob, ACCEPTED_BY_SERVER);
		final Pair<ClientJob, Instant> jobAndStartTime = getJobWithExecutionAndStartTime(nextJob, facts);
		final ClientJob job = jobAndStartTime.getKey();

		final Map<String, Resource> availableResources = agentProps.getAvailableResources(jobAndStartTime.getValue(),
				job.getExpectedEndTime(), mapToJobInstanceId(job), null);

		facts.put(RULE_TYPE, NEW_JOB_POLLING_RULE);
		facts.put(RULE_SET_IDX, agentProps.getRuleSetForJob().get(job));
		facts.put(RESOURCES, availableResources);
		facts.put(JOB, job);
		controller.fire(facts);
	}

	private Pair<ClientJob, Instant> getJobWithExecutionAndStartTime(final ClientJob nextJob, final RuleSetFacts facts) {
		final Instant startTime = getCurrentTime().plusMillis(JOB_START_ERROR);
		final long duration = (long) agentProps.getJobExecutionDuration(nextJob, startTime);
		final ClientJob job = updateJobStartAndExecutionTime(nextJob, startTime, duration, agentProps.getServerJobs());

		MDC.put(MDC_JOB_ID, nextJob.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Updating job execution time in server to {}. Expected end time is {}.",
				duration, job.getExpectedEndTime());

		return Pair.of(job, startTime);
	}

	private void handleJobFailure(final ClientJob nextJob, final RuleSetFacts facts) {
		MDC.put(MDC_JOB_ID, nextJob.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Job would be executed after deadline. Sending failure to RMA.");

		agentProps.removeJob(nextJob);
		agent.send(prepareJobStatusMessageForRMA(nextJob, FAILED_JOB_ID, agentProps, facts.get(RULE_SET_IDX)));
	}

	@Override
	public AgentRule copy() {
		return new PollNextClientJobForExecutionRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
