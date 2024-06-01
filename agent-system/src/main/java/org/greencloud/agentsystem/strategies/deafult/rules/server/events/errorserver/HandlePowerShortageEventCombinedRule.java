package org.greencloud.agentsystem.strategies.deafult.rules.server.events.errorserver;

import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.POWER_SHORTAGE_ERROR_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.EVENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.EVENT_TIME;
import static org.jrba.rulesengine.types.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.List;

import org.greencloud.agentsystem.strategies.deafult.rules.server.events.errorserver.processing.ProcessPowerShortageFinishEventRule;
import org.greencloud.agentsystem.strategies.deafult.rules.server.events.errorserver.processing.ProcessPowerShortageStartNoAffectedJobsRule;
import org.greencloud.agentsystem.strategies.deafult.rules.server.events.errorserver.processing.ProcessPowerShortageStartWithAffectedJobsRule;
import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.basic.PowerJob;
import org.greencloud.gui.agents.server.ServerNode;
import org.greencloud.gui.event.PowerShortageEvent;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

public class HandlePowerShortageEventCombinedRule extends AgentCombinedRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(HandlePowerShortageEventCombinedRule.class);

	public HandlePowerShortageEventCombinedRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(POWER_SHORTAGE_ERROR_RULE,
				"handle power shortage event",
				"rule handles different cases of power shortage event");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessPowerShortageStartNoAffectedJobsRule(controller),
				new ProcessPowerShortageStartWithAffectedJobsRule(controller),
				new ProcessPowerShortageFinishEventRule(controller)
		);
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final PowerShortageEvent powerShortageEvent = facts.get(EVENT);

		if (!powerShortageEvent.isFinished()) {
			final Instant startTime = powerShortageEvent.getOccurrenceTime();

			logger.info("Internal server error was detected for server! Power will be cut off at: {}", startTime);

			facts.put(EVENT_TIME, startTime);
			facts.put(JOBS, getAffectedPowerJobs(startTime));
		}
		return true;
	}

	private List<ClientJob> getAffectedPowerJobs(final Instant startTime) {
		return agentProps.getServerJobs().keySet().stream()
				.filter(PowerJob::isUnderExecution)
				.filter(job -> startTime.isBefore(job.getExpectedEndTime()))
				.toList();
	}

	@Override
	public AgentRule copy() {
		return new HandlePowerShortageEventCombinedRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
