package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.job.execution;

import static jade.lang.acl.ACLMessage.REQUEST;
import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_ID;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_TIME;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FAILED;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.STARTED;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.IN_PROGRESS;
import static org.greencloud.commons.enums.rules.RuleType.FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.enums.rules.RuleType.HANDLE_JOB_STATUS_CHECK_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapClientJobToJobInstanceId;
import static org.greencloud.commons.utils.job.JobUtils.getJobById;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.DELAYED_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.FAILED_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.STARTED_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.JOB_START_STATUS_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForScheduler;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.Date;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.extended.ImmutableJobWithStatus;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.commons.utils.messaging.MessageBuilder;
import org.greencloud.gui.agents.cloudnetwork.CloudNetworkNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentRequestRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class HandleJobStatusStartCheckRule extends AgentRequestRule<CloudNetworkAgentProps, CloudNetworkNode> {

	private static final Logger logger = getLogger(HandleJobStatusStartCheckRule.class);

	public HandleJobStatusStartCheckRule(final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller) {
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
		return MessageBuilder.builder((int) facts.get(RULE_SET_IDX))
				.withPerformative(REQUEST)
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
		MDC.put(MDC_JOB_ID, facts.get(JOB_ID));
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Received job started confirmation. Sending information that the job {} execution has started",
				(String) facts.get(JOB_ID));

		final ClientJob job = requireNonNull(getJobById(facts.get(JOB_ID), agentProps.getNetworkJobs()));
		final Instant jobStart = ((Date) facts.get(JOB_TIME)).toInstant();
		final JobWithStatus jobStatusUpdate = ImmutableJobWithStatus.builder()
				.jobInstance(mapClientJobToJobInstanceId(job))
				.changeTime(jobStart)
				.serverName(inform.getSender().getLocalName())
				.build();
		agentProps.getNetworkJobs().replace(job, IN_PROGRESS);
		agentProps.incrementJobCounter(mapClientJobToJobInstanceId(job), STARTED);
		agentNode.addStartedJob();
		agent.send(prepareJobStatusMessageForScheduler(agentProps, jobStatusUpdate, STARTED_JOB_ID,
				facts.get(RULE_SET_IDX)));
	}

	@Override
	protected void handleRefuse(final ACLMessage refuse, final RuleSetFacts facts) {
		MDC.put(MDC_JOB_ID, facts.get(JOB_ID));
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.error("The job {} execution hasn't started yet. Sending delay information to client",
				(String) facts.get(JOB_ID));

		final ClientJob job = requireNonNull(getJobById(facts.get(JOB_ID), agentProps.getNetworkJobs()));
		final JobWithStatus jobStatusUpdate = ImmutableJobWithStatus.builder()
				.jobInstance(mapClientJobToJobInstanceId(job))
				.changeTime(getCurrentTime())
				.build();
		agent.send(prepareJobStatusMessageForScheduler(agentProps, jobStatusUpdate, DELAYED_JOB_ID,
				facts.get(RULE_SET_IDX)));
	}

	@Override
	protected void handleFailure(final ACLMessage failure, final RuleSetFacts facts) {
		MDC.put(MDC_JOB_ID, facts.get(JOB_ID));
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.error("The job {} execution has failed in the meantime. Sending failure information to client",
				(String) facts.get(JOB_ID));

		final ClientJob job = requireNonNull(getJobById(facts.get(JOB_ID), agentProps.getNetworkJobs()));
		final JobWithStatus jobStatusUpdate = ImmutableJobWithStatus.builder()
				.jobInstance(mapClientJobToJobInstanceId(job))
				.changeTime(getCurrentTime())
				.build();
		agent.send(prepareJobStatusMessageForScheduler(agentProps, jobStatusUpdate, FAILED_JOB_ID,
				facts.get(RULE_SET_IDX)));

		if (agentProps.getNetworkJobs().get(job).equals(ACCEPTED)) {
			agentNode.removePlannedJob();
		}
		final RuleSetFacts jobRemovalFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
		jobRemovalFacts.put(RULE_TYPE, FINISH_JOB_EXECUTION_RULE);
		jobRemovalFacts.put(JOB, facts.get(JOB));
		controller.fire(jobRemovalFacts);

		agentProps.getServerForJobMap().remove(job.getJobId());
		agentProps.incrementJobCounter(mapClientJobToJobInstanceId(job), FAILED);
	}
}
