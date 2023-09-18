package org.greencloud.rulescontroller.strategies.weatherdropstrategy.rules.scheduler.status;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.PROCESSING;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.LOOK_FOR_JOB_EXECUTOR_HANDLE_FAILURE_RULE;
import static org.greencloud.commons.enums.rules.RuleType.START_JOB_EXECUTION_RULE;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.preparePostponeJobMessageForClient;
import static java.lang.String.valueOf;
import static org.greencloud.rulescontroller.strategies.defaultstrategy.rules.scheduler.job.announcing.domain.AnnouncingConstants.JOB_RETRY_MINUTES_ADJUSTMENT;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.schedule.ScheduleOnce;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.scheduler.SchedulerNode;

public class ProcessLookForCNAForJobExecutionFailureWeatherDropRule
		extends AgentBasicRule<SchedulerAgentProps, SchedulerNode> {

	private static final Logger logger = getLogger(ProcessLookForCNAForJobExecutionFailureWeatherDropRule.class);
	private static final Integer MAX_JOB_POSTPONES = 3;
	private final ConcurrentMap<String, AtomicInteger> jobPostponeCounter;

	public ProcessLookForCNAForJobExecutionFailureWeatherDropRule(
			final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
		super(controller, 3);
		this.jobPostponeCounter = new ConcurrentHashMap<>();
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LOOK_FOR_JOB_EXECUTOR_HANDLE_FAILURE_RULE,
				"handle cases when there is no CNA for job execution",
				"rule provides common handler for cases when there are no candidates to execute the job");
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));

		jobPostponeCounter.putIfAbsent(job.getJobInstanceId(), new AtomicInteger(0));

		if (jobPostponeCounter.get(job.getJobInstanceId()).get() < MAX_JOB_POSTPONES
				&& agentProps.postponeJobExecution(job, JOB_RETRY_MINUTES_ADJUSTMENT)) {
			logger.info("All Cloud Network Agents refused to the call for proposal. "
					+ "Job postponed and scheduled for next execution.");
			jobPostponeCounter.get(job.getJobInstanceId()).incrementAndGet();
			agent.send(preparePostponeJobMessageForClient(job, facts.get(STRATEGY_IDX)));
		} else {
			logger.info(
					"All Cloud Network Agents refused to the call for proposal. Scheduling job execution in cloud.");
			agentProps.getClientJobs().replace(job, PROCESSING, ACCEPTED);
			agentNode.announceClientJob();

			final StrategyFacts executionFacts = new StrategyFacts(facts.get(STRATEGY_IDX));
			executionFacts.put(JOB, job);
			agent.addBehaviour(ScheduleOnce.create(agent, executionFacts, START_JOB_EXECUTION_RULE, controller,
					f -> f.get(STRATEGY_IDX)));
		}
	}
}
