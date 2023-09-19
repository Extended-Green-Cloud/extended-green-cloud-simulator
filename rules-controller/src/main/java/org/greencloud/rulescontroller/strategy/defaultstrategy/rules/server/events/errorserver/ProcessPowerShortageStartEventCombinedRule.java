package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.server.events.errorserver;

import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACTIVE_JOB_STATUSES;
import static org.greencloud.commons.constants.FactTypeConstants.EVENT;
import static org.greencloud.commons.constants.FactTypeConstants.EVENT_TIME;
import static org.greencloud.commons.constants.FactTypeConstants.JOBS;
import static org.greencloud.commons.enums.rules.RuleType.POWER_SHORTAGE_ERROR_RULE;
import static org.greencloud.commons.enums.rules.RuleType.POWER_SHORTAGE_ERROR_START_RULE;
import static org.greencloud.rulescontroller.rule.combined.domain.AgentCombinedRuleType.EXECUTE_FIRST;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.List;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.strategy.defaultstrategy.rules.server.events.errorserver.processing.ProcessPowerShortageStartNoAffectedJobsRule;
import org.greencloud.rulescontroller.strategy.defaultstrategy.rules.server.events.errorserver.processing.ProcessPowerShortageStartWithAffectedJobsRule;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.combined.AgentCombinedRule;
import org.slf4j.Logger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.server.ServerNode;
import com.gui.event.PowerShortageEvent;

public class ProcessPowerShortageStartEventCombinedRule extends AgentCombinedRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessPowerShortageStartEventCombinedRule.class);

	public ProcessPowerShortageStartEventCombinedRule(final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController, EXECUTE_FIRST, 1);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(POWER_SHORTAGE_ERROR_RULE, POWER_SHORTAGE_ERROR_START_RULE,
				"handle power shortage start event",
				"rule handles start of power shortage event");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		final PowerShortageEvent powerShortageEvent = facts.get(EVENT);

		if (!powerShortageEvent.isFinished()) {
			final Instant startTime = powerShortageEvent.getOccurrenceTime();

			logger.info("Internal server error was detected for server! Power will be cut off at: {}", startTime);

			facts.put(EVENT_TIME, startTime);
			facts.put(JOBS, getAffectedPowerJobs(startTime));

			return true;
		}
		return false;
	}

	/**
	 * Method construct set of rules that are to be combined
	 */
	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessPowerShortageStartNoAffectedJobsRule(controller),
				new ProcessPowerShortageStartWithAffectedJobsRule(controller)
		);
	}

	private List<ClientJob> getAffectedPowerJobs(final Instant startTime) {
		return agentProps.getServerJobs().keySet().stream()
				.filter(job -> startTime.isBefore(job.getEndTime()))
				.filter(job -> ACTIVE_JOB_STATUSES.contains(agentProps.getServerJobs().get(job)))
				.toList();
	}

}
