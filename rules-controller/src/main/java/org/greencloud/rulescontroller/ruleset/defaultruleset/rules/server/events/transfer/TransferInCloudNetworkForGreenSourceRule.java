package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.transfer;

import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REFUSE;
import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_ID;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FINISH;
import static org.greencloud.commons.enums.job.JobExecutionStateEnum.EXECUTING_ON_BACK_UP;
import static org.greencloud.commons.enums.job.JobExecutionStateEnum.EXECUTING_ON_HOLD_SOURCE;
import static org.greencloud.commons.enums.rules.RuleType.TRANSFER_JOB_FOR_GS_IN_CNA_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getJobByInstanceId;
import static org.greencloud.commons.utils.job.JobUtils.isJobStarted;
import static org.greencloud.commons.utils.job.JobUtils.isJobUnique;
import static org.greencloud.commons.utils.messaging.constants.MessageContentConstants.JOB_NOT_FOUND_CAUSE_MESSAGE;
import static org.greencloud.commons.utils.messaging.constants.MessageContentConstants.NO_SERVER_AVAILABLE_CAUSE_MESSAGE;
import static org.greencloud.commons.utils.messaging.constants.MessageContentConstants.TRANSFER_SUCCESSFUL_MESSAGE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.BACK_UP_POWER_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.ON_HOLD_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.INTERNAL_SERVER_ERROR_ON_HOLD_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobFinishMessage;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForCNA;
import static org.greencloud.commons.utils.messaging.factory.NetworkErrorMessageFactory.prepareJobTransferRequest;
import static org.greencloud.commons.utils.messaging.factory.NetworkErrorMessageFactory.prepareNetworkFailureInformation;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareStringReply;
import static org.greencloud.commons.utils.resources.ResourcesUtilization.areSufficient;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.commons.enums.job.JobExecutionStateEnum;
import org.greencloud.commons.mapper.JobMapper;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentRequestRule;
import org.jeasy.rules.api.Facts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import com.gui.agents.server.ServerNode;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class TransferInCloudNetworkForGreenSourceRule extends AgentRequestRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(TransferInCloudNetworkForGreenSourceRule.class);

	public TransferInCloudNetworkForGreenSourceRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(TRANSFER_JOB_FOR_GS_IN_CNA_RULE,
				"transfer job to another Server based on Green Source request",
				"rule performs transfer request of job to another Server via CNA for power shortage in Green Source");
	}

	@Override
	protected ACLMessage createRequestMessage(final RuleSetFacts facts) {
		final AID cloudNetwork = agentProps.getOwnerCloudNetworkAgent();
		return prepareJobTransferRequest(facts.get(JOB), cloudNetwork, facts.get(RULE_SET_IDX));
	}

	@Override
	protected void handleInform(final ACLMessage inform, final RuleSetFacts facts) {
		final JobInstanceIdentifier jobToTransfer = facts.get(JOB_ID);
		final ClientJob job = getJobByInstanceId(jobToTransfer.getJobInstanceId(), agentProps.getServerJobs());

		if (nonNull(job)) {
			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
			logger.info(
					"Transfer of job with id {} was established successfully. Finishing the job and informing the green source",
					job.getJobInstanceId());

			final AID receiver = agentProps.getGreenSourceForJobMap().get(job.getJobId());
			agent.send(prepareJobFinishMessage(job, facts.get(RULE_SET_IDX), receiver));
			agent.send(prepareStringReply(facts.get(MESSAGE), TRANSFER_SUCCESSFUL_MESSAGE, INFORM));

			updateServerStateUponJobFinish(job);
		}
	}

	@Override
	protected void handleRefuse(final ACLMessage refuse, final RuleSetFacts facts) {
		final String cause = refuse.getContent();
		final JobInstanceIdentifier jobToTransfer = facts.get(JOB_ID);
		final ClientJob job = getJobByInstanceId(jobToTransfer.getJobInstanceId(), agentProps.getServerJobs());

		if (nonNull(job)) {
			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));

			if (cause.equals(JOB_NOT_FOUND_CAUSE_MESSAGE)) {
				MDC.put(MDC_JOB_ID, job.getJobId());
				MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
				logger.info("Cloud Network {} refused to work on job {} transfer. The job was not found.",
						refuse.getSender().getLocalName(), job.getJobInstanceId());

				final AID receiver = agentProps.getGreenSourceForJobMap().get(job.getJobId());
				agent.send(prepareJobFinishMessage(job, facts.get(RULE_SET_IDX), receiver));
				agent.send(prepareStringReply(facts.get(MESSAGE), cause, REFUSE));

			} else if (cause.equals(NO_SERVER_AVAILABLE_CAUSE_MESSAGE)) {

				final AID receiver = agentProps.getGreenSourceForJobMap().get(job.getJobId());
				agent.send(prepareNetworkFailureInformation(jobToTransfer, INTERNAL_SERVER_ERROR_ON_HOLD_PROTOCOL,
						facts.get(RULE_SET_IDX),
						receiver));
				agent.send(prepareStringReply(facts.get(MESSAGE), cause, REFUSE));
			}
			updateServerStateUponJobOnHold(job, jobToTransfer, facts);
		}
	}

	@Override
	protected void handleFailure(final ACLMessage failure, final RuleSetFacts facts) {
		final JobInstanceIdentifier jobToTransfer = facts.get(JOB_ID);
		final ClientJob job = getJobByInstanceId(jobToTransfer.getJobInstanceId(), agentProps.getServerJobs());

		if (nonNull(job)) {
			MDC.put(MDC_JOB_ID, job.getJobId());
			MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
			logger.info(
					"Transfer of job with id {} has failed in Server that was selected for carrying out remaining job execution.",
					job.getJobInstanceId());

			final AID receiver = agentProps.getGreenSourceForJobMap().get(job.getJobId());
			agent.send(
					prepareNetworkFailureInformation(jobToTransfer, INTERNAL_SERVER_ERROR_ON_HOLD_PROTOCOL,
							facts.get(RULE_SET_IDX), receiver));
			agent.send(prepareStringReply(facts.get(MESSAGE), failure.getContent(), REFUSE));

			updateServerStateUponJobOnHold(job, jobToTransfer, facts);
		}
	}

	private void updateServerStateUponJobFinish(final ClientJob job) {
		if (isJobStarted(job, agentProps.getServerJobs())) {
			agentProps.incrementJobCounter(JobMapper.mapClientJobToJobInstanceId(job), FINISH);
		}
		if (isJobUnique(job.getJobId(), agentProps.getServerJobs())) {
			agentProps.getGreenSourceForJobMap().remove(job.getJobId());
		}
		agentProps.removeJob(job);
		agentProps.updateGUI();
	}

	private void updateServerStateUponJobOnHold(final ClientJob job, final JobInstanceIdentifier jobToTransfer, final
	Facts facts) {
		final Map<String, Resource> availableResources = agentProps.getAvailableResources(job, jobToTransfer, null);
		final boolean hasResources = areSufficient(availableResources, job.getRequiredResources());
		final boolean hasStarted = isJobStarted(job, agentProps.getServerJobs());

		final JobExecutionStateEnum state = hasResources ? EXECUTING_ON_BACK_UP : EXECUTING_ON_HOLD_SOURCE;
		final String message = hasResources
				? "Transfer of job with id {} was unsuccessful! Putting the job on back up power"
				: "Transfer of job with id {} was unsuccessful! There is not enough resources to process job "
				+ "with backup power. Putting job on hold";
		final String conversationId = hasResources ? BACK_UP_POWER_JOB_ID : ON_HOLD_JOB_ID;

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info(message, job.getJobId());
		agentProps.getServerJobs().replace(job, state.getStatus(hasStarted));

		if (hasStarted) {
			agent.send(prepareJobStatusMessageForCNA(JobMapper.mapClientJobToJobInstanceId(job), conversationId,
					agentProps,
					facts.get(RULE_SET_IDX)));
		}
		agentProps.updateGUI();
	}
}
