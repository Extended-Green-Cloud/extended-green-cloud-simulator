package com.greencloud.application.agents.greenenergy.behaviour.powersupply.listener;

import static com.greencloud.application.agents.greenenergy.behaviour.powersupply.listener.logs.PowerSupplyListenerLog.FINISH_POWER_SUPPLY_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powersupply.listener.logs.PowerSupplyListenerLog.START_POWER_SUPPLY_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powersupply.listener.template.PowerSupplyMessageTemplates.POWER_SUPPLY_STATUS_TEMPLATE;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.domain.job.JobStatusEnum.RUNNING_JOB_STATUSES;
import static com.greencloud.application.messages.MessagingUtils.readMessageContent;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.FINISH_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.STARTED_JOB_ID;
import static com.greencloud.application.utils.JobUtils.getJobByIdAndStartDate;
import static java.util.Objects.nonNull;

import com.greencloud.commons.job.ExecutionJobStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.JobStatusEnum;
import com.greencloud.application.utils.TimeUtils;
import com.greencloud.commons.job.PowerJob;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour listens for the information that the power supply for given job should start or finish
 */
public class ListenForPowerSupplyStatus extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForPowerSupplyStatus.class);

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
	 * Method which listens for the information that the job execution has started/finished. It is responsible
	 * for updating the current green energy source state.
	 */
	@Override
	public void action() {
		final ACLMessage message = myGreenEnergyAgent.receive(POWER_SUPPLY_STATUS_TEMPLATE);
		if (nonNull(message)) {
			final JobInstanceIdentifier jobInstanceId = readMessageContent(message, JobInstanceIdentifier.class);
			final PowerJob powerJob = getJobByIdAndStartDate(jobInstanceId, myGreenEnergyAgent.getPowerJobs());

			if (nonNull(powerJob)) {
				switch (message.getConversationId()) {
					case FINISH_JOB_ID -> handlePowerSupplyFinish(powerJob, jobInstanceId);
					case STARTED_JOB_ID -> handlePowerSupplyStart(powerJob, jobInstanceId);
				}
			}
		} else {
			block();
		}
	}

	private void handlePowerSupplyStart(final PowerJob powerJob, final JobInstanceIdentifier jobInstance) {
		MDC.put(MDC_JOB_ID, powerJob.getJobId());
		logger.info(START_POWER_SUPPLY_LOG, jobInstance.getJobId());
		myGreenEnergyAgent.getPowerJobs().replace(powerJob, ExecutionJobStatusEnum.ACCEPTED, ExecutionJobStatusEnum.IN_PROGRESS);
		myGreenEnergyAgent.getPowerJobs().replace(powerJob, ExecutionJobStatusEnum.ON_HOLD_PLANNED, ExecutionJobStatusEnum.ON_HOLD);
		myGreenEnergyAgent.manage().incrementStartedJobs(jobInstance);
	}

	private void handlePowerSupplyFinish(final PowerJob powerJob, final JobInstanceIdentifier jobInstance) {
		if (RUNNING_JOB_STATUSES.contains(myGreenEnergyAgent.getPowerJobs().get(powerJob))) {
			myGreenEnergyAgent.manage().incrementFinishedJobs(jobInstance);
		}
		MDC.put(MDC_JOB_ID, powerJob.getJobId());
		logger.info(FINISH_POWER_SUPPLY_LOG, jobInstance.getJobId());
		myGreenEnergyAgent.getPowerJobs().remove(powerJob);
		myGreenEnergyAgent.manage().updateGreenSourceGUI();
	}
}
