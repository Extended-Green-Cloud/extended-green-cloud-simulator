package org.greencloud.agentsystem.strategies.deault.rules.client.job.listening.processing;

import static java.lang.String.valueOf;
import static java.util.Optional.ofNullable;
import static org.greencloud.commons.args.agent.EGCSAgentType.CLIENT;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLE_ACCEPTED_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.ACCEPTED_JOB_ID;
import static org.greencloud.commons.utils.time.TimeConverter.convertMillisecondsToTimeString;
import static org.greencloud.commons.utils.time.TimeConverter.convertToRealTimeMillis;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.jrba.utils.messages.MessageReader.readMessageContent;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Objects;

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

public class ProcessCMAAcceptedJobUpdateRule extends AgentBasicRule<ClientAgentProps, ClientNode> {

	private static final Logger logger = getLogger(ProcessCMAAcceptedJobUpdateRule.class);

	public ProcessCMAAcceptedJobUpdateRule(final RulesController<ClientAgentProps, ClientNode> rulesController) {
		super(rulesController, 12);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE, JOB_STATUS_RECEIVER_HANDLE_ACCEPTED_RULE,
				"handling accepted job status update.",
				"triggers handlers upon job is accepted for execution.");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ACLMessage message = facts.get(MESSAGE);
		return message.getConversationId().equals(ACCEPTED_JOB_ID);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ACLMessage message = facts.get(MESSAGE);
		final JobWithStatus jobUpdate = readMessageContent(message, JobWithStatus.class);
		final long estimatedDuration = convertToRealTimeMillis(ofNullable(jobUpdate.getNewJobExecutionDuration())
				.orElse(agentProps.getExpectedExecutionDuration().doubleValue())
				.longValue());

		MDC.put(MDC_JOB_ID, agentProps.getJob().getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Job has been accepted for execution with estimated price {}!", jobUpdate.getPriceForJob());

		if (!Objects.equals(agentProps.getExpectedExecutionDuration(), estimatedDuration)) {
			final String durationFormatted = convertMillisecondsToTimeString(estimatedDuration);
			logger.info("Estimated job execution duration has changed. New duration: {}", durationFormatted);

			agentProps.setExpectedExecutionDuration(estimatedDuration);
			agentNode.updateEstimatedExecutionTime((double) estimatedDuration / 1000);
		}

		agentNode.updateEstimatedExecutionCost(jobUpdate.getPriceForJob());
		agentProps.saveMonitoringData();
	}

	@Override
	public AgentRule copy() {
		return new ProcessCMAAcceptedJobUpdateRule(controller);
	}

	@Override
	public String getAgentType() {
		return CLIENT.getName();
	}
}
