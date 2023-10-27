package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.announcing.processing;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_ANNOUNCEMENT_HANDLE_ADJUST_DEADLINE_REACH_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_ANNOUNCEMENT_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.FAILED_JOB_ID;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static java.lang.String.valueOf;
import static org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.announcing.domain.AnnouncingConstants.DEADLINE_TIME_ADJUSTMENT;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import com.gui.agents.scheduler.SchedulerNode;

public class ProcessNewClientJobAfterDeadlineRule extends AgentBasicRule<SchedulerAgentProps, SchedulerNode> {

	private static final Logger logger = getLogger(ProcessNewClientJobAfterDeadlineRule.class);

	public ProcessNewClientJobAfterDeadlineRule(final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
		super(controller, 3);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_ANNOUNCEMENT_RULE, NEW_JOB_ANNOUNCEMENT_HANDLE_ADJUST_DEADLINE_REACH_RULE,
				"sending failure when job is after deadline",
				"when job that is to be announced is after deadline, Scheduler sends FAILURE message");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		final Instant postponedEnd = facts.get("job-adjusted-end");

		return postponedEnd.isAfter(job.getDeadline().minusMillis(DEADLINE_TIME_ADJUSTMENT));
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Sending FAIL information to Client. Job {} would be executed after deadline", job.getJobId());

		final int ruleSetIdx = agentProps.removeJob(job);
		controller.removeRuleSet(agentProps.getRuleSetForJob(), ruleSetIdx);
		agent.send(prepareJobStatusMessageForClient(job, FAILED_JOB_ID, facts.get(RULE_SET_IDX)));
	}
}
