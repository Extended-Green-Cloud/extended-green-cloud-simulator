package com.greencloud.application.agents.greenenergy.behaviour.powersupply.initiator;

import static com.greencloud.application.agents.greenenergy.behaviour.powersupply.initiator.logs.PowerSupplyInitiatorLog.POWER_SUPPLY_PROPOSAL_REJECTED_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powersupply.initiator.logs.PowerSupplyInitiatorLog.SEND_POWER_SUPPLY_FAILURE_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powersupply.initiator.logs.PowerSupplyInitiatorLog.SEND_POWER_SUPPLY_RESPONSE_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.messages.MessagingUtils.readMessageContent;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.FAILED_JOB_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.FAILED_SOURCE_TRANSFER_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.FAILED_TRANSFER_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.POWER_SHORTAGE_JOB_CONFIRMATION_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareFailureReply;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareReply;
import static com.greencloud.application.utils.JobUtils.calculateExpectedJobEndTime;
import static com.greencloud.application.utils.JobUtils.getJobById;
import static com.greencloud.application.utils.JobUtils.getJobByIdAndStartDateAndServer;
import static jade.lang.acl.ACLMessage.INFORM;
import static java.util.Objects.isNull;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.greenenergy.behaviour.powersupply.handler.HandleManualPowerSupplyFinish;
import com.greencloud.application.domain.MonitoringData;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.JobWithProtocol;
import com.greencloud.commons.job.ExecutionJobStatusEnum;
import com.greencloud.commons.job.JobResultType;
import com.greencloud.commons.job.ServerJob;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.proto.ProposeInitiator;

/**
 * Behaviour sends the proposal with power supply offer to Server Agent and handles the retrieved responses.
 */
public class InitiatePowerSupplyOffer extends ProposeInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiatePowerSupplyOffer.class);
	private final GreenEnergyAgent myGreenEnergyAgent;
	private final MonitoringData weather;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent   agent which is executing the behaviour
	 * @param msg     proposal message that is sent to the Server Agent
	 * @param weather weather at the time when new job is to be executed
	 */
	public InitiatePowerSupplyOffer(final Agent agent, final ACLMessage msg, final MonitoringData weather) {
		super(agent, msg);
		this.myGreenEnergyAgent = (GreenEnergyAgent) agent;
		this.weather = weather;
	}

	/**
	 * Method handles ACCEPT_PROPOSAL response from server.
	 * It updates the state of the job in green source and replies with the message with correct protocol and the information
	 * that the execution of the given job can be started.
	 *
	 * @param acceptProposal accept proposal response retrieved from the Server Agent
	 */
	@Override
	protected void handleAcceptProposal(final ACLMessage acceptProposal) {
		final JobWithProtocol jobWithProtocol = readMessageContent(acceptProposal, JobWithProtocol.class);
		final ServerJob job = findCorrespondingJob(jobWithProtocol.getJobInstanceIdentifier(),
				acceptProposal.getSender());

		if (Objects.nonNull(job)) {
			handleAcceptPowerSupply(job, acceptProposal, jobWithProtocol);
		}
	}

	/**
	 * Method handles REJECT_PROPOSAL response from server.
	 *
	 * @param rejectProposal reject proposal response retrieved from the Server Agent
	 */
	@Override
	protected void handleRejectProposal(final ACLMessage rejectProposal) {
		final JobInstanceIdentifier jobInstanceId = readMessageContent(rejectProposal, JobInstanceIdentifier.class);
		final ServerJob serverJob = getJobByIdAndStartDateAndServer(jobInstanceId, rejectProposal.getSender(),
				myGreenEnergyAgent.getServerJobs());
		if (Objects.nonNull(serverJob)) {
			myGreenEnergyAgent.getServerJobs().remove(serverJob);
		}
		MDC.put(MDC_JOB_ID, jobInstanceId.getJobId());
		logger.info(POWER_SUPPLY_PROPOSAL_REJECTED_LOG);
	}

	private ServerJob findCorrespondingJob(final JobInstanceIdentifier jobInstance, final AID server) {
		ServerJob job = getJobByIdAndStartDateAndServer(jobInstance, server, myGreenEnergyAgent.getServerJobs());
		if (isNull(job)) {
			job = getJobById(jobInstance.getJobId(), myGreenEnergyAgent.getServerJobs());
		}
		return job;
	}

	private void handleAcceptPowerSupply(final ServerJob job, final ACLMessage acceptProposal,
			final JobWithProtocol jobWithProtocol) {
		final Optional<Double> averageAvailablePower = myGreenEnergyAgent.manage()
				.getAvailablePowerForJob(job, weather, true);
		myGreenEnergyAgent.manage().incrementJobCounter(mapToJobInstanceId(job), JobResultType.ACCEPTED);

		if (averageAvailablePower.isEmpty() || job.getPower() > averageAvailablePower.get()) {
			sendPowerFailureInformation(job, jobWithProtocol, acceptProposal);
		} else {
			sendPowerConfirmationMessage(job, jobWithProtocol, acceptProposal);
		}
	}

	private void sendPowerConfirmationMessage(final ServerJob job, final JobWithProtocol jobWithProtocol,
			final ACLMessage proposal) {
		MDC.put(MDC_JOB_ID, job.getJobId());
		logger.info(SEND_POWER_SUPPLY_RESPONSE_LOG, job.getJobId());
		myGreenEnergyAgent.getServerJobs().replace(job, ExecutionJobStatusEnum.ACCEPTED);

		final Behaviour manualFinishBehaviour = new HandleManualPowerSupplyFinish(myGreenEnergyAgent,
				calculateExpectedJobEndTime(job), job);
		myAgent.addBehaviour(manualFinishBehaviour);

		sendResponseToServer(proposal, jobWithProtocol);
	}

	private void sendPowerFailureInformation(final ServerJob job, final JobWithProtocol jobWithProtocol,
			final ACLMessage proposal) {
		logger.info(SEND_POWER_SUPPLY_FAILURE_LOG, job.getJobId());
		myGreenEnergyAgent.getServerJobs().remove(job);

		final String responseProtocol =
				jobWithProtocol.getReplyProtocol().equals(POWER_SHORTAGE_JOB_CONFIRMATION_PROTOCOL) ?
						FAILED_SOURCE_TRANSFER_PROTOCOL :
						jobWithProtocol.getReplyProtocol().equals(POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL) ?
								FAILED_TRANSFER_PROTOCOL :
								FAILED_JOB_PROTOCOL;
		myGreenEnergyAgent.manage()
				.incrementJobCounter(jobWithProtocol.getJobInstanceIdentifier(), JobResultType.FAILED);
		myGreenEnergyAgent.getServerJobs().remove(job);

		final ACLMessage failureMessage = prepareFailureReply(proposal.createReply(),
				jobWithProtocol.getJobInstanceIdentifier(), responseProtocol);

		myGreenEnergyAgent.send(failureMessage);
	}

	private void sendResponseToServer(final ACLMessage acceptProposal, final JobWithProtocol jobWithProtocol) {
		final ACLMessage response = prepareReply(acceptProposal.createReply(),
				jobWithProtocol.getJobInstanceIdentifier(), INFORM);
		response.setProtocol(jobWithProtocol.getReplyProtocol());
		myAgent.send(response);
	}
}
