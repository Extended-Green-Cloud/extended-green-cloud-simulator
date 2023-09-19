package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.cloudnetwork.job.execution;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.IN_PROGRESS;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_ID;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_TIME;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.constants.FactTypeConstants.TRIGGER_TIME;
import static org.greencloud.commons.enums.rules.RuleType.HANDLE_DELAYED_JOB_RULE;
import static org.greencloud.commons.enums.rules.RuleType.HANDLE_JOB_STATUS_CHECK_RULE;
import static org.greencloud.commons.utils.time.TimeScheduler.alignStartTimeToCurrentTime;
import static org.greencloud.commons.utils.job.JobUtils.getJobById;
import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.Date;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateRequest;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentScheduledRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.cloudnetwork.CloudNetworkNode;

public class ScheduleJobStartVerificationRule extends AgentScheduledRule<CloudNetworkAgentProps, CloudNetworkNode> {

	private static final Logger logger = getLogger(ScheduleJobStartVerificationRule.class);
	private static final Long MAX_ERROR_IN_JOB_START = 1L;

	public ScheduleJobStartVerificationRule(
			final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller) {
		super(controller);
	}

	/**
	 * Method initialize default rule metadata
	 *
	 * @return rule description
	 */
	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(HANDLE_DELAYED_JOB_RULE,
				"schedules job execution verification in Server",
				"when there is no information about job start, ask Server manually if Job has started");
	}

	@Override
	protected Date specifyTime(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		final Instant startTime = alignStartTimeToCurrentTime(job.getStartTime());
		return Date.from(startTime.plusSeconds(MAX_ERROR_IN_JOB_START));
	}

	@Override
	protected boolean evaluateBeforeTrigger(final StrategyFacts facts) {
		final ClientJob initial = facts.get(JOB);
		final String jobId = initial.getJobId();

		final ClientJob job = getJobById(jobId, agentProps.getNetworkJobs());

		return nonNull(job)
				&& agentProps.getServerForJobMap().containsKey(jobId)
				&& !agentProps.getNetworkJobs().get(job).equals(IN_PROGRESS);
	}

	@Override
	protected void handleActionTrigger(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		final String jobId = job.getJobId();
		MDC.put(MDC_JOB_ID, jobId);
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.error("There is no message regarding the job start. Sending request to the server");

		facts.put(JOB_TIME, facts.get(TRIGGER_TIME));
		facts.put(JOB_ID, jobId);
		agent.addBehaviour(InitiateRequest.create(agent, facts, HANDLE_JOB_STATUS_CHECK_RULE, controller));
	}
}
