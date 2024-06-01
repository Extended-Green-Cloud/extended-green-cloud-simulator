package org.greencloud.agentsystem.strategies.deafult.rules.server.job.execution.processing;

import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.job.JobExecutionStateEnum.EXECUTING_ON_BACK_UP;
import static org.greencloud.commons.enums.job.JobExecutionStateEnum.EXECUTING_ON_HOLD_SOURCE;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ON_HOLD_SOURCE_SHORTAGE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_FINISH_JOB_BACK_UP_EXECUTION_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapClientJobToJobInstanceId;
import static org.greencloud.commons.utils.resources.ResourcesUtilization.areSufficient;
import static org.greencloud.commons.utils.time.TimeComparator.isWithinSimulatedTimeStamp;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

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
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		return nonNull(agentProps.getGreenSourceForJobMap().get(job.getJobId()));
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		agentProps.getServerJobs().entrySet().stream()
				.filter(jobEntry -> jobEntry.getKey().isUnderExecution())
				.filter(jobEntry -> isWithinSimulatedTimeStamp(jobEntry.getKey(), getCurrentTime()))
				.filter(jobEntry -> EXECUTING_ON_HOLD_SOURCE.getStatuses().contains(jobEntry.getValue()))
				.forEach(jobEntry -> supplyJobWithBackUpPower(jobEntry.getKey(), jobEntry.getValue(), facts));
	}

	private void supplyJobWithBackUpPower(final ClientJob job, final JobExecutionStatusEnum status,
			final RuleSetFacts facts) {
		final Map<String, Resource> resourcesBackUp = agentProps.getAvailableResources(job,
				mapClientJobToJobInstanceId(job), EXECUTING_ON_HOLD_SOURCE.getStatuses());

		if (areSufficient(resourcesBackUp, job.getRequiredResources())) {
			final boolean hasStarted = status.equals(ON_HOLD_SOURCE_SHORTAGE);

			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
			logger.info("Supplying job {} with back up power", job.getJobId());

			final JobExecutionStatusEnum prevStatus = agentProps.getServerJobs().get(job);
			final JobExecutionStatusEnum newStatus = EXECUTING_ON_BACK_UP.getStatus(hasStarted);

			agentProps.getJobsExecutionTime().updateJobExecutionDuration(job, prevStatus, newStatus, getCurrentTime());
			agentProps.getServerJobs().replace(job, newStatus);
			agentProps.updateGUI();
		}
	}

	@Override
	public AgentRule copy() {
		return new ProcessJobFinishOnBackUpPowerRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
