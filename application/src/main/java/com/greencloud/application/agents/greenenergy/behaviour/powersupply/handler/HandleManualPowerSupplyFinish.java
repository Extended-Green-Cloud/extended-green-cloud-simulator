package com.greencloud.application.agents.greenenergy.behaviour.powersupply.handler;

import static com.greencloud.application.agents.greenenergy.behaviour.powersupply.handler.logs.PowerSupplyHandlerLog.MANUAL_POWER_SUPPLY_FINISH_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareManualFinishMessageForServer;
import static com.greencloud.application.utils.JobUtils.isJobStarted;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.ACCEPTED_JOB_STATUSES;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.commons.job.JobResultType;
import com.greencloud.commons.job.ServerJob;

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;

/**
 * Behaviour finishes power job manually if it has not finished yet
 */
public class HandleManualPowerSupplyFinish extends WakerBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(HandleManualPowerSupplyFinish.class);

	private final ServerJob job;
	private final GreenEnergyAgent myGreenEnergyAgent;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent   agent which is executing the behaviour
	 * @param endDate date when the job execution should finish
	 * @param job     unique job
	 */
	public HandleManualPowerSupplyFinish(final Agent agent, final Date endDate,
			final ServerJob job) {
		super(agent, endDate);
		this.myGreenEnergyAgent = (GreenEnergyAgent) agent;
		this.job = job;
	}

	/**
	 * Method verifies if the job execution finished correctly.
	 * If there was no information about job finish the Green Source finishes the power supply manually and sends
	 * the warning to the Server Agent.
	 */
	@Override
	protected void onWake() {
		final boolean isJobPresent = myGreenEnergyAgent.getServerJobs().containsKey(job);

		if (isJobPresent && ACCEPTED_JOB_STATUSES.contains(myGreenEnergyAgent.getServerJobs().get(job))) {
			MDC.put(MDC_JOB_ID, job.getJobId());
			logger.error(MANUAL_POWER_SUPPLY_FINISH_LOG);

			if (isJobStarted(job, myGreenEnergyAgent.getServerJobs())) {
				myGreenEnergyAgent.manage().incrementJobCounter(mapToJobInstanceId(job), JobResultType.FINISH);
			}
			myGreenEnergyAgent.manage().removeJob(job);
			myGreenEnergyAgent.manage().updateGreenSourceGUI();

			myAgent.send(prepareManualFinishMessageForServer(mapToJobInstanceId(job), job.getServer()));
		}
	}
}
