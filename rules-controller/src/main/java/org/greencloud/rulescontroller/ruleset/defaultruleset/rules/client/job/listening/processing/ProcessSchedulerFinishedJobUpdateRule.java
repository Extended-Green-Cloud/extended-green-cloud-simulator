package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.client.job.listening.processing;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.FINISHED;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.IN_PROGRESS;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.IN_PROGRESS_CLOUD;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLE_FINISHED_JOB_RULE;
import static org.greencloud.commons.utils.messaging.MessageReader.readMessageContent;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.FINISH_JOB_ID;
import static org.greencloud.commons.utils.time.TimeConverter.convertToRealTime;
import static java.lang.String.valueOf;
import static java.time.temporal.ChronoUnit.MILLIS;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.time.temporal.ValueRange;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import com.gui.agents.client.ClientNode;

import jade.lang.acl.ACLMessage;

public class ProcessSchedulerFinishedJobUpdateRule extends AgentBasicRule<ClientAgentProps, ClientNode> {

	private static final Logger logger = getLogger(ProcessSchedulerFinishedJobUpdateRule.class);
	private static final ValueRange MAX_TIME_DIFFERENCE = ValueRange.of(-200, 200);

	private ACLMessage message;

	public ProcessSchedulerFinishedJobUpdateRule(
			final RulesController<ClientAgentProps, ClientNode> rulesController) {
		super(rulesController, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE, JOB_STATUS_RECEIVER_HANDLE_FINISHED_JOB_RULE,
				"handling job status update",
				"triggers handlers upon job status updates");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		message = facts.get(MESSAGE);
		return message.getConversationId().equals(FINISH_JOB_ID);
	}

	/**
	 * Method executes given rule
	 *
	 * @param facts facts used in evaluation
	 */
	@Override
	public void executeRule(final RuleSetFacts facts) {
		final JobWithStatus jobUpdate = readMessageContent(message, JobWithStatus.class);
		agentNode.measureTimeToRetrieveTheMessage(jobUpdate, agentProps);
		agentNode.updateJobStatus(FINISHED);
		agentProps.updateJobStatusDuration(FINISHED, jobUpdate.getChangeTime());

		MDC.put(MDC_JOB_ID, agentProps.getJob().getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		checkIfJobFinishedOnTime(jobUpdate.getChangeTime(), agentProps.getJobSimulatedEnd(),
				agentProps.getJobSimulatedDeadline(), facts);
		shutdownAfterFinishedJob();
	}

	private void shutdownAfterFinishedJob() {
		logger.info("Job finished! Agent shutdown initiated!");

		if(agentProps.getJobDurationMap().get(IN_PROGRESS_CLOUD) > agentProps.getJobDurationMap().get(IN_PROGRESS)) {
			agentNode.incrementFinishedInCloud();
		}
		agentNode.removeClient();
		agentNode.removeClientJob();
		agentNode.setFinished(true);
		agentProps.saveMonitoringData();
		agent.doDelete();
	}

	protected void checkIfJobFinishedOnTime(final Instant endTime, final Instant jobEndTime,
			final Instant jobDeadline, final RuleSetFacts facts) {
		MDC.put(MDC_JOB_ID, agentProps.getJob().getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		if (!jobDeadline.isBefore(endTime)) {
			final long timeDifference = MILLIS.between(endTime, jobEndTime);
			final long delay = -1 * convertToRealTime(timeDifference);

			if (delay == 0) {
				logger.info("The execution of my job finished!");
			} else {
				logger.info("The execution of my job finished (with delay {} min) before deadline", delay);
			}
		} else {
			final long deadlineDifference = MILLIS.between(endTime, jobDeadline);

			if (MAX_TIME_DIFFERENCE.isValidValue(deadlineDifference)) {
				logger.info("The execution of my job finished on time! :)");
			} else {
				logger.info("The execution of my job finished with a delay equal to {}! :(", deadlineDifference);
			}
		}
	}

}
