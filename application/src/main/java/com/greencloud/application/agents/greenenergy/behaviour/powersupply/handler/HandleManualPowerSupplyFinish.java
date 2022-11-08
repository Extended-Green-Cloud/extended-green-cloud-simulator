package com.greencloud.application.agents.greenenergy.behaviour.powersupply.handler;

import static com.greencloud.application.agents.greenenergy.behaviour.powersupply.handler.logs.PowerSupplyHandlerLog.MANUAL_POWER_SUPPLY_FINISH_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.domain.job.JobStatusEnum.ACCEPTED_JOB_STATUSES;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareManualFinishMessageForServer;
import static java.util.Objects.nonNull;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.PowerJob;

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;

/**
 * Behaviour finishes power job manually if it has not finished yet
 */
public class HandleManualPowerSupplyFinish extends WakerBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(HandleManualPowerSupplyFinish.class);

	private final JobInstanceIdentifier jobInstanceId;
	private final GreenEnergyAgent myGreenEnergyAgent;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent         agent which is executing the behaviour
	 * @param endDate       date when the job execution should finish
	 * @param jobInstanceId unique job instance identifier
	 */
	public HandleManualPowerSupplyFinish(final Agent agent, final Date endDate,
			final JobInstanceIdentifier jobInstanceId) {
		super(agent, endDate);
		this.myGreenEnergyAgent = (GreenEnergyAgent) agent;
		this.jobInstanceId = jobInstanceId;
	}

	/**
	 * Method verifies if the job execution finished correctly.
	 * If there was no information about job finish the Green Source finishes the power supply manually and sends
	 * the warning to the Server Agent.
	 */
	@Override
	protected void onWake() {
		final PowerJob job = myGreenEnergyAgent.manage()
				.getJobByIdAndStartDate(jobInstanceId.getJobId(), jobInstanceId.getStartTime());

		if (nonNull(job) && ACCEPTED_JOB_STATUSES.contains(myGreenEnergyAgent.getPowerJobs().get(job))) {
			MDC.put(MDC_JOB_ID, job.getJobId());
			logger.error(MANUAL_POWER_SUPPLY_FINISH_LOG);

			myGreenEnergyAgent.manage().incrementFinishedJobs(jobInstanceId);
			myGreenEnergyAgent.getPowerJobs().remove(job);
			myGreenEnergyAgent.manage().updateGreenSourceGUI();

			myAgent.send(prepareManualFinishMessageForServer(jobInstanceId, myGreenEnergyAgent.getOwnerServer()));
		}
	}
}
