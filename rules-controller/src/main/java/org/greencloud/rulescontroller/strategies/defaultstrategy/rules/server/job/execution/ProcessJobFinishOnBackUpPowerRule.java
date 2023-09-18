package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.server.job.execution;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.enums.job.JobExecutionStateEnum.EXECUTING_ON_BACK_UP;
import static org.greencloud.commons.enums.job.JobExecutionStateEnum.EXECUTING_ON_HOLD_SOURCE;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ON_HOLD_SOURCE_SHORTAGE;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_FINISH_JOB_BACK_UP_EXECUTION_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToJobInstanceId;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.greencloud.commons.utils.time.TimeComparator.isWithinTimeStamp;
import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.resources.HardwareResources;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.server.ServerNode;

public class ProcessJobFinishOnBackUpPowerRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessJobFinishOnBackUpPowerRule.class);

	public ProcessJobFinishOnBackUpPowerRule(final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROCESS_FINISH_JOB_BACK_UP_EXECUTION_RULE,
				"processing finish of the job executed with back-up in Server",
				"rule handles finish of the Job executed with back-up in given Server");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		return nonNull(agentProps.getGreenSourceForJobMap().get(job.getJobId()));
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		agentProps.getServerJobs().entrySet().stream()
				.filter(clientJob -> isWithinTimeStamp(clientJob.getKey().getStartTime(),
						clientJob.getKey().getEndTime(), getCurrentTime()))
				.filter(clientJob -> EXECUTING_ON_HOLD_SOURCE.getStatuses().contains(clientJob.getValue()))
				.forEach(jobEntry -> {
					final ClientJob clientJob = jobEntry.getKey();
					final HardwareResources resourcesBackUp = agentProps.getAvailableResources(clientJob.getStartTime(),
							clientJob.getEndTime(), mapToJobInstanceId(clientJob),
							EXECUTING_ON_BACK_UP.getStatuses());

					if (resourcesBackUp.areSufficient(clientJob.getEstimatedResources())) {
						final boolean hasStarted = jobEntry.getValue().equals(ON_HOLD_SOURCE_SHORTAGE);

						MDC.put(MDC_JOB_ID, clientJob.getJobId());
						MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
						logger.info("Supplying job {} with back up power", clientJob.getJobId());

						agentProps.getServerJobs().replace(clientJob, EXECUTING_ON_BACK_UP.getStatus(hasStarted));
						agentProps.updateGUI();
					}
				});
	}
}
