package org.greencloud.agentsystem.strategies.deault.rules.server.job.execution.processing;

import static java.lang.String.valueOf;
import static java.util.Collections.singletonList;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.ENERGY_TYPE;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.energy.EnergyTypeEnum.BACK_UP_POWER;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.STARTED;
import static org.greencloud.commons.enums.job.JobExecutionStateEnum.replaceStatusToActive;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_START_JOB_EXECUTION_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapClientJobToJobInstanceId;
import static org.greencloud.commons.utils.job.JobUtils.getMessageConversationId;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStartedMessage;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForRMA;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.jrba.utils.rules.RuleSetSelector.SELECT_BY_FACTS_IDX;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.greencloud.gui.agents.server.ServerNode;
import org.jeasy.rules.api.Facts;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.schedule.ScheduleOnce;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.AID;

public class ProcessJobStartExecutionRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessJobStartExecutionRule.class);

	public ProcessJobStartExecutionRule(final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROCESS_START_JOB_EXECUTION_RULE,
				"processing start job execution in Server",
				"rule handles start of Job execution in given Server");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		if (facts.get(ENERGY_TYPE).equals(BACK_UP_POWER)) {
			return true;
		}
		final ClientJob job = facts.get(JOB);
		final boolean isGreenSourceSelected = agentProps.getGreenSourceForJobMap().containsKey(job.getJobId());

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		if (!isGreenSourceSelected) {
			logger.info("Job execution couldn't start: there is no green source for the job {}",
					mapClientJobToJobInstanceId(job));
		}
		return isGreenSourceSelected;
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		logger.info("Start executing the job {} by executing step {}.", job.getJobId(), "");

		sendJobStartMessage(facts, job);
		substituteJobStatus(facts, job);
		agentProps.incrementJobCounter(job.getJobId(), STARTED);

		agent.addBehaviour(ScheduleOnce.create(agent, facts, FINISH_JOB_EXECUTION_RULE, controller,
				SELECT_BY_FACTS_IDX));
	}

	private void sendJobStartMessage(final Facts facts, final ClientJob job) {
		final AID greenSource = agentProps.getGreenSourceForJobMap().get(job.getJobId());
		final List<AID> receivers = facts.get(ENERGY_TYPE).equals(BACK_UP_POWER) ?
				singletonList(agentProps.getOwnerRegionalManagerAgent()) :
				List.of(greenSource, agentProps.getOwnerRegionalManagerAgent());

		agent.send(prepareJobStartedMessage(job, facts.get(RULE_SET_IDX), receivers.toArray(new AID[0])));
	}

	private void substituteJobStatus(final Facts facts, final ClientJob job) {
		final JobExecutionStatusEnum currentStatus = agentProps.getServerJobs().get(job);
		final JobExecutionStatusEnum newStatus = replaceStatusToActive(agentProps.getServerJobs(), job);

		agentProps.getJobsExecutionTime().startJobExecutionTimer(job, newStatus, getCurrentTime());
		agent.send(prepareJobStatusMessageForRMA(job, getMessageConversationId(currentStatus), agentProps,
				facts.get(RULE_SET_IDX)));
	}

	@Override
	public AgentRule copy() {
		return new ProcessJobStartExecutionRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
