package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.listening.processing;

import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLE_QUEUE_LIMIT_RULE;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.scheduler.SchedulerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

public class ProcessNewClientJobQueueLimitRule extends AgentBasicRule<SchedulerAgentProps, SchedulerNode> {

	private static final Logger logger = getLogger(ProcessNewClientJobQueueLimitRule.class);

	public ProcessNewClientJobQueueLimitRule(final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
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
		final ClientJob job = facts.get(MESSAGE_CONTENT);
		return !agentProps.getJobsToBeExecuted().offer(job);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		logger.info("WARNING! The queue has reached the expected threshold!");
	}
}
