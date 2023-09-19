package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.client.job.listening.processing;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.FAILED;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLE_FAILED_JOB_RULE;
import static org.greencloud.commons.utils.messaging.MessageReader.readMessageContent;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.FAILED_JOB_ID;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.client.ClientNode;

import jade.lang.acl.ACLMessage;

public class ProcessSchedulerFailedJobUpdateRule extends AgentBasicRule<ClientAgentProps, ClientNode> {

	private static final Logger logger = getLogger(ProcessSchedulerFailedJobUpdateRule.class);

	private ACLMessage message;

	public ProcessSchedulerFailedJobUpdateRule(
			final RulesController<ClientAgentProps, ClientNode> rulesController) {
		super(rulesController, 11);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE, JOB_STATUS_RECEIVER_HANDLE_FAILED_JOB_RULE,
				"handling job status update",
				"triggers handlers upon job status updates");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		message = facts.get(MESSAGE);
		return message.getConversationId().equals(FAILED_JOB_ID);
	}

	/**
	 * Method executes given rule
	 *
	 * @param facts facts used in evaluation
	 */
	@Override
	public void executeRule(final StrategyFacts facts) {
		final JobWithStatus jobUpdate = readMessageContent(message, JobWithStatus.class);

		MDC.put(MDC_JOB_ID, agentProps.getJob().getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("The execution of my job has failed :(");

		agentNode.updateJobStatus(FAILED);
		agentProps.updateJobStatusDuration(FAILED, jobUpdate.getChangeTime());
		agentNode.removeClient();
		agentNode.announceFailedJob();
		agentNode.setFinished(true);
		agentProps.saveMonitoringData();
		agent.doDelete();
	}
}
