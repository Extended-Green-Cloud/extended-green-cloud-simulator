package com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer;

import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.logs.PowerShortageSourceAnnouncerLog.POWER_SHORTAGE_SOURCE_FINISH_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.logs.PowerShortageSourceAnnouncerLog.POWER_SHORTAGE_SOURCE_FINISH_NO_JOBS_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.logs.PowerShortageSourceAnnouncerLog.POWER_SHORTAGE_SOURCE_JOB_ENDED_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.logs.PowerShortageSourceAnnouncerLog.POWER_SHORTAGE_SOURCE_VERIFY_POWER_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.ON_HOLD_JOB_CHECK_PROTOCOL;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener.ListenForWeatherData;
import com.greencloud.application.agents.greenenergy.behaviour.weathercheck.request.RequestWeatherData;
import com.greencloud.application.domain.job.JobStatusEnum;
import com.greencloud.application.domain.job.PowerJob;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;

/**
 * Behaviour announces that the power shortage has finished at the given moment
 */
public class AnnounceSourcePowerShortageFinish extends OneShotBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(AnnounceSourcePowerShortageFinish.class);

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
		final List<PowerJob> jobsOnHold = getJobsOnHold();

		if (jobsOnHold.isEmpty()) {
			logger.info(POWER_SHORTAGE_SOURCE_FINISH_NO_JOBS_LOG);
		} else {
			jobsOnHold.forEach(powerJob -> {
				MDC.put(MDC_JOB_ID, powerJob.getJobId());
				if (myGreenAgent.getPowerJobs().containsKey(powerJob)) {
					logger.info(POWER_SHORTAGE_SOURCE_VERIFY_POWER_LOG, powerJob.getJobId());
					myAgent.addBehaviour(prepareVerificationBehaviour(powerJob));
				} else {
					logger.info(POWER_SHORTAGE_SOURCE_JOB_ENDED_LOG, powerJob.getJobId());
				}
			});
		}
		myGreenAgent.manageGreenPower()
				.setCurrentMaximumCapacity(myGreenAgent.manageGreenPower().getInitialMaximumCapacity());
	}

	private Behaviour prepareVerificationBehaviour(final PowerJob affectedJob) {
		final String conversationId = String.join("_", affectedJob.getJobId(),
				affectedJob.getStartTime().toString());

		final SequentialBehaviour sequentialBehaviour = new SequentialBehaviour();
		sequentialBehaviour.addSubBehaviour(
				new RequestWeatherData(myGreenAgent, conversationId, ON_HOLD_JOB_CHECK_PROTOCOL, affectedJob));
		sequentialBehaviour.addSubBehaviour(
				new ListenForWeatherData(myGreenAgent, affectedJob, ON_HOLD_JOB_CHECK_PROTOCOL, conversationId,
						sequentialBehaviour));
		return sequentialBehaviour;
	}

	private List<PowerJob> getJobsOnHold() {
		return myGreenAgent.getPowerJobs().entrySet().stream()
				.filter(job -> job.getValue().equals(JobStatusEnum.ON_HOLD) &&
						job.getKey().getEndTime().isAfter(getCurrentTime()))
				.map(Map.Entry::getKey)
				.toList();
	}
}
