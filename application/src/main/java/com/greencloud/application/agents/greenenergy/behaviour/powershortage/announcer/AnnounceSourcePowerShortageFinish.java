package com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer;

import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.logs.PowerShortageSourceAnnouncerLog.CHANGE_JOB_STATUS_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.logs.PowerShortageSourceAnnouncerLog.NO_ENERGY_LEAVE_ON_HOLD_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.logs.PowerShortageSourceAnnouncerLog.POWER_SHORTAGE_SOURCE_FINISH_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.logs.PowerShortageSourceAnnouncerLog.POWER_SHORTAGE_SOURCE_FINISH_NO_JOBS_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.logs.PowerShortageSourceAnnouncerLog.POWER_SHORTAGE_SOURCE_JOB_ENDED_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.logs.PowerShortageSourceAnnouncerLog.POWER_SHORTAGE_SOURCE_VERIFY_POWER_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.logs.PowerShortageSourceAnnouncerLog.WEATHER_UNAVAILABLE_JOB_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.request.RequestWeatherData.createWeatherRequest;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.NETWORK_ERROR_FINISH_ALERT_PROTOCOL;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.ON_HOLD_JOB_CHECK_PROTOCOL;
import static com.greencloud.application.messages.factory.NetworkErrorMessageFactory.prepareNetworkFailureInformation;
import static com.greencloud.application.utils.JobUtils.isJobStarted;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.domain.job.enums.JobExecutionStateEnum.EXECUTING_ON_GREEN;
import static java.lang.String.join;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.domain.weather.MonitoringData;
import com.greencloud.commons.domain.job.ServerJob;
import com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;

/**
 * Behaviour announces that the power shortage will be finished at the given moment
 */
public class AnnounceSourcePowerShortageFinish extends OneShotBehaviour {

	private static final Logger logger = getLogger(AnnounceSourcePowerShortageFinish.class);

	private final GreenEnergyAgent myGreenAgent;

	/**
	 * Behaviour constructor
	 *
	 * @param myAgent agent executing the behaviour
	 */
	public AnnounceSourcePowerShortageFinish(GreenEnergyAgent myAgent) {
		super(myAgent);
		this.myGreenAgent = myAgent;
	}

	/**
	 * Method sends the information that the power shortage has finished and
	 * that the jobs which were kept on hold can now be supplied with green power
	 */
	@Override
	public void action() {
		logger.info(POWER_SHORTAGE_SOURCE_FINISH_LOG);
		final List<ServerJob> jobsOnHold = myGreenAgent.manage().getActiveJobsOnHold(myGreenAgent.getServerJobs());

		if (jobsOnHold.isEmpty()) {
			logger.info(POWER_SHORTAGE_SOURCE_FINISH_NO_JOBS_LOG);
		} else {
			jobsOnHold.forEach(powerJob -> {
				MDC.put(MDC_JOB_ID, powerJob.getJobId());

				if (myGreenAgent.getServerJobs().containsKey(powerJob)) {
					logger.info(POWER_SHORTAGE_SOURCE_VERIFY_POWER_LOG, powerJob.getJobId());
					myAgent.addBehaviour(prepareVerificationBehaviour(powerJob));
				} else {
					logger.info(POWER_SHORTAGE_SOURCE_JOB_ENDED_LOG, powerJob.getJobId());
				}
			});
		}
		myGreenAgent.setHasError(false);
	}

	private Behaviour prepareVerificationBehaviour(final ServerJob affectedJob) {
		final String conversationId = join("_", affectedJob.getJobId(), affectedJob.getStartTime().toString());
		return createWeatherRequest(myGreenAgent, ON_HOLD_JOB_CHECK_PROTOCOL, conversationId,
				getResponseHandler(affectedJob), getRequestRefuseHandler(affectedJob), affectedJob);
	}

	private BiConsumer<MonitoringData, Exception> getResponseHandler(final ServerJob job) {
		return (data, error) -> {
			MDC.put(MDC_JOB_ID, job.getJobId());

			if (nonNull(error)) {
				logger.info(WEATHER_UNAVAILABLE_JOB_LOG, job.getJobId());
				return;
			}

			final Optional<Double> availableEnergy = myGreenAgent.power().getAvailableEnergy(job, data, false);
			if (availableEnergy.isEmpty() || job.getEstimatedEnergy() > availableEnergy.get()) {
				logger.info(NO_ENERGY_LEAVE_ON_HOLD_LOG, job.getJobId());
			} else {
				logger.info(CHANGE_JOB_STATUS_LOG, job.getJobId());
				final boolean isJobStarted = isJobStarted(job, myGreenAgent.getServerJobs());
				final JobExecutionStatusEnum newStatus = EXECUTING_ON_GREEN.getStatus(isJobStarted);

				myGreenAgent.getServerJobs().replace(job, newStatus);
				myGreenAgent.manage().updateGUI();
				myGreenAgent.send(prepareNetworkFailureInformation(mapToJobInstanceId(job),
						NETWORK_ERROR_FINISH_ALERT_PROTOCOL, job.getServer()));
			}
		};
	}

	private Runnable getRequestRefuseHandler(final ServerJob affectedJob) {
		MDC.put(MDC_JOB_ID, affectedJob.getJobId());
		return () -> logger.info(WEATHER_UNAVAILABLE_JOB_LOG, affectedJob.getJobId());
	}
}
