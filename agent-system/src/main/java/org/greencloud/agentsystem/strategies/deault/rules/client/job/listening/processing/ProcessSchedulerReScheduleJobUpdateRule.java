package org.greencloud.agentsystem.strategies.deault.rules.client.job.listening.processing;

import static java.lang.String.valueOf;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLE_RE_SCHEDULED_RULE;
import static org.jrba.utils.messages.MessageReader.readMessageContent;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.RE_SCHEDULED_JOB_ID;
import static org.greencloud.commons.utils.time.TimeConverter.convertToRealTime;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;

import org.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.greencloud.commons.domain.job.extended.JobWithTimeFrames;
import org.greencloud.gui.agents.client.ClientNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class ProcessSchedulerReScheduleJobUpdateRule extends AgentBasicRule<ClientAgentProps, ClientNode> {

	private static final Logger logger = getLogger(ProcessSchedulerReScheduleJobUpdateRule.class);

	private ACLMessage message;

	public ProcessSchedulerReScheduleJobUpdateRule(final RulesController<ClientAgentProps, ClientNode> rulesController) {
		super(rulesController, 8);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE, JOB_STATUS_RECEIVER_HANDLE_RE_SCHEDULED_RULE,
				"handling job status update",
				"triggers handlers upon job status updates");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		message = facts.get(MESSAGE);
		return message.getConversationId().equals(RE_SCHEDULED_JOB_ID);
	}

	/**
	 * Method executes given rule
	 *
	 * @param facts facts used in evaluation
	 */
	@Override
	public void executeRule(final RuleSetFacts facts) {
		MDC.put(MDC_JOB_ID, agentProps.getJob().getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("The time frames of my job has been adjusted.");

		final JobWithTimeFrames newTimeFrames = readMessageContent(message, JobWithTimeFrames.class);
		readjustJobTimeFrames(newTimeFrames.getNewJobStart(), newTimeFrames.getNewJobEnd());
		agentProps.saveMonitoringData();
	}

	protected void readjustJobTimeFrames(final Instant newStart, final Instant newEnd) {
		agentProps.setJobSimulatedStart(newStart);
		agentProps.setJobSimulatedEnd(newEnd);

		agentNode.updateJobTimeFrame(
				convertToRealTime(agentProps.getJobSimulatedStart()),
				convertToRealTime(agentProps.getJobSimulatedEnd()));
	}
}
