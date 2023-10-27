package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.execution;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.STARTED;
import static org.greencloud.commons.enums.job.JobExecutionStateEnum.replaceStatusToActive;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_START_INFORM;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.enums.rules.RuleType.FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_START_JOB_EXECUTION_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getMessageConversationId;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStartedMessage;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForCNA;
import static java.lang.String.valueOf;
import static java.util.Collections.singletonList;
import static org.greencloud.rulescontroller.ruleset.RuleSetSelector.SELECT_BY_FACTS_IDX;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.greencloud.commons.mapper.JobMapper;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.schedule.ScheduleOnce;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.jeasy.rules.api.Facts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import com.gui.agents.server.ServerNode;

import jade.core.AID;

public class ProcessJobStartRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessJobStartRule.class);

	private ClientJob job;
	private boolean informAboutStart;

	public ProcessJobStartRule(final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROCESS_START_JOB_EXECUTION_RULE,
				"processing start job execution in Server",
				"rule handles start of Job execution in given Server");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		job = facts.get(JOB);
		informAboutStart = facts.get(JOB_START_INFORM);

		if (!agentProps.getGreenSourceForJobMap().containsKey(job.getJobId())) {
			logger.info("Job execution couldn't start: there is no green source for the job {}",
					JobMapper.mapClientJobToJobInstanceId(job));
			return;
		}

		final String logMessage = informAboutStart
				? "Start executing the job {} by executing step {}."
				: "Start executing the job {} by executing step {} without informing CNA";

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info(logMessage, job.getJobId());

		sendJobStartMessage(facts);
		substituteJobStatus(facts);
		agentProps.incrementJobCounter(JobMapper.mapClientJobToJobInstanceId(job), STARTED);

		agent.addBehaviour(
				ScheduleOnce.create(agent, facts, FINISH_JOB_EXECUTION_RULE, controller, SELECT_BY_FACTS_IDX));
	}

	private void sendJobStartMessage(final Facts facts) {
		final AID greenSource = agentProps.getGreenSourceForJobMap().get(job.getJobId());
		final List<AID> receivers = informAboutStart ?
				List.of(greenSource, agentProps.getOwnerCloudNetworkAgent()) :
				singletonList(greenSource);

		agent.send(prepareJobStartedMessage(job, facts.get(RULE_SET_IDX), receivers.toArray(new AID[0])));
	}

	private void substituteJobStatus(final Facts facts) {
		final JobExecutionStatusEnum currentStatus = agentProps.getServerJobs().get(job);
		final JobInstanceIdentifier jobInstance = JobMapper.mapClientJobToJobInstanceId(job);

		replaceStatusToActive(agentProps.getServerJobs(), job);
		agent.send(prepareJobStatusMessageForCNA(jobInstance, getMessageConversationId(currentStatus), agentProps,
				facts.get(RULE_SET_IDX)));
	}
}
