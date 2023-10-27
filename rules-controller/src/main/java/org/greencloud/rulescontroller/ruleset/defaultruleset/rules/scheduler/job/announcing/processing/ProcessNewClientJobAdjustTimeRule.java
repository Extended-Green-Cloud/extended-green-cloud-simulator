package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.announcing.processing;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_ANNOUNCEMENT_HANDLE_ADJUST_TIME_FRAMES_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_ANNOUNCEMENT_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToJobWithNewTime;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobAdjustmentMessage;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;

import org.greencloud.rulescontroller.ruleset.RuleSet;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.slf4j.Logger;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import com.gui.agents.scheduler.SchedulerNode;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.simple.AgentChainRule;
import org.slf4j.MDC;

public class ProcessNewClientJobAdjustTimeRule extends AgentChainRule<SchedulerAgentProps, SchedulerNode> {

	private static final Logger logger = getLogger(ProcessNewClientJobAdjustTimeRule.class);

	private Instant newAdjustedStart;
	private Instant newAdjustedEnd;

	public ProcessNewClientJobAdjustTimeRule(final RulesController<SchedulerAgentProps, SchedulerNode> controller,
			final RuleSet ruleSet) {
		super(controller, 2, ruleSet);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_ANNOUNCEMENT_RULE, NEW_JOB_ANNOUNCEMENT_HANDLE_ADJUST_TIME_FRAMES_RULE,
				"adjust job time frames",
				"when job can be executed before deadline, but is delayed, Scheduler adjusts its time frames");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		newAdjustedEnd = facts.get("job-adjusted-end");
		newAdjustedStart = facts.get("job-adjusted-start");

		return job.getStartTime().isBefore(newAdjustedStart);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		final ClientJob adjustedJob = mapToJobWithNewTime(job, newAdjustedStart, newAdjustedEnd);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Job {} time frames are outdated. Adjusting job time frames. New job start: {}, new job edn: {}",
				job.getJobId(), newAdjustedStart, newAdjustedEnd);

		agentProps.swapJobInstances(adjustedJob, job);
		agent.send(prepareJobAdjustmentMessage(adjustedJob, facts.get(RULE_SET_IDX)));

		facts.put(JOB, adjustedJob);
	}
}
