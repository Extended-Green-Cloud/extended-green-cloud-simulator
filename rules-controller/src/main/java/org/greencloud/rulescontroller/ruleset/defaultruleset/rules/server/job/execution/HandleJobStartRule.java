package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.execution;

import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.PLANNED_JOB_STATUSES;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_START_JOB_EXECUTION_RULE;
import static org.greencloud.commons.enums.rules.RuleType.START_JOB_EXECUTION_RULE;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.Date;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.mapper.JobMapper;
import org.greencloud.gui.agents.server.ServerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentScheduledRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class HandleJobStartRule extends AgentScheduledRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(HandleJobStartRule.class);

	public HandleJobStartRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(START_JOB_EXECUTION_RULE,
				"start of job execution in Server",
				"rule initiates start of Job execution in given Server");
	}

	@Override
	protected Date specifyTime(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		final Instant startDate = getCurrentTime().isAfter(job.getStartTime())
				? getCurrentTime()
				: job.getStartTime();
		return Date.from(startDate);
	}

	@Override
	protected void handleActionTrigger(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		final String jobId = job.getJobId();

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		if (!agentProps.getServerJobs().containsKey(job)) {
			logger.info("Job execution couldn't start: job {} is not present",
					JobMapper.mapClientJobToJobInstanceId(job));
			return;
		}

		if (PLANNED_JOB_STATUSES.contains(agentProps.getServerJobs().getOrDefault(job, ACCEPTED))) {
			facts.put(RULE_TYPE, PROCESS_START_JOB_EXECUTION_RULE);
			controller.fire(facts);
		} else {
			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
			logger.info("The execution of specific job {} instance has already started", jobId);
		}
	}
}
