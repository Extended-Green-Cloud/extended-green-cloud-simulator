package org.greencloud.agentsystem.strategies.deault.rules.server.events.errorserver.processing;

import static java.util.Objects.nonNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.HANDLE_POWER_SHORTAGE_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.POWER_SHORTAGE_ERROR_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.POWER_SHORTAGE_ERROR_START_NONE_AFFECTED_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.SET_EVENT_ERROR;
import static org.jrba.utils.rules.RuleSetSelector.SELECT_LATEST;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.schedule.ScheduleOnce;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

public class ProcessPowerShortageStartNoAffectedJobsRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessPowerShortageStartNoAffectedJobsRule.class);

	public ProcessPowerShortageStartNoAffectedJobsRule(
			final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(POWER_SHORTAGE_ERROR_RULE, POWER_SHORTAGE_ERROR_START_NONE_AFFECTED_RULE,
				"handle power shortage start event - no affected jobs",
				"rule handles start of power shortage event");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final List<ClientJob> affectedJobs = facts.get(JOBS);
		return nonNull(affectedJobs) && affectedJobs.isEmpty();
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		logger.info("Internal server error won't affect any jobs");

		facts.put(SET_EVENT_ERROR, true);
		agent.addBehaviour(ScheduleOnce.create(agent, facts, HANDLE_POWER_SHORTAGE_RULE, controller, SELECT_LATEST));
	}

	@Override
	public AgentRule copy() {
		return new ProcessPowerShortageStartNoAffectedJobsRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
