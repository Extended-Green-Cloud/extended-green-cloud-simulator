package org.greencloud.rulescontroller.strategies.weatherdropstrategy.rules.scheduler.execution;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED_JOB_STATUSES;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.FINISH_JOB_ID;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.Date;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentScheduledRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.scheduler.SchedulerNode;

public class HandleFinishJobExecutionInCloudRule extends AgentScheduledRule<SchedulerAgentProps, SchedulerNode> {

	private static final Logger logger = getLogger(HandleFinishJobExecutionInCloudRule.class);

	public HandleFinishJobExecutionInCloudRule(final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(FINISH_JOB_EXECUTION_RULE,
				"finish of job execution in central cloud",
				"rule initiates finish of Job execution in the central cloud");
	}

	@Override
	protected boolean evaluateBeforeTrigger(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		return agentProps.getClientJobs().containsKey(job)
				&& ACCEPTED_JOB_STATUSES.contains(agentProps.getClientJobs().get(job));
	}

	@Override
	protected Date specifyTime(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		final Instant endDate = getCurrentTime().isAfter(job.getEndTime()) ? getCurrentTime() : job.getEndTime();
		return Date.from(endDate);
	}

	@Override
	protected void handleActionTrigger(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("Client job {} execution has finished in cloud.", job.getJobId());
		agentNode.addFinishedInCloudJob();

		final int strategyIdx = agentProps.removeJob(job);
		controller.removeStrategy(agentProps.getStrategyForJob(), strategyIdx);
		agent.send(prepareJobStatusMessageForClient(job, FINISH_JOB_ID, facts.get(STRATEGY_IDX)));
	}
}
