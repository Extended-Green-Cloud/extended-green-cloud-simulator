package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.regionalmanager.job.execution;

import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB_ID;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB_TIME;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.IN_PROGRESS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.HANDLE_DELAYED_JOB_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.HANDLE_JOB_STATUS_CHECK_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getJobById;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.TRIGGER_TIME;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Date;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.initiate.InitiateRequest;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentScheduledRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ScheduleJobStartVerificationRule extends AgentScheduledRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(ScheduleJobStartVerificationRule.class);

	public ScheduleJobStartVerificationRule(final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(HANDLE_DELAYED_JOB_RULE,
				"schedules job execution verification in Server",
				"when there is no information about job start, ask Server manually if Job has started");
	}

	@Override
	protected Date specifyTime(final RuleSetFacts facts) {
		final ClientJob initial = facts.get(JOB);
		return Date.from(initial.getDeadline());
	}

	@Override
	protected boolean evaluateBeforeTrigger(final RuleSetFacts facts) {
		final ClientJob initial = facts.get(JOB);
		final String jobId = initial.getJobId();

		final ClientJob job = getJobById(jobId, agentProps.getNetworkJobs());

		return nonNull(job)
				&& agentProps.getServerForJobMap().containsKey(jobId)
				&& !agentProps.getNetworkJobs().get(job).equals(IN_PROGRESS);
	}

	@Override
	protected void handleActionTrigger(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		final String jobId = job.getJobId();
		MDC.put(MDC_JOB_ID, jobId);
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.error("There is no message regarding the job start. Sending request to the server.");

		facts.put(JOB_TIME, facts.get(TRIGGER_TIME));
		facts.put(JOB_ID, jobId);
		agent.addBehaviour(InitiateRequest.create(agent, facts, HANDLE_JOB_STATUS_CHECK_RULE, controller));
	}

	@Override
	public AgentRule copy() {
		return new ScheduleJobStartVerificationRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
