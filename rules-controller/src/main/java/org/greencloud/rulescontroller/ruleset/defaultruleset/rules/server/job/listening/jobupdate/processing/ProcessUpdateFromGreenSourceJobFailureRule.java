package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.jobupdate.processing;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FAILED;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLE_FAILED_JOB_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.FAILED_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.FAILED_JOB_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForCNA;
import static org.greencloud.commons.utils.job.JobUtils.isJobUnique;
import static jade.lang.acl.ACLMessage.INFORM;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import com.gui.agents.server.ServerNode;

import jade.lang.acl.ACLMessage;

public class ProcessUpdateFromGreenSourceJobFailureRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessUpdateFromGreenSourceJobFailureRule.class);

	private ClientJob job;
	private JobInstanceIdentifier jobInstance;

	public ProcessUpdateFromGreenSourceJobFailureRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE, JOB_STATUS_RECEIVER_HANDLE_FAILED_JOB_RULE,
				"handles job failure update",
				"handling messages received from Green Source informing about job failure");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		job = facts.get(JOB);
		jobInstance = facts.get(MESSAGE_CONTENT);
		final ACLMessage message = facts.get(MESSAGE);
		return message.getPerformative() == INFORM && message.getProtocol().equals(FAILED_JOB_PROTOCOL);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final String jobId = job.getJobId();

		if (isJobUnique(jobId, agentProps.getServerJobs())) {
			agentProps.getGreenSourceForJobMap().remove(jobId);
		}
		agentProps.removeJob(job);

		if (agentProps.isDisabled() && agentProps.getServerJobs().size() == 0) {
			logger.info("Server completed all planned jobs and is fully disabled.");
			agentNode.disableServer();
		}

		agentProps.incrementJobCounter(jobInstance, FAILED);
		agentProps.updateGUI();

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Job {} execution has failed in green source", jobInstance.getJobId());
		agent.send(prepareJobStatusMessageForCNA(jobInstance, FAILED_JOB_ID, agentProps, facts.get(RULE_SET_IDX)));
	}
}


