package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.scheduler.job.polling.processing;

import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_POLLING_HANDLE_NO_CLOUD_AGENTS_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_POLLING_RULE;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.scheduler.SchedulerNode;

public class ProcessPollNextClientJobNoCloudAgentsRule extends AgentBasicRule<SchedulerAgentProps, SchedulerNode> {
	private static final Logger logger = getLogger(ProcessPollNextClientJobNoCloudAgentsRule.class);

	public ProcessPollNextClientJobNoCloudAgentsRule(
			final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
		super(controller, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_POLLING_RULE, NEW_JOB_POLLING_HANDLE_NO_CLOUD_AGENTS_RULE,
				"do not announce job when there are no CNAs",
				"when Scheduler has no CNA agents, it logs a message");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		return agentProps.getAvailableCloudNetworks().isEmpty();
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		logger.info("There are no available Cloud Network Agents!");
	}
}
