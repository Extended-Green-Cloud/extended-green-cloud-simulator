package com.greencloud.application.agents.greenenergy.behaviour.powershortage.listener;

import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.listener.logs.PowerShortageSourceListenerLog.SERVER_POWER_SHORTAGE_FAILURE_NOT_FOUND_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.listener.logs.PowerShortageSourceListenerLog.SERVER_POWER_SHORTAGE_FAILURE_PUT_ON_HOLD_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.listener.logs.PowerShortageSourceListenerLog.SERVER_POWER_SHORTAGE_FINISH_CHANGE_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.listener.logs.PowerShortageSourceListenerLog.SERVER_POWER_SHORTAGE_FINISH_NOT_FOUND_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.listener.logs.PowerShortageSourceListenerLog.SERVER_POWER_SHORTAGE_START_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.listener.logs.PowerShortageSourceListenerLog.SERVER_POWER_SHORTAGE_START_NOT_FOUND_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.listener.templates.PowerShortageSourceMessageTemplates.SERVER_POWER_SHORTAGE_INFORMATION_TEMPLATE;
import static com.greencloud.application.agents.greenenergy.constants.GreenEnergyAgentConstants.MAX_NUMBER_OF_SERVER_MESSAGES;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.NETWORK_ERROR_FINISH_ALERT_PROTOCOL;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.INTERNAL_SERVER_ERROR_ALERT_PROTOCOL;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.INTERNAL_SERVER_ERROR_ON_HOLD_PROTOCOL;
import static com.greencloud.application.utils.JobUtils.getJobByInstanceIdAndServer;
import static com.greencloud.application.utils.JobUtils.isJobStarted;
import static com.greencloud.application.utils.MessagingUtils.readMessageContent;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.domain.job.enums.JobExecutionStateEnum.EXECUTING_ON_GREEN;
import static com.greencloud.commons.domain.job.enums.JobExecutionStateEnum.EXECUTING_ON_HOLD;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.JobPowerShortageTransfer;
import com.greencloud.commons.domain.job.ServerJob;
import com.greencloud.commons.domain.job.enums.JobExecutionStateEnum;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour listens for the power information coming from the owner Server
 */
public class ListenForServerPowerInformation extends CyclicBehaviour {

	private static final Logger logger = getLogger(ListenForServerPowerInformation.class);

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
	 * <p> - information about power shortage start </p>
	 * <p> - informing about power shortage finish </p>
	 * <p> - informing about putting job on hold due to power shortage </p>
	 */
	@Override
	public void action() {
		final List<ACLMessage> messages = myAgent.receive(SERVER_POWER_SHORTAGE_INFORMATION_TEMPLATE,
				MAX_NUMBER_OF_SERVER_MESSAGES);

		if (nonNull(messages)) {
			messages.forEach(msg -> {
				switch (msg.getProtocol()) {
					case INTERNAL_SERVER_ERROR_ALERT_PROTOCOL -> handleServerPowerShortageStart(msg);
					case NETWORK_ERROR_FINISH_ALERT_PROTOCOL -> handlePowerSupplyUpdate(msg,
							SERVER_POWER_SHORTAGE_FINISH_NOT_FOUND_LOG,
							SERVER_POWER_SHORTAGE_FINISH_CHANGE_LOG,
							EXECUTING_ON_GREEN);
					case INTERNAL_SERVER_ERROR_ON_HOLD_PROTOCOL -> handlePowerSupplyUpdate(msg,
							SERVER_POWER_SHORTAGE_FAILURE_NOT_FOUND_LOG,
							SERVER_POWER_SHORTAGE_FAILURE_PUT_ON_HOLD_LOG,
							EXECUTING_ON_HOLD);
				}
			});
		} else {
			block();
		}
	}

	private void handleServerPowerShortageStart(final ACLMessage inform) {
		final JobPowerShortageTransfer jobTransfer = readMessageContent(inform, JobPowerShortageTransfer.class);
		final String jobInstanceId = jobTransfer.getOriginalJobInstanceId();
		final String jobId = jobTransfer.getSecondJobInstanceId().getJobId();

		final ServerJob affectedJob = getJobByInstanceIdAndServer(jobInstanceId, inform.getSender(),
				myGreenEnergyAgent.getServerJobs());

		MDC.put(MDC_JOB_ID, jobId);
		if (nonNull(affectedJob)) {
			logger.info(SERVER_POWER_SHORTAGE_START_LOG, affectedJob.getJobId());
			myGreenEnergyAgent.manage().divideJobForPowerShortage(jobTransfer, affectedJob);
		} else {
			logger.info(SERVER_POWER_SHORTAGE_START_NOT_FOUND_LOG, jobId);
		}
	}

	private void handlePowerSupplyUpdate(final ACLMessage inform, final String notFoundLog, final String updateLog,
			final JobExecutionStateEnum jobState) {
		final JobInstanceIdentifier jobInstanceId = readMessageContent(inform, JobInstanceIdentifier.class);
		final ServerJob job = getJobByInstanceIdAndServer(jobInstanceId.getJobInstanceId(), inform.getSender(),
				myGreenEnergyAgent.getServerJobs());

		MDC.put(MDC_JOB_ID, jobInstanceId.getJobId());
		if (nonNull(job)) {
			logger.info(updateLog, jobInstanceId.getJobId());
			final boolean hasStarted = isJobStarted(job, myGreenEnergyAgent.getServerJobs());

			myGreenEnergyAgent.getServerJobs().replace(job, jobState.getStatus(hasStarted));
			myGreenEnergyAgent.manage().updateGUI();
		} else {
			logger.info(notFoundLog, jobInstanceId.getJobId());
		}
	}
}
