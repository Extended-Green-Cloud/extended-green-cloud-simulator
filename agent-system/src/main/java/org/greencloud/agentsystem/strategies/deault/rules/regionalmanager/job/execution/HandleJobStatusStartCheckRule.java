package org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.job.execution;

import static jade.lang.acl.ACLMessage.REQUEST;
import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB_ID;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB_TIME;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FAILED;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.STARTED;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.IN_PROGRESS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.HANDLE_JOB_STATUS_CHECK_RULE;
import static org.greencloud.commons.mapper.JobStatusMapper.mapToJobWithStatusForCurrentTime;
import static org.greencloud.commons.utils.facts.FactsFactory.constructFactsForJobRemoval;
import static org.greencloud.commons.utils.job.JobUtils.getJobById;
import static org.greencloud.commons.utils.job.JobUtils.updateJobStartAndExecutionTime;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.DELAYED_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.FAILED_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.STARTED_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.JOB_START_STATUS_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForCMA;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.jrba.utils.messages.MessageReader.readMessageContent;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.Date;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.ImmutableJobWithStatus;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentRequestRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.jrba.utils.messages.MessageBuilder;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class HandleJobStatusStartCheckRule extends AgentRequestRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(HandleJobStatusStartCheckRule.class);

	public HandleJobStatusStartCheckRule(final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(HANDLE_JOB_STATUS_CHECK_RULE,
				"verifies with Server if job execution has started",
				"communicate with Server to verify if job execution has started");
	}

	@Override
	protected ACLMessage createRequestMessage(final RuleSetFacts facts) {
		final AID server = agentProps.getServerForJobMap().get((String) facts.get(JOB_ID));
		return MessageBuilder.builder((int) facts.get(RULE_SET_IDX), REQUEST)
				.withStringContent(facts.get(JOB_ID))
				.withMessageProtocol(JOB_START_STATUS_PROTOCOL)
				.withReceivers(server)
				.build();
	}

	@Override
	protected boolean evaluateBeforeForAll(final RuleSetFacts facts) {
		final ClientJob job = getJobById(facts.get(JOB_ID), agentProps.getNetworkJobs());
		return nonNull(job) && !agentProps.getNetworkJobs().get(job).equals(IN_PROGRESS);
	}

	@Override
	protected void handleInform(final ACLMessage inform, final RuleSetFacts facts) {
		final String jobId = facts.get(JOB_ID);
		final ClientJob job = readMessageContent(inform, ClientJob.class);

		MDC.put(MDC_JOB_ID, jobId);
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Received job start confirmation. Sending information that job {} execution started.", jobId);

		final Instant jobStart = ((Date) facts.get(JOB_TIME)).toInstant();
		final JobWithStatus jobStatusUpdate = ImmutableJobWithStatus.builder()
				.jobId(jobId)
				.jobInstanceId(job.getJobInstanceId())
				.changeTime(jobStart)
				.serverName(inform.getSender().getLocalName())
				.build();
		updateJobStartAndExecutionTime(job, jobStart, job.getDuration(), agentProps.getNetworkJobs());
		agentProps.getNetworkJobs().replace(job, IN_PROGRESS);
		agentProps.incrementJobCounter(jobId, STARTED);
		agentNode.addStartedJob();
		agent.send(prepareJobStatusMessageForCMA(agentProps, jobStatusUpdate, STARTED_JOB_ID, facts.get(RULE_SET_IDX)));
	}

	@Override
	protected void handleRefuse(final ACLMessage refuse, final RuleSetFacts facts) {
		final String jobId = facts.get(JOB_ID);

		MDC.put(MDC_JOB_ID, jobId);
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.error("The job {} execution hasn't started yet. Sending delay information to client", jobId);

		final ClientJob job = requireNonNull(getJobById(jobId, agentProps.getNetworkJobs()));
		final JobWithStatus jobStatusUpdate = mapToJobWithStatusForCurrentTime(job);
		agent.send(prepareJobStatusMessageForCMA(agentProps, jobStatusUpdate, DELAYED_JOB_ID, facts.get(RULE_SET_IDX)));
	}

	@Override
	protected void handleFailure(final ACLMessage failure, final RuleSetFacts facts) {
		final String jobId = facts.get(JOB_ID);

		MDC.put(MDC_JOB_ID, jobId);
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.error("The job {} execution has failed in the meantime. Sending failure information to client", jobId);

		final ClientJob job = requireNonNull(getJobById(jobId, agentProps.getNetworkJobs()));
		final JobWithStatus jobStatusUpdate = mapToJobWithStatusForCurrentTime(job);
		agent.send(prepareJobStatusMessageForCMA(agentProps, jobStatusUpdate, FAILED_JOB_ID, facts.get(RULE_SET_IDX)));

		if (agentProps.getNetworkJobs().get(job).equals(ACCEPTED)) {
			agentNode.removePlannedJob();
		}
		controller.fire(constructFactsForJobRemoval(facts.get(RULE_SET_IDX), facts.get(JOB)));
		agentProps.getServerForJobMap().remove(jobId);
		agentProps.incrementJobCounter(jobId, FAILED);
	}

	@Override
	public AgentRule copy() {
		return new HandleJobStatusStartCheckRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
