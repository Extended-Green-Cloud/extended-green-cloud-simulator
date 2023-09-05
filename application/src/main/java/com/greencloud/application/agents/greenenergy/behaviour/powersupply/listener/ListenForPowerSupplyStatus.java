package com.greencloud.application.agents.greenenergy.behaviour.powersupply.listener;

import static com.greencloud.application.agents.greenenergy.behaviour.powersupply.listener.logs.PowerSupplyListenerLog.FINISH_POWER_SUPPLY_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powersupply.listener.logs.PowerSupplyListenerLog.JOB_NOT_FOUND;
import static com.greencloud.application.agents.greenenergy.behaviour.powersupply.listener.logs.PowerSupplyListenerLog.START_POWER_SUPPLY_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powersupply.listener.template.PowerSupplyMessageTemplates.POWER_SUPPLY_STATUS_TEMPLATE;
import static com.greencloud.application.agents.greenenergy.constants.GreenEnergyAgentConstants.MAX_NUMBER_OF_SERVER_MESSAGES;
import static com.greencloud.application.messages.constants.MessageConversationConstants.FINISH_JOB_ID;
import static com.greencloud.application.messages.constants.MessageConversationConstants.STARTED_JOB_ID;
import static com.greencloud.application.utils.JobUtils.getJobByInstanceIdAndServer;
import static com.greencloud.application.utils.JobUtils.isJobStarted;
import static com.greencloud.application.utils.MessagingUtils.readMessageContent;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.domain.job.enums.JobExecutionResultEnum.FINISH;
import static com.greencloud.commons.domain.job.enums.JobExecutionResultEnum.STARTED;
import static com.greencloud.commons.domain.job.enums.JobExecutionStateEnum.replaceStatusToActive;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.JobStatusUpdate;
import com.greencloud.commons.domain.job.ServerJob;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour listens for the information that the power supply for given job should start or finish
 */
public class ListenForPowerSupplyStatus extends CyclicBehaviour {

	private static final Logger logger = getLogger(ListenForPowerSupplyStatus.class);

	private final GreenEnergyAgent myGreenEnergyAgent;

	/**
	 * Behaviour constructor.
	 *
	 * @param myGreenEnergyAgent agent which is executing the behaviour
	 */
	public ListenForPowerSupplyStatus(final GreenEnergyAgent myGreenEnergyAgent) {
		this.myGreenEnergyAgent = myGreenEnergyAgent;
	}

	/**
	 * Method which listens for the information that the job execution has started/finished.
	 * It is responsible for updating the current green energy source state.
	 */
	@Override
	public void action() {
		final List<ACLMessage> messages = myGreenEnergyAgent.receive(POWER_SUPPLY_STATUS_TEMPLATE,
				MAX_NUMBER_OF_SERVER_MESSAGES);

		if (nonNull(messages)) {
			messages.forEach(message -> {
				final JobStatusUpdate jobStatusUpdate = readMessageContent(message, JobStatusUpdate.class);
				final JobInstanceIdentifier jobInstanceId = jobStatusUpdate.getJobInstance();
				final ServerJob serverJob = getJobByInstanceIdAndServer(jobInstanceId.getJobInstanceId(),
						message.getSender(), myGreenEnergyAgent.getServerJobs());

				if (nonNull(serverJob)) {
					MDC.put(MDC_JOB_ID, serverJob.getJobId());
					switch (message.getConversationId()) {
						case FINISH_JOB_ID -> handlePowerSupplyFinish(serverJob, jobInstanceId);
						case STARTED_JOB_ID -> handlePowerSupplyStart(serverJob, jobInstanceId);
					}
				} else {
					MDC.put(MDC_JOB_ID, jobStatusUpdate.getJobInstance().getJobId());
					logger.info(JOB_NOT_FOUND, jobInstanceId.getJobInstanceId());
				}
			});
		} else {
			block();
		}
	}

	private void handlePowerSupplyStart(final ServerJob serverJob, final JobInstanceIdentifier jobInstance) {
		logger.info(START_POWER_SUPPLY_LOG, jobInstance.getJobId());

		replaceStatusToActive(myGreenEnergyAgent.getServerJobs(), serverJob);
		myGreenEnergyAgent.manage().incrementJobCounter(jobInstance, STARTED);
	}

	private void handlePowerSupplyFinish(final ServerJob serverJob, final JobInstanceIdentifier jobInstance) {
		if (isJobStarted(serverJob, myGreenEnergyAgent.getServerJobs())) {
			myGreenEnergyAgent.manage().incrementJobCounter(jobInstance, FINISH);
		}
		logger.info(FINISH_POWER_SUPPLY_LOG, jobInstance);

		myGreenEnergyAgent.manage().removeJob(serverJob);
		myGreenEnergyAgent.manage().updateGUI();
	}
}
