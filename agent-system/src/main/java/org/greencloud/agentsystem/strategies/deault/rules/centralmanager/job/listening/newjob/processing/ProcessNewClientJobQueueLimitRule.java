package org.greencloud.agentsystem.strategies.deault.rules.centralmanager.job.listening.newjob.processing;

import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_HANDLE_QUEUE_LIMIT_RULE;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

public class ProcessNewClientJobQueueLimitRule extends AgentBasicRule<CentralManagerAgentProps, CMANode> {

	private static final Logger logger = getLogger(ProcessNewClientJobQueueLimitRule.class);

	public ProcessNewClientJobQueueLimitRule(final RulesController<CentralManagerAgentProps, CMANode> controller) {
		super(controller, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_HANDLER_RULE, NEW_JOB_RECEIVER_HANDLE_QUEUE_LIMIT_RULE,
				"handles new client job - queue limit",
				"rule runs when new client job goes over the queue limit");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		return agentProps.getJobsToBeExecuted().size() == agentProps.getMaximumQueueSize() - 1;
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		logger.info("WARNING! The queue has reached the expected threshold!");
	}

	@Override
	public AgentRule copy() {
		return new ProcessNewClientJobQueueLimitRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
