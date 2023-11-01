package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.jobupdate.processing;

import static jade.lang.acl.ACLMessage.INFORM;
import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_FINISH_INFORM;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_START_INFORM;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED_JOB_STATUSES;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_HANDLE_CONFIRM_RULE;
import static org.greencloud.commons.enums.rules.RuleType.START_JOB_EXECUTION_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getJobCount;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.CONFIRMED_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForCNA;
import static org.greencloud.rulescontroller.ruleset.RuleSetSelector.SELECT_BY_FACTS_IDX;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.gui.agents.server.ServerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.schedule.ScheduleOnce;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class ProcessUpdateFromGreenSourceJobConfirmationRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessUpdateFromGreenSourceJobConfirmationRule.class);

	private ClientJob job;
	private JobInstanceIdentifier jobInstance;

	public ProcessUpdateFromGreenSourceJobConfirmationRule(
			final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller, 4);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_RECEIVER_HANDLER_RULE, JOB_STATUS_RECEIVER_HANDLE_CONFIRM_RULE,
				"handles job confirmation update",
				"handling messages received from Green Source informing about job confirmation");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		job = facts.get(JOB);
		jobInstance = facts.get(MESSAGE_CONTENT);
		final ACLMessage message = facts.get(MESSAGE);
		return message.getPerformative() == INFORM && message.getProtocol().equals(SERVER_JOB_CFP_PROTOCOL);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Scheduling the execution of the job {}", jobInstance.getJobId());

		agentProps.getServerJobs().replace(job, ACCEPTED);
		agentNode.updateClientNumber(getJobCount(agentProps.getServerJobs(), ACCEPTED_JOB_STATUSES));

		logger.info("Announcing new job {} in network!", jobInstance.getJobId());
		agentNode.announceClientJob();

		agent.send(prepareJobStatusMessageForCNA(jobInstance, CONFIRMED_JOB_ID, agentProps, facts.get(RULE_SET_IDX)));

		facts.put(JOB_START_INFORM, true);
		facts.put(JOB_FINISH_INFORM, true);

		agent.addBehaviour(
				ScheduleOnce.create(agent, facts, START_JOB_EXECUTION_RULE, controller, SELECT_BY_FACTS_IDX));
	}
}

