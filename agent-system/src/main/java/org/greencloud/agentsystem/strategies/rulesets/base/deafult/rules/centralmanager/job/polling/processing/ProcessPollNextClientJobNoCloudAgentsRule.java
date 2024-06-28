package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.job.polling.processing;

import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_POLLING_HANDLE_NO_CLOUD_AGENTS_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_POLLING_RULE;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

public class ProcessPollNextClientJobNoCloudAgentsRule extends AgentBasicRule<CentralManagerAgentProps, CMANode> {

	private static final Logger logger = getLogger(ProcessPollNextClientJobNoCloudAgentsRule.class);

	public ProcessPollNextClientJobNoCloudAgentsRule(
			final RulesController<CentralManagerAgentProps, CMANode> controller) {
		super(controller, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_POLLING_RULE, NEW_JOB_POLLING_HANDLE_NO_CLOUD_AGENTS_RULE,
				"do not announce job when there are no RMAs",
				"when CMA has no RMA agents, it logs a message");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		return agentProps.getAvailableRegionalManagers().isEmpty();
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		logger.info("There are no available Regional Manager Agents!");
	}

	@Override
	public AgentRule copy() {
		return new ProcessPollNextClientJobNoCloudAgentsRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
