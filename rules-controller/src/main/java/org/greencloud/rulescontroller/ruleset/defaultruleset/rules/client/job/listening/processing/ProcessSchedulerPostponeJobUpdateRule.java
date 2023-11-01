package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.client.job.listening.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLE_POSTPONED_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.POSTPONED_JOB_ID;
import static org.greencloud.commons.utils.time.TimeConverter.convertToRealTime;
import static org.greencloud.commons.utils.time.TimeScheduler.postponeTime;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;

import org.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.gui.agents.client.ClientNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.announcing.domain.AnnouncingConstants;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class ProcessSchedulerPostponeJobUpdateRule extends AgentBasicRule<ClientAgentProps, ClientNode> {

	private static final Logger logger = getLogger(ProcessSchedulerPostponeJobUpdateRule.class);

	public ProcessSchedulerPostponeJobUpdateRule(
			final RulesController<ClientAgentProps, ClientNode> rulesController) {
		super(rulesController, 6);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE, JOB_STATUS_RECEIVER_HANDLE_POSTPONED_RULE,
				"handling job status update",
				"triggers handlers upon job status updates");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ACLMessage message = facts.get(MESSAGE);
		return message.getConversationId().equals(POSTPONED_JOB_ID);
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
		logger.info("The execution of my job is being postponed.");

		final Instant postponedStart = postponeTime(agentProps.getJobSimulatedStart(),
				AnnouncingConstants.JOB_RETRY_MINUTES_ADJUSTMENT);
		final Instant postponedEnd = postponeTime(agentProps.getJobSimulatedEnd(),
				AnnouncingConstants.JOB_RETRY_MINUTES_ADJUSTMENT);
		readjustJobTimeFrames(postponedStart, postponedEnd);
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
