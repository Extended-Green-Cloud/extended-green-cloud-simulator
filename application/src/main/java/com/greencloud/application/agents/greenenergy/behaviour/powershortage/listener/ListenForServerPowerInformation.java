package com.greencloud.application.agents.greenenergy.behaviour.powershortage.listener;

import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.listener.logs.PowerShortageSourceListenerLog.SERVER_JOB_RE_SUPPLY_REQUEST_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.listener.logs.PowerShortageSourceListenerLog.SERVER_JOB_RE_SUPPLY_REQUEST_NOT_FOUND_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.listener.logs.PowerShortageSourceListenerLog.SERVER_POWER_SHORTAGE_FAILURE_NOT_FOUND_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.listener.logs.PowerShortageSourceListenerLog.SERVER_POWER_SHORTAGE_FAILURE_PUT_ON_HOLD_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.listener.logs.PowerShortageSourceListenerLog.SERVER_POWER_SHORTAGE_FINISH_CHANGE_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.listener.logs.PowerShortageSourceListenerLog.SERVER_POWER_SHORTAGE_FINISH_NOT_FOUND_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.listener.logs.PowerShortageSourceListenerLog.SERVER_POWER_SHORTAGE_START_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.listener.logs.PowerShortageSourceListenerLog.SERVER_POWER_SHORTAGE_START_NOT_FOUND_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.listener.templates.PowerShortageSourceMessageTemplates.SERVER_POWER_SHORTAGE_INFORMATION_TEMPLATE;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.messages.MessagingUtils.readMessageContent;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.POWER_SHORTAGE_FINISH_ALERT_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.SERVER_POWER_SHORTAGE_ALERT_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.SERVER_POWER_SHORTAGE_ON_HOLD_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.PowerShortageMessageContentConstants.JOB_NOT_FOUND_CAUSE_MESSAGE;
import static com.greencloud.application.messages.domain.constants.PowerShortageMessageContentConstants.PROCESSING_RE_SUPPLY_MESSAGE;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareStringReply;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static jade.lang.acl.ACLMessage.AGREE;
import static jade.lang.acl.ACLMessage.REFUSE;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener.ListenForWeatherData;
import com.greencloud.application.agents.greenenergy.behaviour.weathercheck.request.RequestWeatherData;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.JobStatusEnum;
import com.greencloud.application.domain.job.PowerJob;
import com.greencloud.application.domain.powershortage.PowerShortageJob;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour listens for the power information coming from the owner Server
 */
