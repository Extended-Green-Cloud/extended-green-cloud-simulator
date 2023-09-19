package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.scheduler.job.announcing.processing;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.LOOK_FOR_JOB_EXECUTOR_HANDLE_FAILURE_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.FAILED_JOB_ID;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.preparePostponeJobMessageForClient;
import static java.lang.String.valueOf;
import static org.greencloud.rulescontroller.strategy.defaultstrategy.rules.scheduler.job.announcing.domain.AnnouncingConstants.JOB_RETRY_MINUTES_ADJUSTMENT;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.scheduler.SchedulerNode;

public class ProcessLookForCNAForJobExecutionFailureRule extends AgentBasicRule<SchedulerAgentProps, SchedulerNode> {

	private static final Logger logger = getLogger(ProcessLookForCNAForJobExecutionFailureRule.class);

	public ProcessLookForCNAForJobExecutionFailureRule(final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
		super(controller, 3);
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

		if (agentProps.postponeJobExecution(job, JOB_RETRY_MINUTES_ADJUSTMENT)) {
			logger.info("All Cloud Network Agents refused to the call for proposal. "
					+ "Job postponed and scheduled for next execution.");
			agent.send(preparePostponeJobMessageForClient(job, facts.get(STRATEGY_IDX)));
		} else {
			logger.info("All Cloud Network Agents refused to the call for proposal. Sending failure information.");

			final int strategyIdx = agentProps.removeJob(job);
			controller.removeStrategy(agentProps.getStrategyForJob(), strategyIdx);
			agentProps.getCnaForJobMap().remove(job.getJobId());
			agent.send(prepareJobStatusMessageForClient(job, FAILED_JOB_ID, facts.get(STRATEGY_IDX)));
		}
	}
}
