package org.greencloud.agentsystem.strategies.deault.rules.server.job.execution;

import static java.lang.String.valueOf;
import static java.util.Optional.ofNullable;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.PLANNED_JOB_STATUSES;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_START_JOB_EXECUTION_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.START_JOB_EXECUTION_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapClientJobToJobInstanceId;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.Date;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentScheduledRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
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
		final Instant startDate = ofNullable(job.getStartTime())
				.filter(start -> !getCurrentTime().isAfter(start))
				.orElse(getCurrentTime());
		return Date.from(startDate);
	}

	@Override
	protected boolean evaluateBeforeTrigger(RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		final boolean isJobPresent = agentProps.getServerJobs().containsKey(job);
		final boolean isPlanned = PLANNED_JOB_STATUSES.contains(agentProps.getServerJobs().getOrDefault(job, ACCEPTED));

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));

		if (!isJobPresent) {
			logger.info("Job execution couldn't start: job {} is not present", mapClientJobToJobInstanceId(job));
		} else if (!isPlanned) {
			logger.info("The execution of specific job {} instance has already started", job.getJobId());
		}

		return isJobPresent && isPlanned;
	}

	@Override
	protected void handleActionTrigger(final RuleSetFacts facts) {
		facts.put(RULE_TYPE, PROCESS_START_JOB_EXECUTION_RULE);
		controller.fire(facts);
	}

	@Override
	public AgentRule copy() {
		return new HandleJobStartRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
