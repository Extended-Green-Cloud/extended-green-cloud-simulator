package com.greencloud.application.agents.server.behaviour.powershortage.announcer;

import static com.greencloud.application.agents.server.behaviour.powershortage.announcer.logs.PowerShortageServerAnnouncerLog.POWER_SHORTAGE_FINISH_DETECTED_LOG;
import static com.greencloud.application.agents.server.behaviour.powershortage.announcer.logs.PowerShortageServerAnnouncerLog.POWER_SHORTAGE_FINISH_LEAVE_ON_HOLD_LOG;
import static com.greencloud.application.agents.server.behaviour.powershortage.announcer.logs.PowerShortageServerAnnouncerLog.POWER_SHORTAGE_FINISH_UPDATE_CAPACITY_LOG;
import static com.greencloud.application.agents.server.behaviour.powershortage.announcer.logs.PowerShortageServerAnnouncerLog.POWER_SHORTAGE_FINISH_UPDATE_JOB_STATUS_LOG;
import static com.greencloud.application.agents.server.behaviour.powershortage.announcer.logs.PowerShortageServerAnnouncerLog.POWER_SHORTAGE_FINISH_USE_BACK_UP_LOG;
import static com.greencloud.application.agents.server.behaviour.powershortage.announcer.logs.PowerShortageServerAnnouncerLog.POWER_SHORTAGE_FINISH_USE_GREEN_ENERGY_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.domain.job.JobStatusEnum.BACK_UP_POWER_STATUSES;
import static com.greencloud.application.domain.job.JobStatusEnum.IN_PROGRESS_BACKUP_ENERGY;
import static com.greencloud.application.domain.job.JobStatusEnum.IN_PROGRESS_BACKUP_ENERGY_PLANNED;
import static com.greencloud.application.domain.job.JobStatusEnum.ON_HOLD;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.BACK_UP_POWER_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.GREEN_POWER_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.POWER_SHORTAGE_FINISH_ALERT_PROTOCOL;
import static com.greencloud.application.messages.domain.factory.PowerShortageMessageFactory.prepareJobPowerShortageInformation;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.domain.job.ClientJob;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.JobStatusEnum;
import com.greencloud.application.mapper.JobMapper;
import com.greencloud.application.utils.TimeUtils;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour sends the information that the power shortage for given server has finished
 */
public class AnnounceServerPowerShortageFinish extends OneShotBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(AnnounceServerPowerShortageFinish.class);

	private final ServerAgent myServerAgent;

	/**
	 * Behaviour constructor
	 *
	 * @param myAgent agent executing the behaviour
	 */
	public AnnounceServerPowerShortageFinish(ServerAgent myAgent) {
		super(myAgent);
		this.myServerAgent = myAgent;
	}

	/**
	 * Method which is responsible for passing the information that the server power shortage has
	 * finished and that the jobs affected by it can be supplied using the green source power.
	 */
	@Override
	public void action() {
		logger.info(POWER_SHORTAGE_FINISH_DETECTED_LOG);
		myServerAgent.setCurrentMaximumCapacity(myServerAgent.getInitialMaximumCapacity());
		final List<ClientJob> affectedJobs = getJobsOnHold();

		if (affectedJobs.isEmpty()) {
			logger.info(POWER_SHORTAGE_FINISH_UPDATE_CAPACITY_LOG);
		} else {
			logger.info(POWER_SHORTAGE_FINISH_UPDATE_JOB_STATUS_LOG);

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
		final JobInstanceIdentifier jobInstance = JobMapper.mapToJobInstanceId(job);
		final int jobPower = job.getPower();
		final int availablePower = myServerAgent.manage()
				.getAvailableCapacity(job.getStartTime(), job.getEndTime(), jobInstance, null);
		final int availableBackUpPower = myServerAgent.manage()
				.getAvailableCapacity(job.getStartTime(), job.getEndTime(), jobInstance,
						BACK_UP_POWER_STATUSES);

		MDC.put(MDC_JOB_ID, job.getJobId());

		if (availablePower < jobPower && availableBackUpPower < jobPower) {
			logger.info(POWER_SHORTAGE_FINISH_LEAVE_ON_HOLD_LOG, job.getJobId());
		} else if (availableBackUpPower >= jobPower) {
			logger.info(POWER_SHORTAGE_FINISH_USE_BACK_UP_LOG, job.getJobId());
			supplyJobWithBackUpPower(job);
		} else {
			logger.info(POWER_SHORTAGE_FINISH_USE_GREEN_ENERGY_LOG, job.getJobId());
			supplyJobWithGreenEnergy(job, jobInstance);
		}
	}

	private void supplyJobWithBackUpPower(final ClientJob job) {
		final boolean hasJobStarted = myServerAgent.getServerJobs().get(job)
				.equals(JobStatusEnum.ON_HOLD);
		final JobStatusEnum status = hasJobStarted ?
				IN_PROGRESS_BACKUP_ENERGY :
				IN_PROGRESS_BACKUP_ENERGY_PLANNED;
		myServerAgent.getServerJobs().replace(job, status);
		myServerAgent.manage().updateServerGUI();

		if (hasJobStarted) {
			myServerAgent.manage()
					.informCNAAboutStatusChange(mapToJobInstanceId(job), BACK_UP_POWER_JOB_ID);
		}
	}

	private void supplyJobWithGreenEnergy(final ClientJob job, final JobInstanceIdentifier jobInstance) {
		final boolean hasStarted = job.getStartTime().isAfter(getCurrentTime());
		final JobStatusEnum newStatus = hasStarted ?
				JobStatusEnum.ACCEPTED :
				JobStatusEnum.IN_PROGRESS;
		myServerAgent.getServerJobs().replace(job, newStatus);

		final AID greenSource = myServerAgent.getGreenSourceForJobMap().get(job.getJobId());
		final ACLMessage finishInformation = prepareJobPowerShortageInformation(jobInstance, greenSource,
				POWER_SHORTAGE_FINISH_ALERT_PROTOCOL);
		finishInformation.addReceiver(myServerAgent.getOwnerCloudNetworkAgent());

		if (hasStarted) {
			myServerAgent.manage().informCNAAboutStatusChange(mapToJobInstanceId(job), GREEN_POWER_JOB_ID);
		}

		myServerAgent.manage().updateServerGUI();
		myServerAgent.send(finishInformation);
	}

	private List<ClientJob> getJobsOnHold() {
		return myServerAgent.getServerJobs().entrySet().stream()
				.filter(job -> (job.getValue().equals(JobStatusEnum.ON_HOLD_PLANNED) || job.getValue().equals(ON_HOLD))
						&& job.getKey().getEndTime().isAfter(TimeUtils.getCurrentTime()))
				.map(Map.Entry::getKey)
				.toList();
	}
}
