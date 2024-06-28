package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.client.job.listening.processing;

import static java.util.Objects.nonNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.CLIENT;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.IN_PROGRESS;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.IN_PROGRESS_CLOUD;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_HANDLE_STARTED_JOB_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.STARTED_IN_CLOUD_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.STARTED_JOB_ID;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.utils.messages.MessageReader.readMessageContent;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.commons.enums.job.JobClientStatusEnum;
import org.greencloud.gui.agents.client.ClientNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

import jade.lang.acl.ACLMessage;

public class ProcessCMAStartedJobUpdateRule extends AgentBasicRule<ClientAgentProps, ClientNode> {

	private static final Logger logger = getLogger(ProcessCMAStartedJobUpdateRule.class);

	public ProcessCMAStartedJobUpdateRule(final RulesController<ClientAgentProps, ClientNode> rulesController) {
		super(rulesController, 10);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE, JOB_STATUS_RECEIVER_HANDLE_STARTED_JOB_RULE,
				"handling job status update.",
				"triggers handlers upon job status updates.");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ACLMessage message = facts.get(MESSAGE);
		return message.getConversationId().equals(STARTED_JOB_ID)
				|| message.getConversationId().equals(STARTED_IN_CLOUD_ID);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ACLMessage message = facts.get(MESSAGE);
		final JobWithStatus jobUpdate = readMessageContent(message, JobWithStatus.class);
		final JobClientStatusEnum statusEnum = message.getConversationId().equals(STARTED_JOB_ID)
				? IN_PROGRESS : IN_PROGRESS_CLOUD;
		agentNode.measureTimeToRetrieveTheMessage(jobUpdate, agentProps);
		agentNode.updateJobStatus(statusEnum);
		agentProps.updateJobStatusDuration(statusEnum, jobUpdate.getChangeTime());

		final String statusMsg = statusEnum.equals(IN_PROGRESS) ? "" : " in cloud";
		logger.info("The execution of my job started{}! :)", statusMsg);

		if (nonNull(jobUpdate.getServerName())) {
			agentNode.updateServerForExecution(jobUpdate.getServerName());
		}
	}

	@Override
	public AgentRule copy() {
		return new ProcessCMAStartedJobUpdateRule(controller);
	}

	@Override
	public String getAgentType() {
		return CLIENT.getName();
	}
}
