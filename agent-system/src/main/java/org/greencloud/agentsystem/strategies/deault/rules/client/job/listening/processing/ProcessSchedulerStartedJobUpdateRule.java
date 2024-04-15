package org.greencloud.agentsystem.strategies.deault.rules.client.job.listening.processing;

import static java.lang.String.valueOf;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Objects.nonNull;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.IN_PROGRESS;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.IN_PROGRESS_CLOUD;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLE_STARTED_JOB_RULE;
import static org.jrba.utils.messages.MessageReader.readMessageContent;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.STARTED_IN_CLOUD_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.STARTED_JOB_ID;
import static org.greencloud.commons.utils.time.TimeConverter.convertToRealTime;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.time.temporal.ValueRange;

import org.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.commons.enums.job.JobClientStatusEnum;
import org.greencloud.gui.agents.client.ClientNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class ProcessSchedulerStartedJobUpdateRule extends AgentBasicRule<ClientAgentProps, ClientNode> {

	private static final Logger logger = getLogger(ProcessSchedulerStartedJobUpdateRule.class);
	private static final ValueRange MAX_TIME_DIFFERENCE = ValueRange.of(-200, 200);

	public ProcessSchedulerStartedJobUpdateRule(final RulesController<ClientAgentProps, ClientNode> rulesController) {
		super(rulesController, 10);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE, JOB_STATUS_RECEIVER_HANDLE_STARTED_JOB_RULE,
				"handling job status update",
				"triggers handlers upon job status updates");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ACLMessage message = facts.get(MESSAGE);
		return message.getConversationId().equals(STARTED_JOB_ID)
				|| message.getConversationId().equals(STARTED_IN_CLOUD_ID);
	}

	/**
	 * Method executes given rule
	 *
	 * @param facts facts used in evaluation
	 */
	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ACLMessage message = facts.get(MESSAGE);
		final JobWithStatus jobUpdate = readMessageContent(message, JobWithStatus.class);
		final JobClientStatusEnum statusEnum = message.getConversationId().equals(STARTED_JOB_ID)
				? IN_PROGRESS : IN_PROGRESS_CLOUD;
		agentNode.measureTimeToRetrieveTheMessage(jobUpdate, agentProps);
		agentNode.updateJobStatus(statusEnum);
		agentProps.updateJobStatusDuration(statusEnum, jobUpdate.getChangeTime());
		checkIfJobStartedOnTime(jobUpdate.getChangeTime(), agentProps.getJobSimulatedStart(), facts, statusEnum);

		if (nonNull(jobUpdate.getServerName())) {
			agentNode.updateServerForExecution(jobUpdate.getServerName());
		}
	}

	protected void checkIfJobStartedOnTime(final Instant startTime, final Instant jobStartTime,
			final RuleSetFacts facts, final JobClientStatusEnum statusEnum) {
		final long timeDifference = MILLIS.between(jobStartTime, startTime);
		final String statusMsg = statusEnum.equals(IN_PROGRESS) ? "" : " in cloud";

		MDC.put(MDC_JOB_ID, agentProps.getJob().getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		if (MAX_TIME_DIFFERENCE.isValidValue(timeDifference)) {
			logger.info("The execution of my job started on time{}! :)", statusMsg);
		} else {
			logger.info("The execution of my job started{} {} min after the preferred start time",
					statusMsg, convertToRealTime(timeDifference));
		}
		agentProps.saveMonitoringData();
	}
}
