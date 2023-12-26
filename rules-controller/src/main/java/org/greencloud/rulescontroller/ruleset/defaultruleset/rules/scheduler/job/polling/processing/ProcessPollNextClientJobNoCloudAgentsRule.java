package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.polling.processing;

import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_POLLING_HANDLE_NO_CLOUD_AGENTS_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_POLLING_RULE;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.gui.agents.scheduler.SchedulerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

public class ProcessPollNextClientJobNoCloudAgentsRule extends AgentBasicRule<SchedulerAgentProps, SchedulerNode> {
	private static final Logger logger = getLogger(ProcessPollNextClientJobNoCloudAgentsRule.class);

	public ProcessPollNextClientJobNoCloudAgentsRule(
			final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
		super(controller, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_POLLING_RULE, NEW_JOB_POLLING_HANDLE_NO_CLOUD_AGENTS_RULE,
				"do not announce job when there are no RMAs",
				"when Scheduler has no RMA agents, it logs a message");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		return agentProps.getAvailableRegionalManagers().isEmpty();
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		logger.info("There are no available Regional Manager Agents!");
	}
}
