package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.events.resupply;

import static java.util.stream.Collectors.toSet;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.POWER_SHORTAGE_SOURCE_STATUSES;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.CHECK_AFFECTED_JOBS_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.CHECK_SINGLE_AFFECTED_JOB_RULE;
import static org.greencloud.commons.utils.facts.JobFactsFactory.constructFactsWithJob;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;

import java.util.Map;
import java.util.Set;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentPeriodicRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

public class HandleJobsAffectedByPowerShortageRule extends AgentPeriodicRule<ServerAgentProps, ServerNode> {

	private static final long SERVER_CHECK_POWER_SHORTAGE_JOBS = 2000L;

	public HandleJobsAffectedByPowerShortageRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(CHECK_AFFECTED_JOBS_RULE,
				"check if there are jobs affected by power shortage",
				"rule verifies if there are jobs on power shortage and handles them according to appropriate rule set");
	}

	@Override
	protected long specifyPeriod() {
		return SERVER_CHECK_POWER_SHORTAGE_JOBS;
	}

	@Override
	protected void handleActionTrigger(final RuleSetFacts facts) {
		final Set<ClientJob> affectedJobs = agentProps.getServerJobs().entrySet().stream()
				.filter(entry -> POWER_SHORTAGE_SOURCE_STATUSES.contains(entry.getValue()))
				.map(Map.Entry::getKey)
				.collect(toSet());

		affectedJobs.forEach(job -> {
			final RuleSetFacts handlerFacts = constructFactsWithJob(facts.get(RULE_SET_IDX), job);
			handlerFacts.put(RULE_TYPE, CHECK_SINGLE_AFFECTED_JOB_RULE);
			controller.fire(handlerFacts);
		});
	}

	@Override
	public AgentRule copy() {
		return new HandleJobsAffectedByPowerShortageRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
