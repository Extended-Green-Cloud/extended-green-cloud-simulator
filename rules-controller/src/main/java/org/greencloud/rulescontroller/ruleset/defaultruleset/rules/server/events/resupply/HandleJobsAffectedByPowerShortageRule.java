package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.resupply;

import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.POWER_SHORTAGE_SOURCE_STATUSES;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.enums.rules.RuleType.CHECK_AFFECTED_JOBS_RULE;
import static org.greencloud.commons.enums.rules.RuleType.CHECK_SINGLE_AFFECTED_JOB_RULE;
import static java.util.stream.Collectors.toSet;

import java.util.Map;
import java.util.Set;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentPeriodicRule;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import com.gui.agents.server.ServerNode;

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

	/**
	 * Method specify period after which behaviour is to be executed
	 */
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
			final int ruleSet = agentProps.getRuleSetForJob().get(job.getJobInstanceId());
			final RuleSetFacts handlerFacts = new RuleSetFacts(ruleSet);
			handlerFacts.put(RULE_TYPE, CHECK_SINGLE_AFFECTED_JOB_RULE);
			handlerFacts.put(JOB, job);
			controller.fire(handlerFacts);
		});
	}
}