public class ListenForServerPowerInformation extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForServerPowerInformation.class);

	private final GreenEnergyAgent myGreenEnergyAgent;

	/**
	 * Behaviour constructor
	 *
	 * @param myAgent agent executing the behaviour
	 */
	public ListenForServerPowerInformation(final Agent myAgent) {
		super(myAgent);
		this.myGreenEnergyAgent = (GreenEnergyAgent) myAgent;
	}

	/**
	 * Method listens for the message coming from the Server passing the information regarding power
	 * supply changes:
	 *
	 * - information about power shortage start
	 * - informing about power shortage finish
	 * - informing about putting job on hold due to power shortage
	 */
	@Override
	public void action() {
		final ACLMessage msg = myAgent.receive(SERVER_POWER_SHORTAGE_INFORMATION_TEMPLATE);

		if (Objects.nonNull(msg)) {
			switch (msg.getProtocol()) {
				case SERVER_POWER_SHORTAGE_ALERT_PROTOCOL -> handleServerPowerShortageStart(msg);
				case POWER_SHORTAGE_FINISH_ALERT_PROTOCOL -> handleServerPowerShortageFinish(msg);
				case SERVER_POWER_SHORTAGE_ON_HOLD_PROTOCOL -> handleServerJobTransferFailure(msg);
				case SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL -> handleJobReSupplyingWithGreenEnergy(msg);
			}
		} else {
			block();
		}
	}

	private void handleServerPowerShortageStart(final ACLMessage inform) {
		final PowerShortageJob powerShortageJob = readMessageContent(inform, PowerShortageJob.class);
		MDC.put(MDC_JOB_ID, powerShortageJob.getJobInstanceId().getJobId());
		final PowerJob affectedJob = myGreenEnergyAgent.manage()
				.getJobByIdAndStartDate(powerShortageJob.getJobInstanceId());

		if (Objects.nonNull(affectedJob)) {
			logger.info(SERVER_POWER_SHORTAGE_START_LOG, powerShortageJob.getJobInstanceId().getJobId());
			myGreenEnergyAgent.manage()
					.dividePowerJobForPowerShortage(affectedJob, powerShortageJob.getPowerShortageStart());
		} else {
			logger.info(SERVER_POWER_SHORTAGE_START_NOT_FOUND_LOG, powerShortageJob.getJobInstanceId().getJobId());
		}
	}

	private void handleServerPowerShortageFinish(final ACLMessage inform) {
		final JobInstanceIdentifier jobInstanceId = readMessageContent(inform, JobInstanceIdentifier.class);
		final PowerJob powerJob = myGreenEnergyAgent.manage().getJobByIdAndStartDate(jobInstanceId);
		MDC.put(MDC_JOB_ID, jobInstanceId.getJobId());

		if (Objects.nonNull(powerJob)) {
			logger.info(SERVER_POWER_SHORTAGE_FINISH_CHANGE_LOG, jobInstanceId.getJobId());
			final JobStatusEnum newStatus = powerJob.getStartTime().isAfter(getCurrentTime()) ?
					JobStatusEnum.ACCEPTED :
					JobStatusEnum.IN_PROGRESS;
			myGreenEnergyAgent.getPowerJobs().replace(powerJob, newStatus);
			myGreenEnergyAgent.manage().updateGreenSourceGUI();
		} else {
			logger.info(SERVER_POWER_SHORTAGE_FINISH_NOT_FOUND_LOG, jobInstanceId.getJobId());
		}
	}

	private void handleServerJobTransferFailure(final ACLMessage inform) {
		final PowerShortageJob powerShortageJob = readMessageContent(inform, PowerShortageJob.class);
		final JobInstanceIdentifier jobInstanceId = powerShortageJob.getJobInstanceId();
		final PowerJob jobToPutOnHold = myGreenEnergyAgent.manage().getJobByIdAndStartDate(jobInstanceId);
		MDC.put(MDC_JOB_ID, jobInstanceId.getJobId());

		if (Objects.nonNull(jobToPutOnHold)) {
			final boolean hasStarted = !jobToPutOnHold.getStartTime().isAfter(getCurrentTime());
			logger.info(SERVER_POWER_SHORTAGE_FAILURE_PUT_ON_HOLD_LOG, jobInstanceId.getJobId());
			myGreenEnergyAgent.getPowerJobs()
					.replace(jobToPutOnHold, hasStarted ? JobStatusEnum.ON_HOLD : JobStatusEnum.ON_HOLD_PLANNED);
			myGreenEnergyAgent.manage().updateGreenSourceGUI();
		} else {
			logger.info(SERVER_POWER_SHORTAGE_FAILURE_NOT_FOUND_LOG, jobInstanceId.getJobId());
		}
	}

	private void handleJobReSupplyingWithGreenEnergy(final ACLMessage request) {
		final JobInstanceIdentifier jobInstanceId = readMessageContent(request, JobInstanceIdentifier.class);
		final PowerJob jobToCheck = myGreenEnergyAgent.manage().getJobByIdAndStartDate(jobInstanceId);

		MDC.put(MDC_JOB_ID, jobInstanceId.getJobId());
		if(Objects.nonNull(jobToCheck)) {
			logger.info(SERVER_JOB_RE_SUPPLY_REQUEST_LOG, jobInstanceId.getJobId());
			myGreenEnergyAgent.send(prepareStringReply(request.createReply(), PROCESSING_RE_SUPPLY_MESSAGE, AGREE));

			final SequentialBehaviour verificationBehaviour = new SequentialBehaviour();
			verificationBehaviour.addSubBehaviour(new RequestWeatherData(myGreenEnergyAgent,
					SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL,
					SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL,
					jobToCheck));
			verificationBehaviour.addSubBehaviour(new ListenForWeatherData(myGreenEnergyAgent,
					jobToCheck, SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL, SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL,
					verificationBehaviour, request.createReply()));
			myGreenEnergyAgent.addBehaviour(verificationBehaviour);
		} else {
			logger.info(SERVER_JOB_RE_SUPPLY_REQUEST_NOT_FOUND_LOG, jobInstanceId.getJobId());
			myGreenEnergyAgent.send(prepareStringReply(request.createReply(), JOB_NOT_FOUND_CAUSE_MESSAGE, REFUSE));
		}
	}
}
