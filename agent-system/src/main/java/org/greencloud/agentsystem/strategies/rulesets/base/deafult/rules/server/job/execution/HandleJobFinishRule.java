package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.execution;

import static java.lang.String.valueOf;
import static java.util.Optional.ofNullable;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED_JOB_STATUSES;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_FINISH_JOB_EXECUTION_RULE;
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

public class HandleJobFinishRule extends AgentScheduledRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(HandleJobFinishRule.class);

	public HandleJobFinishRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(FINISH_JOB_EXECUTION_RULE,
				"start of job execution in Server",
				"rule initiates start of Job execution in given Server");
	}

	@Override
	protected boolean evaluateBeforeTrigger(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		return agentProps.getServerJobs().containsKey(job)
				&& ACCEPTED_JOB_STATUSES.contains(agentProps.getServerJobs().get(job));
	}

	@Override
	protected Date specifyTime(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		final Instant endDate = ofNullable(job.getExpectedEndTime())
				.filter(end -> !getCurrentTime().isAfter(end))
				.orElse(getCurrentTime());
		return Date.from(endDate);
	}

	@Override
	protected void handleActionTrigger(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		final String jobId = job.getJobId();

		MDC.put(MDC_JOB_ID, jobId);
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Finished executing the job {} at {}", job.getJobId(), getCurrentTime());

		facts.put(RULE_TYPE, PROCESS_FINISH_JOB_EXECUTION_RULE);
		controller.fire(facts);
	}

	@Override
	public AgentRule copy() {
		return new HandleJobFinishRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
