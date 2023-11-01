package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.announcing.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.CREATED;
import static org.greencloud.commons.enums.rules.RuleType.LOOK_FOR_JOB_EXECUTOR_HANDLE_FAILURE_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.FAILED_JOB_ID;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.preparePostponeJobMessageForClient;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.announcing.domain.AnnouncingConstants.JOB_RETRY_MINUTES_ADJUSTMENT;
import static org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.announcing.domain.AnnouncingConstants.PROCESSING_TIME_ADJUSTMENT;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.scheduler.SchedulerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ProcessLookForCNAForJobExecutionFailureRule extends AgentBasicRule<SchedulerAgentProps, SchedulerNode> {

	public static final Integer MAX_RETRIES = 10;
	private static final Logger logger = getLogger(ProcessLookForCNAForJobExecutionFailureRule.class);
	private final ConcurrentHashMap<String, AtomicInteger> retryCounter;

	public ProcessLookForCNAForJobExecutionFailureRule(
			final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
		super(controller, 3);
		this.retryCounter = new ConcurrentHashMap<>();
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LOOK_FOR_JOB_EXECUTOR_HANDLE_FAILURE_RULE,
				"handle cases when there is no CNA for job execution",
				"rule provides common handler for cases when there are no candidates to execute the job");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		retryCounter.putIfAbsent(job.getJobId(), new AtomicInteger(0));

		if (!job.getStartTime().isBefore(getCurrentTime().plusMillis(PROCESSING_TIME_ADJUSTMENT))
				&& retryCounter.get(job.getJobId()).get() < MAX_RETRIES) {
			logger.info("All Cloud Network Agents refused to the call for proposal. Putting job back to the queue");

			if (agentProps.getJobsToBeExecuted().offer(job)) {
				retryCounter.get(job.getJobId()).incrementAndGet();
				agentProps.getClientJobs().replace(job, CREATED);
			}
		} else if (agentProps.postponeJobExecution(job, JOB_RETRY_MINUTES_ADJUSTMENT)) {
			logger.info("All Cloud Network Agents refused to the call for proposal. "
					+ "Job postponed and scheduled for next execution.");
			agent.send(preparePostponeJobMessageForClient(job, facts.get(RULE_SET_IDX)));
			retryCounter.get(job.getJobId()).set(0);
		} else {
			logger.info("All Cloud Network Agents refused to the call for proposal. Sending failure information.");

			final int ruleSetIdx = agentProps.removeJob(job);
			controller.removeRuleSet(agentProps.getRuleSetForJob(), ruleSetIdx);
			agentProps.getCnaForJobMap().remove(job.getJobId());
			agent.send(prepareJobStatusMessageForClient(job, FAILED_JOB_ID, facts.get(RULE_SET_IDX)));
			retryCounter.remove(job.getJobId());
		}
	}
}
