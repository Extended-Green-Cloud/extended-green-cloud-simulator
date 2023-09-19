package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.scheduler.job.listening.processing;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.CREATED;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLE_JOB_DUPLICATE_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.SCHEDULED_JOB_ID;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.scheduler.SchedulerNode;

public class ProcessNewClientJobRule extends AgentBasicRule<SchedulerAgentProps, SchedulerNode> {

	private static final Logger logger = getLogger(ProcessNewClientJobRule.class);

	public ProcessNewClientJobRule(final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
		super(controller, 1);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_HANDLER_RULE, NEW_JOB_RECEIVER_HANDLE_JOB_DUPLICATE_RULE,
				"handles new client job",
				"rule runs when new client job was received");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		final ClientJob job = facts.get(MESSAGE_CONTENT);
		return agentProps.getJobsToBeExecuted().offer(job);
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final ClientJob job = facts.get(MESSAGE_CONTENT);

		agentProps.addJob(job, facts.get(STRATEGY_IDX), CREATED);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("Job {} has been successfully added to job scheduling queue", job.getJobId());

		agentNode.updateScheduledJobQueue(agentProps);
		agent.send(prepareJobStatusMessageForClient(job, SCHEDULED_JOB_ID, facts.get(STRATEGY_IDX)));
	}
}