package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.scheduler.job.listening.processing;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLE_FINISHED_JOB_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.FINISH_JOB_ID;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.scheduler.SchedulerNode;

public class ProcessCNAJobStatusUpdateFinishedJobRule extends AgentBasicRule<SchedulerAgentProps, SchedulerNode> {

	private static final Logger logger = getLogger(ProcessCNAJobStatusUpdateFinishedJobRule.class);

	public ProcessCNAJobStatusUpdateFinishedJobRule(
			final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
		super(controller, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE, JOB_STATUS_RECEIVER_HANDLE_FINISHED_JOB_RULE,
				"handles job update - finished job",
				"rule runs when new client job was finished");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		final String type = facts.get(MESSAGE_TYPE);
		return type.equals(FINISH_JOB_ID);
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final Optional<ClientJob> jobOptional = facts.get(JOB);

		if (jobOptional.isPresent()) {
			final ClientJob job = jobOptional.get();
			final JobWithStatus jobStatusUpdate = facts.get(MESSAGE_CONTENT);

			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
			logger.info("Client job {} execution has finished.", job.getJobId());

			final int strategyIdx = agentProps.removeJob(job);
			controller.removeStrategy(agentProps.getStrategyForJob(), strategyIdx);
			agentProps.getCnaForJobMap().remove(job.getJobId());
			agent.send(prepareJobStatusMessageForClient(job, jobStatusUpdate, FINISH_JOB_ID, facts.get(STRATEGY_IDX)));
		}
	}
}
