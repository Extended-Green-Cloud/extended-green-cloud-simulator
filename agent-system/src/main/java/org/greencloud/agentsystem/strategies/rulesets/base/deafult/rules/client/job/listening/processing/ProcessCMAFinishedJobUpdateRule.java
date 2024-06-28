package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.client.job.listening.processing;

import static java.lang.String.valueOf;
import static java.time.temporal.ChronoUnit.MILLIS;
import static org.greencloud.commons.args.agent.EGCSAgentType.CLIENT;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.FINISHED;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.IN_PROGRESS;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.IN_PROGRESS_CLOUD;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLE_FINISHED_JOB_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.FINISH_JOB_ID;
import static org.greencloud.commons.utils.time.TimeConverter.convertToRealTime;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.jrba.utils.messages.MessageReader.readMessageContent;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;

import org.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.gui.agents.client.ClientNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class ProcessCMAFinishedJobUpdateRule extends AgentBasicRule<ClientAgentProps, ClientNode> {

	private static final Logger logger = getLogger(ProcessCMAFinishedJobUpdateRule.class);

	public ProcessCMAFinishedJobUpdateRule(final RulesController<ClientAgentProps, ClientNode> rulesController) {
		super(rulesController, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE, JOB_STATUS_RECEIVER_HANDLE_FINISHED_JOB_RULE,
				"handling job status update.",
				"triggers handlers upon job status updates.");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ACLMessage message = facts.get(MESSAGE);
		return message.getConversationId().equals(FINISH_JOB_ID);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ACLMessage message = facts.get(MESSAGE);
		final JobWithStatus jobUpdate = readMessageContent(message, JobWithStatus.class);
		agentNode.measureTimeToRetrieveTheMessage(jobUpdate, agentProps);
		agentNode.updateJobStatus(FINISHED);
		agentNode.updateFinalExecutionCost(jobUpdate.getPriceForJob());
		agentNode.updateJobExecutionFinishDate(convertToRealTime(jobUpdate.getChangeTime()));
		agentProps.updateJobStatusDuration(FINISHED, jobUpdate.getChangeTime());
		agentNode.setWithinBudget(agentProps.isJobWithinBudget(jobUpdate.getPriceForJob()));

		MDC.put(MDC_JOB_ID, agentProps.getJob().getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		checkIfJobFinishedOnTime(jobUpdate.getChangeTime(), agentProps.getJobSimulatedDeadline(), facts);
		shutdownAfterFinishedJob();
	}

	private void shutdownAfterFinishedJob() {
		logger.info("Job finished! Agent shutdown initiated!");

		if (agentProps.getJobDurationMap().get(IN_PROGRESS_CLOUD) > agentProps.getJobDurationMap().get(IN_PROGRESS)) {
			agentNode.incrementFinishedInCloud();
		}
		agentNode.removeClient();
		agentNode.removeClientJob();
		agentNode.setFinished(true);
		agentProps.saveMonitoringData();
		agent.doDelete();
	}

	protected void checkIfJobFinishedOnTime(final Instant endTime, final Instant jobDeadline,
			final RuleSetFacts facts) {
		MDC.put(MDC_JOB_ID, agentProps.getJob().getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));

		if (endTime.isAfter(jobDeadline)) {
			final long deadlineDifference = MILLIS.between(endTime, jobDeadline);
			final long delay = convertToRealTime(deadlineDifference);
			agentNode.setWithinDeadline(false);
			logger.info("The execution of my job finished with a delay equal to {} min! :(", delay);
		} else {
			agentNode.setWithinDeadline(true);
			logger.info("The execution of my job finished!");
		}
	}

	@Override
	public AgentRule copy() {
		return new ProcessCMAFinishedJobUpdateRule(controller);
	}

	@Override
	public String getAgentType() {
		return CLIENT.getName();
	}

}
