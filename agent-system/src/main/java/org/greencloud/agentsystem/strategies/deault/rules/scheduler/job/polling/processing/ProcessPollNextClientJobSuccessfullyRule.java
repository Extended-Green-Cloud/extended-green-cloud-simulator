package org.greencloud.agentsystem.strategies.deault.rules.scheduler.job.polling.processing;

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_ANNOUNCEMENT_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_POLLING_HANDLE_JOB_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_POLLING_RULE;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;

import java.time.Instant;

import org.greencloud.agentsystem.strategies.deault.rules.scheduler.job.announcing.domain.AnnouncingConstants;
import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.scheduler.SchedulerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.AgentBasicRule;

public class ProcessPollNextClientJobSuccessfullyRule extends AgentBasicRule<SchedulerAgentProps, SchedulerNode> {

	public ProcessPollNextClientJobSuccessfullyRule(
			final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
		super(controller, 1);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_POLLING_RULE, NEW_JOB_POLLING_HANDLE_JOB_RULE,
				"poll next job to be announced",
				"when there are jobs in the queue, Scheduler polls next job");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		return !agentProps.getJobsToBeExecuted().isEmpty();
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob jobToExecute = agentProps.getJobsToBeExecuted().poll();
		agentNode.updateScheduledJobQueue(agentProps);

		if(nonNull(jobToExecute)) {
			facts.put(RULE_SET_IDX, agentProps.getRuleSetForJob().get(jobToExecute.getJobId()));
			final RuleSetFacts announcementFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
			announcementFacts.put(JOB, jobToExecute);
			announcementFacts.put(RULE_TYPE, NEW_JOB_ANNOUNCEMENT_RULE);
			putAdjustedTimeFrame(requireNonNull(jobToExecute), announcementFacts);

			controller.fire(announcementFacts);
		}
	}

	private void putAdjustedTimeFrame(final ClientJob job, final RuleSetFacts facts) {
		final long jobDuration = MILLIS.between(job.getStartTime(), job.getEndTime());
		final Instant newAdjustedStart = getCurrentTime().plusMillis(AnnouncingConstants.PROCESSING_TIME_ADJUSTMENT);
		final Instant newAdjustedEnd = newAdjustedStart.plusMillis(jobDuration);

		facts.put("job-adjusted-start", newAdjustedStart);
		facts.put("job-adjusted-end", newAdjustedEnd);
	}
}
