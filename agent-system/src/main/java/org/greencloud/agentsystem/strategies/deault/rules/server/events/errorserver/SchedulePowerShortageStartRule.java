package org.greencloud.agentsystem.strategies.deault.rules.server.events.errorserver;

import static java.lang.String.valueOf;
import static org.jrba.rulesengine.constants.FactTypeConstants.EVENT_TIME;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.SET_EVENT_ERROR;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.HANDLE_POWER_SHORTAGE_RULE;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentScheduledRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class SchedulePowerShortageStartRule extends AgentScheduledRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(SchedulePowerShortageStartRule.class);

	public SchedulePowerShortageStartRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(HANDLE_POWER_SHORTAGE_RULE,
				"handle power shortage start",
				"rule performs actions upon power shortage start");
	}

	@Override
	protected Date specifyTime(final RuleSetFacts facts) {
		final Instant shortageStart = facts.get(EVENT_TIME);
		final Instant startTime = getCurrentTime().isAfter(shortageStart) ? getCurrentTime() : shortageStart;
		return Date.from(startTime);
	}

	@Override
	protected void handleActionTrigger(final RuleSetFacts facts) {
		final List<ClientJob> affectedJobs = facts.get(JOBS);
		affectedJobs.forEach(job -> {
			if (agentProps.getServerJobs().containsKey(job)) {
				final JobExecutionStatusEnum jobStatus = agentProps.getServerJobs().get(job);
				final String jobId = job.getJobId();

				MDC.put(MDC_JOB_ID, jobId);
				MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
				switch (jobStatus) {
					case ON_HOLD_TRANSFER, ON_HOLD_TRANSFER_PLANNED ->
							logger.info("Job with id {} put temporarily on hold.", jobId);
					case IN_PROGRESS_BACKUP_ENERGY_PLANNED, IN_PROGRESS_BACKUP_ENERGY ->
							logger.info("Supplying job with id {} using backup power", jobId);
					default -> logger.info("Putting job with id {} on hold", jobId);
				}
				agentProps.updateGUI();
			}
		});
		agentProps.setHasError(facts.get(SET_EVENT_ERROR));
	}
}
