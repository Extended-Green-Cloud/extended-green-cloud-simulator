package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.client.job.listening.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLE_ACCEPTED_RULE;
import static org.greencloud.commons.utils.messaging.MessageReader.readMessageContent;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.ACCEPTED_JOB_ID;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.gui.agents.client.ClientNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class ProcessSchedulerAcceptedJobUpdateRule extends AgentBasicRule<ClientAgentProps, ClientNode> {

	private static final Logger logger = getLogger(ProcessSchedulerAcceptedJobUpdateRule.class);

	private ACLMessage message;

	public ProcessSchedulerAcceptedJobUpdateRule(
			final RulesController<ClientAgentProps, ClientNode> rulesController) {
		super(rulesController, 12);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE, JOB_STATUS_RECEIVER_HANDLE_ACCEPTED_RULE,
				"handling accepted job status update",
				"triggers handlers upon job is accepted for execution");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		message = facts.get(MESSAGE);
		return message.getConversationId().equals(ACCEPTED_JOB_ID);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final JobWithStatus jobUpdate = readMessageContent(message, JobWithStatus.class);

		MDC.put(MDC_JOB_ID, agentProps.getJob().getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Job has been accepted for execution with estimated price {}!", jobUpdate.getPriceForJob());

		agentNode.updateEstimatedExecutionCost(jobUpdate.getPriceForJob());
		agentProps.saveMonitoringData();
	}
}
