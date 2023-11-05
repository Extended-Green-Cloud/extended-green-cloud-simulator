package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.transfer;

import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_ID;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FINISH;
import static org.greencloud.commons.enums.job.JobExecutionStateEnum.EXECUTING_ON_HOLD;
import static org.greencloud.commons.enums.rules.RuleType.TRANSFER_JOB_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getJobByInstanceId;
import static org.greencloud.commons.utils.job.JobUtils.isJobStarted;
import static org.greencloud.commons.utils.job.JobUtils.isJobUnique;
import static org.greencloud.commons.utils.messaging.constants.MessageContentConstants.JOB_NOT_FOUND_CAUSE_MESSAGE;
import static org.greencloud.commons.utils.messaging.constants.MessageContentConstants.NO_SERVER_AVAILABLE_CAUSE_MESSAGE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.ON_HOLD_JOB_ID;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.INTERNAL_SERVER_ERROR_ON_HOLD_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobFinishMessage;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForCNA;
import static org.greencloud.commons.utils.messaging.factory.NetworkErrorMessageFactory.prepareJobTransferRequest;
import static org.greencloud.commons.utils.messaging.factory.NetworkErrorMessageFactory.prepareNetworkFailureInformation;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.greencloud.commons.mapper.JobMapper;
import org.greencloud.gui.agents.server.ServerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentRequestRule;
import org.jeasy.rules.api.Facts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class TransferInCloudNetworkRule extends AgentRequestRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(TransferInCloudNetworkRule.class);

	public TransferInCloudNetworkRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(TRANSFER_JOB_RULE,
				"transfer job to another server",
				"rule performs transfer request of job to another Server via CNA for internal Server error");
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

			informGreenSourceUponJobFinish(job, facts);
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
				logger.info("Cloud Network {} refused to work on job {} transfer. The job was not found.",
						refuse.getSender().getLocalName(), job.getJobInstanceId());
				informGreenSourceUponJobFinish(job, facts);
				updateServerStateUponJobFinish(job);
			} else if (cause.equals(NO_SERVER_AVAILABLE_CAUSE_MESSAGE)) {
				informGreenSourceUponJobOnHold(job.getJobId(), jobToTransfer, facts);
				updateServerStateUponJobOnHold(job, facts);
			}
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

			informGreenSourceUponJobOnHold(job.getJobId(), jobToTransfer, facts);
			updateServerStateUponJobOnHold(job, facts);
		}
	}

	private void informGreenSourceUponJobFinish(final ClientJob job, final Facts facts) {
		final AID receiver = agentProps.getGreenSourceForJobMap().get(job.getJobId());
		agent.send(prepareJobFinishMessage(job, facts.get(RULE_SET_IDX), receiver));
	}

	private void updateServerStateUponJobFinish(final ClientJob job) {
		if (isJobStarted(job, agentProps.getServerJobs())) {
			agentProps.incrementJobCounter(JobMapper.mapClientJobToJobInstanceId(job), FINISH);
		}
		if (isJobUnique(job.getJobId(), agentProps.getServerJobs())) {
			agentProps.getGreenSourceForJobMap().remove(job.getJobId());
		}
		agentProps.getJobsExecutionTime()
				.stopJobExecutionTimer(job, agentProps.getServerJobs().get(job), getCurrentTime());
		agentProps.updateJobExecutionCost(job);
		agentProps.removeJob(job);

		if (agentProps.isDisabled() && agentProps.getServerJobs().size() == 0) {
			logger.info("Server completed all planned jobs and is fully disabled.");
			agentNode.disableServer();
		}

		agentProps.updateGUI();
	}

	private void informGreenSourceUponJobOnHold(final String jobId, final JobInstanceIdentifier jobToTransfer,
			final Facts facts) {
		final AID receiver = agentProps.getGreenSourceForJobMap().get(jobId);
		agent.send(prepareNetworkFailureInformation(jobToTransfer, INTERNAL_SERVER_ERROR_ON_HOLD_PROTOCOL,
				facts.get(RULE_SET_IDX), receiver));
	}

	private void updateServerStateUponJobOnHold(final ClientJob job, final Facts facts) {
		final boolean hasStarted = isJobStarted(job, agentProps.getServerJobs());

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Transfer of job with id {} was unsuccessful! Putting job on hold", job.getJobId());
		final JobExecutionStatusEnum prevStatus = agentProps.getServerJobs().get(job);
		final JobExecutionStatusEnum newStatus = EXECUTING_ON_HOLD.getStatus(hasStarted);

		agentProps.getJobsExecutionTime().updateJobExecutionDuration(job, prevStatus, newStatus, getCurrentTime());
		agentProps.getServerJobs().replace(job, newStatus);

		if (hasStarted) {
			agent.send(prepareJobStatusMessageForCNA(JobMapper.mapClientJobToJobInstanceId(job), ON_HOLD_JOB_ID,
					agentProps,
					facts.get(RULE_SET_IDX)));
		}
		agentProps.updateGUI();
	}
}
