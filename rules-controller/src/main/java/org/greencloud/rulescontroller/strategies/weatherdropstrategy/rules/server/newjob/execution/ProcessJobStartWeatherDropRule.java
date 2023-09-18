package org.greencloud.rulescontroller.strategies.weatherdropstrategy.rules.server.newjob.execution;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.STARTED;
import static org.greencloud.commons.enums.job.JobExecutionStateEnum.replaceStatusToActive;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_START_JOB_EXECUTION_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToJobInstanceId;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.BACK_UP_POWER_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.ON_HOLD_JOB_ID;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStartedMessage;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForCNA;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.schedule.ScheduleOnce;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.server.ServerNode;

public class ProcessJobStartWeatherDropRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessJobStartWeatherDropRule.class);

	public ProcessJobStartWeatherDropRule(final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROCESS_START_JOB_EXECUTION_RULE,
				"processing start job execution in Server",
				"rule handles start of Job execution in given Server");
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("Start executing the job {}.", job.getJobId());

		final JobExecutionStatusEnum currentStatus = agentProps.getServerJobs().get(job);
		final JobInstanceIdentifier jobInstance = mapToJobInstanceId(job);

		replaceStatusToActive(agentProps.getServerJobs(), job);

		agent.send(prepareJobStartedMessage(job, facts.get(STRATEGY_IDX), agentProps.getOwnerCloudNetworkAgent()));
		agent.send(prepareJobStatusMessageForCNA(jobInstance, getStatus(currentStatus), agentProps,
				facts.get(STRATEGY_IDX)));
		agentProps.incrementJobCounter(mapToJobInstanceId(job), STARTED);

		agent.addBehaviour(
				ScheduleOnce.create(agent, facts, FINISH_JOB_EXECUTION_RULE, controller, f -> f.get(STRATEGY_IDX)));
	}

	private String getStatus(final JobExecutionStatusEnum currentStatus) {
		return switch (currentStatus) {
			case ON_HOLD_SOURCE_SHORTAGE_PLANNED, ON_HOLD_PLANNED, ON_HOLD_TRANSFER_PLANNED -> ON_HOLD_JOB_ID;
			case IN_PROGRESS_BACKUP_ENERGY_PLANNED -> BACK_UP_POWER_JOB_ID;
			default -> null;
		};
	}
}
