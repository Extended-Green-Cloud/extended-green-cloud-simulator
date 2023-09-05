package com.greencloud.application.agents.greenenergy.behaviour.powersupply.initiator;

import static com.greencloud.application.agents.greenenergy.behaviour.powersupply.initiator.logs.PowerSupplyInitiatorLog.POWER_SUPPLY_PROPOSAL_REJECTED_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powersupply.initiator.logs.PowerSupplyInitiatorLog.SEND_POWER_SUPPLY_FAILURE_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powersupply.initiator.logs.PowerSupplyInitiatorLog.SEND_POWER_SUPPLY_RESPONSE_LOG;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.FAILED_JOB_PROTOCOL;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.FAILED_SOURCE_TRANSFER_PROTOCOL;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.FAILED_TRANSFER_PROTOCOL;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.POWER_SHORTAGE_JOB_CONFIRMATION_PROTOCOL;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL;
import static com.greencloud.application.messages.factory.OfferMessageFactory.prepareGreenEnergyPowerSupplyOffer;
import static com.greencloud.application.messages.factory.ReplyMessageFactory.prepareFailureReply;
import static com.greencloud.application.messages.factory.ReplyMessageFactory.prepareReply;
import static com.greencloud.application.utils.JobUtils.getJobByInstanceIdAndServer;
import static com.greencloud.application.utils.MessagingUtils.readMessageContent;
import static com.greencloud.application.utils.TimeUtils.differenceInHours;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.domain.job.enums.JobExecutionResultEnum.ACCEPTED;
import static com.greencloud.commons.domain.job.enums.JobExecutionResultEnum.FAILED;
import static jade.lang.acl.ACLMessage.INFORM;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.greenenergy.behaviour.powersupply.handler.HandleManualPowerSupplyFinish;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.JobWithProtocol;
import com.greencloud.application.domain.weather.MonitoringData;
import com.greencloud.commons.domain.job.ServerJob;
import com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ProposeInitiator;

/**
 * Behaviour sends the proposal with power supply offer to Server Agent and handles the retrieved responses.
 */
public class InitiatePowerSupplyOffer extends ProposeInitiator {

	private static final Logger logger = getLogger(InitiatePowerSupplyOffer.class);
	private final GreenEnergyAgent myGreenEnergyAgent;
	private final MonitoringData weather;

	private InitiatePowerSupplyOffer(final Agent agent, final ACLMessage msg, final MonitoringData weather) {
		super(agent, msg);
		this.myGreenEnergyAgent = (GreenEnergyAgent) agent;
		this.weather = weather;
	}

	/**
	 * Method creates behaviour.
	 *
	 * @param agent          agent which is executing the behaviour
	 * @param job            job for which power supply is initiated
	 * @param availablePower power available in the given Green Source
	 * @param serverMessage  original server CFP
	 * @param data           weather conditions that were received for the time frames when job is to be executed
	 */
	public static InitiatePowerSupplyOffer create(final GreenEnergyAgent agent, final ServerJob job,
			final double availablePower, final ACLMessage serverMessage, final MonitoringData data) {
		final double energyCost = differenceInHours(job.getStartTime(), job.getEndTime())
				* agent.getPricePerPowerUnit();
		final ACLMessage offer = prepareGreenEnergyPowerSupplyOffer(energyCost, availablePower,
				agent.power().computeCombinedPowerError(job),
				job.getJobId(), serverMessage);

		return new InitiatePowerSupplyOffer(agent, offer, data);
	}

	/**
	 * Method handles ACCEPT_PROPOSAL response received from Server.
	 * It updates the state of the job in green source and replies with the message with
	 * correct protocol and the information whether the execution of the given job can be started.
	 *
	 * @param acceptProposal accept proposal response retrieved from the Server Agent
	 */
	@Override
	protected void handleAcceptProposal(final ACLMessage acceptProposal) {
		final JobWithProtocol jobWithProtocol = readMessageContent(acceptProposal, JobWithProtocol.class);
		final JobInstanceIdentifier jobInstance = jobWithProtocol.getJobInstanceIdentifier();
		final ServerJob job = getJobByInstanceIdAndServer(jobInstance.getJobInstanceId(), acceptProposal.getSender(),
				myGreenEnergyAgent.getServerJobs());

		if (nonNull(job)) {
			final Optional<Double> averageAvailablePower =
					myGreenEnergyAgent.power().getAvailableEnergy(job, weather, true);
			myGreenEnergyAgent.manage().incrementJobCounter(jobInstance, ACCEPTED);

			MDC.put(MDC_JOB_ID, job.getJobId());
			if (averageAvailablePower.isEmpty() || job.getEstimatedEnergy() > averageAvailablePower.get()) {
				sendPowerFailureMessage(job, jobWithProtocol, acceptProposal);
			} else {
				sendPowerConfirmationMessage(job, jobWithProtocol, acceptProposal);
			}
		}
	}

	/**
	 * Method handles REJECT_PROPOSAL response received from Server.
	 *
	 * @param rejectProposal reject proposal response retrieved from the Server Agent
	 */
	@Override
	protected void handleRejectProposal(final ACLMessage rejectProposal) {
		final JobInstanceIdentifier jobInstanceId = readMessageContent(rejectProposal, JobInstanceIdentifier.class);
		final ServerJob serverJob = getJobByInstanceIdAndServer(jobInstanceId.getJobInstanceId(),
				rejectProposal.getSender(), myGreenEnergyAgent.getServerJobs());

		if (nonNull(serverJob)) {
			myGreenEnergyAgent.manage().removeJob(serverJob);
		}

		MDC.put(MDC_JOB_ID, jobInstanceId.getJobId());
		logger.info(POWER_SUPPLY_PROPOSAL_REJECTED_LOG);
	}

	private void sendPowerConfirmationMessage(final ServerJob job, final JobWithProtocol jobWithProtocol,
			final ACLMessage proposal) {
		logger.info(SEND_POWER_SUPPLY_RESPONSE_LOG, job.getJobId());
		myGreenEnergyAgent.getServerJobs().replace(job, JobExecutionStatusEnum.ACCEPTED);

		myAgent.addBehaviour(HandleManualPowerSupplyFinish.create(myGreenEnergyAgent, job));
		myAgent.send(prepareReply(proposal, jobWithProtocol.getJobInstanceIdentifier(), INFORM,
				jobWithProtocol.getReplyProtocol()));
	}

	private void sendPowerFailureMessage(final ServerJob job, final JobWithProtocol jobWithProtocol,
			final ACLMessage proposal) {
		logger.info(SEND_POWER_SUPPLY_FAILURE_LOG, job.getJobId());
		myGreenEnergyAgent.manage().removeJob(job);

		final JobInstanceIdentifier jobInstanceId = jobWithProtocol.getJobInstanceIdentifier();
		final String responseProtocol = getResponseProtocol(jobWithProtocol.getReplyProtocol());

		myGreenEnergyAgent.manage().incrementJobCounter(jobInstanceId, FAILED);
		myGreenEnergyAgent.send(prepareFailureReply(proposal, jobInstanceId, responseProtocol));
	}

	private String getResponseProtocol(final String replyProtocol) {
		return switch (replyProtocol) {
			case POWER_SHORTAGE_JOB_CONFIRMATION_PROTOCOL -> FAILED_SOURCE_TRANSFER_PROTOCOL;
			case POWER_SHORTAGE_POWER_TRANSFER_PROTOCOL -> FAILED_TRANSFER_PROTOCOL;
			default -> FAILED_JOB_PROTOCOL;
		};
	}
}
