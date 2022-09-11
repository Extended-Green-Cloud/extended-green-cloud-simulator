package agents.server.behaviour.powershortage.announcer;

import static agents.server.behaviour.powershortage.announcer.logs.PowerShortageServerAnnouncerLog.POWER_SHORTAGE_FINISH_DETECTED_LOG;
import static agents.server.behaviour.powershortage.announcer.logs.PowerShortageServerAnnouncerLog.POWER_SHORTAGE_FINISH_LEAVE_ON_HOLD_LOG;
import static agents.server.behaviour.powershortage.announcer.logs.PowerShortageServerAnnouncerLog.POWER_SHORTAGE_FINISH_UPDATE_CAPACITY_LOG;
import static agents.server.behaviour.powershortage.announcer.logs.PowerShortageServerAnnouncerLog.POWER_SHORTAGE_FINISH_UPDATE_JOB_STATUS_LOG;
import static agents.server.behaviour.powershortage.announcer.logs.PowerShortageServerAnnouncerLog.POWER_SHORTAGE_FINISH_USE_BACK_UP_LOG;
import static agents.server.behaviour.powershortage.announcer.logs.PowerShortageServerAnnouncerLog.POWER_SHORTAGE_FINISH_USE_GREEN_ENERGY_LOG;
import static agents.server.domain.ServerPowerSourceType.BACK_UP_POWER;
import static utils.GUIUtils.displayMessageArrow;
import static utils.TimeUtils.getCurrentTime;
import static messages.domain.factory.PowerShortageMessageFactory.preparePowerShortageFinishInformation;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agents.server.ServerAgent;
import mapper.JobMapper;
import domain.job.ClientJob;
import domain.job.JobInstanceIdentifier;
import domain.job.JobStatusEnum;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour sends the information that the power shortage for given server has finished
 */
public class AnnounceServerPowerShortageFinish extends OneShotBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(AnnounceServerPowerShortageFinish.class);

	private final ServerAgent myServerAgent;
	private final String guid;

	/**
	 * Behaviour constructor
	 *
	 * @param myAgent agent executing the behaviour
	 */
	public AnnounceServerPowerShortageFinish(ServerAgent myAgent) {
		super(myAgent);
		this.myServerAgent = myAgent;
		this.guid = myServerAgent.getName();
	}

	/**
	 * Method which is responsible for passing the information that the server power shortage has
	 * finished and that the jobs affected by it can be supplied using the green source power.
	 */
	@Override
	public void action() {
		logger.info(POWER_SHORTAGE_FINISH_DETECTED_LOG, guid);
		myServerAgent.setCurrentMaximumCapacity(myServerAgent.getInitialMaximumCapacity());
		final List<ClientJob> affectedJobs = getJobsOnHold();

		if (affectedJobs.isEmpty()) {
			logger.info(POWER_SHORTAGE_FINISH_UPDATE_CAPACITY_LOG, guid);
		} else {
			logger.info(POWER_SHORTAGE_FINISH_UPDATE_JOB_STATUS_LOG, guid);

			affectedJobs.forEach(job -> {
				final boolean isJobPresent = myServerAgent.getServerJobs().containsKey(job) &&
						myServerAgent.getGreenSourceForJobMap().containsKey(job.getJobId());

				if (isJobPresent) {
					final JobInstanceIdentifier jobInstance = JobMapper.mapToJobInstanceId(job);
					final int jobPower = job.getPower();
					final int availablePower = myServerAgent.manage()
							.getAvailableCapacity(job.getStartTime(), job.getEndTime(), jobInstance, null);
					final int availableBackUpPower = myServerAgent.manage()
							.getAvailableCapacity(job.getStartTime(), job.getEndTime(), jobInstance, BACK_UP_POWER);

					if (availablePower < jobPower && availableBackUpPower < jobPower) {
						logger.info(POWER_SHORTAGE_FINISH_LEAVE_ON_HOLD_LOG, guid, job.getJobId());
					} else if (availableBackUpPower >= jobPower) {
						logger.info(POWER_SHORTAGE_FINISH_USE_BACK_UP_LOG, guid, job.getJobId());
						myServerAgent.getServerJobs().replace(job, JobStatusEnum.IN_PROGRESS_BACKUP_ENERGY);
						myServerAgent.manage().updateServerGUI();
					} else {
						logger.info(POWER_SHORTAGE_FINISH_USE_GREEN_ENERGY_LOG, guid, job.getJobId());
						updateJobStatus(job, jobInstance);
					}
				}
			});
		}
	}

	private void updateJobStatus(final ClientJob job, final JobInstanceIdentifier jobInstance) {
		final JobStatusEnum newStatus = job.getStartTime().isAfter(getCurrentTime()) ?
				JobStatusEnum.ACCEPTED :
				JobStatusEnum.IN_PROGRESS;
		myServerAgent.getServerJobs().replace(job, newStatus);

		final AID greenSource = myServerAgent.getGreenSourceForJobMap().get(job.getJobId());
		final ACLMessage finishInformation = preparePowerShortageFinishInformation(jobInstance, greenSource);
		finishInformation.addReceiver(myServerAgent.getOwnerCloudNetworkAgent());

		displayMessageArrow(myServerAgent, finishInformation.getAllReceiver());
		myServerAgent.manage().updateServerGUI();
		myServerAgent.send(finishInformation);
	}

	private List<ClientJob> getJobsOnHold() {
		return myServerAgent.getServerJobs().entrySet().stream()
				.filter(job -> job.getValue().equals(JobStatusEnum.ON_HOLD)
						&& job.getKey().getEndTime().isAfter(getCurrentTime()))
				.map(Map.Entry::getKey)
				.toList();
	}
}
