package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.client.job.listening.processing;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLE_RE_SCHEDULED_RULE;
import static org.greencloud.commons.utils.messaging.MessageReader.readMessageContent;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.RE_SCHEDULED_JOB_ID;
import static org.greencloud.commons.utils.time.TimeConverter.convertToRealTime;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import org.greencloud.commons.domain.job.extended.JobWithTimeFrames;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.client.ClientNode;

import jade.lang.acl.ACLMessage;

public class ProcessSchedulerReScheduleJobUpdateRule extends AgentBasicRule<ClientAgentProps, ClientNode> {

	private static final Logger logger = getLogger(ProcessSchedulerReScheduleJobUpdateRule.class);

	private ACLMessage message;

	public ProcessSchedulerReScheduleJobUpdateRule(
			final RulesController<ClientAgentProps, ClientNode> rulesController) {
		super(rulesController, 8);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE, JOB_STATUS_RECEIVER_HANDLE_RE_SCHEDULED_RULE,
				"handling job status update",
				"triggers handlers upon job status updates");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		message = facts.get(MESSAGE);
		return message.getConversationId().equals(RE_SCHEDULED_JOB_ID);
	}

	/**
	 * Method executes given rule
	 *
	 * @param facts facts used in evaluation
	 */
	@Override
	public void executeRule(final StrategyFacts facts) {
		MDC.put(MDC_JOB_ID, agentProps.getJob().getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
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