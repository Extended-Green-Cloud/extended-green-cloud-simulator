package com.greencloud.application.agents.server.behaviour.errorhandling.announcer;

import static com.greencloud.application.agents.server.behaviour.errorhandling.announcer.logs.ErrorHandlingAnnouncerLog.INTERNAL_SERVER_ERROR_FINISH_DETECTED_LOG;
import static com.greencloud.application.agents.server.behaviour.errorhandling.announcer.logs.ErrorHandlingAnnouncerLog.INTERNAL_SERVER_ERROR_FINISH_LEAVE_ON_HOLD_LOG;
import static com.greencloud.application.agents.server.behaviour.errorhandling.announcer.logs.ErrorHandlingAnnouncerLog.INTERNAL_SERVER_ERROR_FINISH_UPDATE_JOB_STATUS_LOG;
import static com.greencloud.application.agents.server.behaviour.errorhandling.announcer.logs.ErrorHandlingAnnouncerLog.INTERNAL_SERVER_ERROR_FINISH_UPDATE_SERVER_LOG;
import static com.greencloud.application.agents.server.behaviour.errorhandling.announcer.logs.ErrorHandlingAnnouncerLog.INTERNAL_SERVER_ERROR_FINISH_USE_GREEN_ENERGY_LOG;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.messages.constants.MessageConversationConstants.GREEN_POWER_JOB_ID;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.NETWORK_ERROR_FINISH_ALERT_PROTOCOL;
import static com.greencloud.application.messages.factory.NetworkErrorMessageFactory.prepareNetworkFailureInformation;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.domain.job.enums.JobExecutionStateEnum.EXECUTING_ON_GREEN;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.utils.JobUtils;
import com.greencloud.commons.domain.job.ClientJob;
import com.greencloud.commons.domain.resources.HardwareResources;

import jade.core.behaviours.OneShotBehaviour;

/**
 * Behaviour sends the information that the internal server error has finished
 */
public class AnnounceInternalServerErrorFinish extends OneShotBehaviour {

	private static final Logger logger = getLogger(AnnounceInternalServerErrorFinish.class);

	private final ServerAgent myServerAgent;

	/**
	 * Behaviour constructor
	 *
	 * @param myAgent agent executing the behaviour
	 */
	public AnnounceInternalServerErrorFinish(ServerAgent myAgent) {
		super(myAgent);
		this.myServerAgent = myAgent;
	}

	/**
	 * Method which is responsible for passing the information that the internal server error has
	 * finished and that the jobs affected by it can be supplied using the green source power.
	 */
	@Override
	public void action() {
		logger.info(INTERNAL_SERVER_ERROR_FINISH_DETECTED_LOG);
		myServerAgent.setHasError(false);
		final List<ClientJob> affectedJobs = myServerAgent.manage().getActiveJobsOnHold(myServerAgent.getServerJobs());

		if (affectedJobs.isEmpty()) {
			logger.info(INTERNAL_SERVER_ERROR_FINISH_UPDATE_SERVER_LOG);
		} else {
			logger.info(INTERNAL_SERVER_ERROR_FINISH_UPDATE_JOB_STATUS_LOG);

			affectedJobs.forEach(job -> {
				final boolean isJobPresent = myServerAgent.getServerJobs().containsKey(job) &&
						myServerAgent.getGreenSourceForJobMap().containsKey(job.getJobId());

				if (isJobPresent) {
					handlePowerShortageFinish(job);
				}
			});
		}
	}

	private void handlePowerShortageFinish(final ClientJob job) {
		final JobInstanceIdentifier jobInstance = mapToJobInstanceId(job);
		final HardwareResources availableResources = myServerAgent.resources()
				.getAvailableResources(job, jobInstance, null);

		MDC.put(MDC_JOB_ID, job.getJobId());
		if (!availableResources.areSufficient(job.getEstimatedResources())) {
			logger.info(INTERNAL_SERVER_ERROR_FINISH_LEAVE_ON_HOLD_LOG, job.getJobId());
		} else {
			logger.info(INTERNAL_SERVER_ERROR_FINISH_USE_GREEN_ENERGY_LOG, job.getJobId());
			supplyJobWithGreenEnergy(job, jobInstance);
		}
	}

	private void supplyJobWithGreenEnergy(final ClientJob job, final JobInstanceIdentifier jobInstance) {
		final boolean hasStarted = JobUtils.isJobStarted(job, myServerAgent.getServerJobs());

		myServerAgent.getServerJobs().replace(job, EXECUTING_ON_GREEN.getStatus(hasStarted));
		if (hasStarted) {
			myServerAgent.message().informCNAAboutStatusChange(mapToJobInstanceId(job), GREEN_POWER_JOB_ID);
		}
		myServerAgent.manage().updateGUI();
		myServerAgent.send(prepareNetworkFailureInformation(jobInstance, NETWORK_ERROR_FINISH_ALERT_PROTOCOL,
				myServerAgent.getGreenSourceForJobMap().get(job.getJobId()),
				myServerAgent.getOwnerCloudNetworkAgent()));
	}
}
