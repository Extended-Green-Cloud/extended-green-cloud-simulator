package org.greencloud.rulescontroller.strategies.weatherdropstrategy.rules.scheduler.execution;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.IN_PROGRESS_CLOUD;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.PLANNED_JOB_STATUSES;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.enums.rules.RuleType.START_JOB_EXECUTION_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.STARTED_IN_CLOUD_ID;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.Date;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.schedule.ScheduleOnce;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentScheduledRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.scheduler.SchedulerNode;

public class HandleStartJobExecutionInCloudRule extends AgentScheduledRule<SchedulerAgentProps, SchedulerNode> {

	private static final Logger logger = getLogger(HandleStartJobExecutionInCloudRule.class);

	public HandleStartJobExecutionInCloudRule(final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(START_JOB_EXECUTION_RULE,
				"start of job execution in central cloud",
				"rule initiates start of Job execution in the central cloud");
	}

	@Override
	protected Date specifyTime(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		final Instant startDate = getCurrentTime().isAfter(job.getStartTime())
				? getCurrentTime()
				: job.getStartTime();
		return Date.from(startDate);
	}

	@Override
	protected void handleActionTrigger(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));

		if (PLANNED_JOB_STATUSES.contains(agentProps.getClientJobs().getOrDefault(job, ACCEPTED))) {
			logger.info("Start execution of the job {} in cloud.", job.getJobId());
			agentNode.addStartedInCloudJob();

			final StrategyFacts executionFacts = new StrategyFacts(facts.get(STRATEGY_IDX));
			executionFacts.put(JOB, job);
			agent.addBehaviour(ScheduleOnce.create(agent, executionFacts, FINISH_JOB_EXECUTION_RULE, controller,
					f -> f.get(STRATEGY_IDX)));

			agentProps.getClientJobs().replace(job, ACCEPTED, IN_PROGRESS_CLOUD);
			agent.send(prepareJobStatusMessageForClient(job, STARTED_IN_CLOUD_ID, facts.get(STRATEGY_IDX)));
		} else {
			logger.info("The execution of specific job {} instance has already started", job.getJobId());
		}
	}
}
