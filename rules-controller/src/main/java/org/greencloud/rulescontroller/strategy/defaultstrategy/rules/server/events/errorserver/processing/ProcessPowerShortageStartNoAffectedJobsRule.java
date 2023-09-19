package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.server.events.errorserver.processing;

import static org.greencloud.commons.constants.FactTypeConstants.JOBS;
import static org.greencloud.commons.constants.FactTypeConstants.SET_EVENT_ERROR;
import static org.greencloud.commons.enums.rules.RuleType.HANDLE_POWER_SHORTAGE_RULE;
import static org.greencloud.commons.enums.rules.RuleType.POWER_SHORTAGE_ERROR_START_NONE_AFFECTED_RULE;
import static org.greencloud.commons.enums.rules.RuleType.POWER_SHORTAGE_ERROR_START_RULE;
import static org.greencloud.rulescontroller.strategy.StrategySelector.SELECT_LATEST;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.schedule.ScheduleOnce;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

import com.gui.agents.server.ServerNode;

public class ProcessPowerShortageStartNoAffectedJobsRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessPowerShortageStartNoAffectedJobsRule.class);

	public ProcessPowerShortageStartNoAffectedJobsRule(
			final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(POWER_SHORTAGE_ERROR_START_RULE, POWER_SHORTAGE_ERROR_START_NONE_AFFECTED_RULE,
				"handle power shortage start event - no affected jobs",
				"rule handles start of power shortage event");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		final List<ClientJob> affectedJobs = facts.get(JOBS);
		return affectedJobs.isEmpty();
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		logger.info("Internal server error won't affect any jobs");

		facts.put(SET_EVENT_ERROR, true);
		agent.addBehaviour(ScheduleOnce.create(agent, facts, HANDLE_POWER_SHORTAGE_RULE, controller, SELECT_LATEST));
	}
}
