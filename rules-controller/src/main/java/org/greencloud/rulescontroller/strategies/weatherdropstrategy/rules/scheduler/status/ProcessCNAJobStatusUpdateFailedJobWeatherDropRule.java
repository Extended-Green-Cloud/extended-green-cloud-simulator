package org.greencloud.rulescontroller.strategies.weatherdropstrategy.rules.scheduler.status;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLE_FAILED_JOB_RULE;
import static org.greencloud.commons.enums.rules.RuleType.START_JOB_EXECUTION_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.FAILED_JOB_ID;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static org.greencloud.commons.utils.job.JobUtils.isJobStarted;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.schedule.ScheduleOnce;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.scheduler.SchedulerNode;

public class ProcessCNAJobStatusUpdateFailedJobWeatherDropRule
		extends AgentBasicRule<SchedulerAgentProps, SchedulerNode> {

	private static final Logger logger = getLogger(ProcessCNAJobStatusUpdateFailedJobWeatherDropRule.class);

	public ProcessCNAJobStatusUpdateFailedJobWeatherDropRule(
			final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
		super(controller, 3);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE, JOB_STATUS_RECEIVER_HANDLE_FAILED_JOB_RULE,
				"handles job update - failed job",
				"rule runs when execution of new client job has failed");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		final String type = facts.get(MESSAGE_TYPE);
		return type.equals(FAILED_JOB_ID);
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final Optional<ClientJob> jobOptional = facts.get(JOB);

		if (jobOptional.isPresent()) {
			final ClientJob job = jobOptional.get();
			final JobWithStatus jobStatusUpdate = facts.get(MESSAGE_CONTENT);

			MDC.put(MDC_JOB_ID, jobStatusUpdate.getJobInstance().getJobId());
			MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));

			agentProps.getCnaForJobMap().remove(job.getJobId());
			if (isJobStarted(agentProps.getClientJobs().get(job))) {
				logger.info("Execution of the job {} has failed after its start. Passing information to Client.",
						job.getJobId());

				final int strategyIdx = agentProps.removeJob(job);
				controller.removeStrategy(agentProps.getStrategyForJob(), strategyIdx);
				agent.send(
						prepareJobStatusMessageForClient(job, jobStatusUpdate, FAILED_JOB_ID, facts.get(STRATEGY_IDX)));
			} else {
				logger.info("Execution of the job {} has failed before its start. Moving execution to central cloud.",
						job.getJobId());
				agentNode.announceClientJob();

				final StrategyFacts executionFacts = new StrategyFacts(facts.get(STRATEGY_IDX));
				executionFacts.put(JOB, job);

				agent.addBehaviour(ScheduleOnce.create(agent, executionFacts, START_JOB_EXECUTION_RULE, controller,
						f -> f.get(STRATEGY_IDX)));
			}
		}
	}
}
