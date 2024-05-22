package org.greencloud.agentsystem.strategies.deault.rules.greenenergy.events.sourcepowershortage;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.GREEN_ENERGY;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.HANDLE_POWER_SHORTAGE_RULE;
import static org.greencloud.commons.utils.time.TimeScheduler.alignStartTimeToCurrentTime;
import static org.jrba.rulesengine.constants.FactTypeConstants.EVENT_TIME;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.SET_EVENT_ERROR;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentScheduledRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ScheduleGreenSourcePowerShortageStartRule
		extends AgentScheduledRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ScheduleGreenSourcePowerShortageStartRule.class);

	public ScheduleGreenSourcePowerShortageStartRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(HANDLE_POWER_SHORTAGE_RULE,
				"handle Green Source power shortage start",
				"rule performs actions upon Green Source power shortage start");
	}

	@Override
	protected Date specifyTime(final RuleSetFacts facts) {
		final Instant shortageStart = facts.get(EVENT_TIME);
		final Instant startTime = alignStartTimeToCurrentTime(shortageStart);
		return Date.from(startTime);
	}

	@Override
	protected void handleActionTrigger(final RuleSetFacts facts) {
		final boolean setError = facts.get(SET_EVENT_ERROR);
		final List<ServerJob> affectedJobs = facts.get(JOBS);

		affectedJobs.forEach(jobToHalt -> {
			if (agentProps.getServerJobs().containsKey(jobToHalt)) {
				MDC.put(MDC_JOB_ID, jobToHalt.getJobId());
				MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
				logger.info("Power shortage has started. Putting job {} on hold", jobToHalt.getJobId());
			}
		});
		agentProps.setHasError(setError);
	}

	@Override
	public AgentRule copy() {
		return new ScheduleGreenSourcePowerShortageStartRule(controller);
	}

	@Override
	public String getAgentType() {
		return GREEN_ENERGY.getName();
	}
}
