package com.greencloud.application.agents.server.behaviour.errorhandling.initiator;

import static com.greencloud.application.agents.server.behaviour.errorhandling.initiator.logs.ErrorHandlingInitiatorLog.GS_TRANSFER_CHOSEN_GS_LOG;
import static com.greencloud.application.agents.server.behaviour.errorhandling.initiator.logs.ErrorHandlingInitiatorLog.GS_TRANSFER_FAIL_BACK_UP_LOG;
import static com.greencloud.application.agents.server.behaviour.errorhandling.initiator.logs.ErrorHandlingInitiatorLog.GS_TRANSFER_FAIL_NO_BACK_UP_LOG;
import static com.greencloud.application.agents.server.behaviour.errorhandling.initiator.logs.ErrorHandlingInitiatorLog.GS_TRANSFER_NONE_AVAILABLE_LOG;
import static com.greencloud.application.agents.server.behaviour.errorhandling.initiator.logs.ErrorHandlingInitiatorLog.GS_TRANSFER_NO_RESPONSE_RETRIEVED_LOG;
import static com.greencloud.application.mapper.JobMapper.mapPowerJobToEnergyJob;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.messages.constants.MessageContentConstants.NO_SOURCES_AVAILABLE_CAUSE_MESSAGE;
import static com.greencloud.application.messages.constants.MessageConversationConstants.BACK_UP_POWER_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.ON_HOLD_JOB_ID;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.POWER_SHORTAGE_JOB_CONFIRMATION_PROTOCOL;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL;
import static com.greencloud.application.messages.factory.CallForProposalMessageFactory.prepareCallForProposal;
import static com.greencloud.application.messages.factory.ReplyMessageFactory.prepareAcceptJobOfferReply;
import static com.greencloud.application.messages.factory.ReplyMessageFactory.prepareStringReply;
import static com.greencloud.application.utils.JobUtils.getJobByInstanceId;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.domain.job.enums.JobExecutionStateEnum.EXECUTING_ON_BACK_UP;
import static com.greencloud.commons.domain.job.enums.JobExecutionStateEnum.EXECUTING_ON_HOLD_SOURCE;
import static jade.lang.acl.ACLMessage.REFUSE;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.agents.server.behaviour.errorhandling.listener.ListenForSourceJobTransferConfirmation;
import com.greencloud.application.behaviours.initiator.AbstractCFPInitiator;
import com.greencloud.application.domain.agent.GreenSourceData;
import com.greencloud.application.domain.job.JobDivided;
import com.greencloud.commons.domain.job.ClientJob;
import com.greencloud.commons.domain.job.enums.JobExecutionStateEnum;
import com.greencloud.commons.domain.resources.HardwareResources;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Behaviours sends the CFP to remaining green sources looking for job transfer and selects the one which will
 * handle the remaining job execution
 */
public class InitiateJobTransferInGreenSources extends AbstractCFPInitiator<GreenSourceData> {

	private static final Logger logger = getLogger(InitiateJobTransferInGreenSources.class);

	private final ServerAgent myServerAgent;
	private final JobDivided<ClientJob> newJobInstances;
	private final Instant powerShortageStart;

	public InitiateJobTransferInGreenSources(final ServerAgent agent, final ACLMessage cfp,
			final ACLMessage originalMessage, final JobDivided<ClientJob> newJobInstances,
			final Instant powerShortageStart) {
		super(agent, cfp, originalMessage, mapToJobInstanceId(newJobInstances.getSecondInstance()),
				agent.manage().offerComparator(), GreenSourceData.class);

		this.myServerAgent = agent;
		this.newJobInstances = newJobInstances;
		this.powerShortageStart = powerShortageStart;
	}

	/**
	 * Method creates behaviour
	 *
	 * @param agent              agent executing the behaviour
	 * @param newJobInstances    pair of job instances including previous job instance (first) and job to transfer
	 *                           instance (second)
	 * @param greenSources       list of green sources to which CFP is sent
	 * @param greenSourceRequest original green source request
	 * @param shortageTime       time when power shortage is to start
	 * @return InitiateJobTransferInGreenSources
	 */
	public static InitiateJobTransferInGreenSources create(final ServerAgent agent,
			final JobDivided<ClientJob> newJobInstances, final List<AID> greenSources,
			final ACLMessage greenSourceRequest, final Instant shortageTime) {
		final ClientJob job = newJobInstances.getSecondInstance();
		final double estimatedEnergy = agent.resources().estimateEnergyForJob(job);
		final ACLMessage cfp = prepareCallForProposal(mapPowerJobToEnergyJob(job, estimatedEnergy),
				greenSources, SERVER_JOB_CFP_PROTOCOL);

		return new InitiateJobTransferInGreenSources(agent, cfp, greenSourceRequest, newJobInstances, shortageTime);
	}

	@Override
	protected void handleNoResponses() {
		logger.info(GS_TRANSFER_NO_RESPONSE_RETRIEVED_LOG);
		handleTransferFailure();
	}

	@Override
	protected void handleNoAvailableAgents() {
		logger.info(GS_TRANSFER_NONE_AVAILABLE_LOG, jobInstance.getJobId());
		myServerAgent.message().passTransferRequestToCloudNetwork(jobInstance, powerShortageStart, originalMessage);
	}

	@Override
	protected void handleSelectedOffer(final GreenSourceData chosenOfferData) {
		MDC.put(MDC_JOB_ID, jobInstance.getJobId());
		logger.info(GS_TRANSFER_CHOSEN_GS_LOG, jobInstance.getJobId(), bestProposal.getSender().getLocalName());

		myServerAgent.addBehaviour(new ListenForSourceJobTransferConfirmation(myServerAgent, newJobInstances,
				powerShortageStart, originalMessage));
		myAgent.send(prepareAcceptJobOfferReply(bestProposal, jobInstance, POWER_SHORTAGE_JOB_CONFIRMATION_PROTOCOL));
	}

	private void handleTransferFailure() {
		MDC.put(MDC_JOB_ID, newJobInstances.getSecondInstance().getJobId());
		final ClientJob job = getJobByInstanceId(jobInstance.getJobInstanceId(), myServerAgent.getServerJobs());

		if (nonNull(job)) {
			myServerAgent.manage().handleJobStateChange(getFieldsForJobState(job), job);
			myServerAgent.send(prepareStringReply(originalMessage, NO_SOURCES_AVAILABLE_CAUSE_MESSAGE, REFUSE));
		}
	}

	private Triple<JobExecutionStateEnum, String, String> getFieldsForJobState(final ClientJob job) {
		final HardwareResources availableResources = myServerAgent.resources()
				.getAvailableResources(newJobInstances.getSecondInstance(), jobInstance, null);

		return !availableResources.areSufficient(job.getEstimatedResources()) ?
				new ImmutableTriple<>(EXECUTING_ON_HOLD_SOURCE, GS_TRANSFER_FAIL_NO_BACK_UP_LOG, ON_HOLD_JOB_ID) :
				new ImmutableTriple<>(EXECUTING_ON_BACK_UP, GS_TRANSFER_FAIL_BACK_UP_LOG, BACK_UP_POWER_JOB_ID);
	}
}
