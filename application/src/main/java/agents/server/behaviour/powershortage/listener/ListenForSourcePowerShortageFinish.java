package agents.server.behaviour.powershortage.listener;

import static agents.server.behaviour.powershortage.listener.logs.PowerShortageServerListenerLog.GS_SHORTAGE_FINISH_LOG;
import static agents.server.behaviour.powershortage.listener.templates.PowerShortageServerMessageTemplates.SOURCE_POWER_SHORTAGE_FINISH_TEMPLATE;
import static utils.GUIUtils.displayMessageArrow;
import static utils.TimeUtils.getCurrentTime;
import static mapper.JsonMapper.getMapper;
import static messages.domain.factory.PowerShortageMessageFactory.preparePowerShortageFinishInformation;

import java.util.EnumSet;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agents.server.ServerAgent;
import domain.job.ClientJob;
import mapper.JobMapper;
import domain.job.JobInstanceIdentifier;
import domain.job.JobStatusEnum;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour listens for the information that the power shortage in the given green source has finished
 */
public class ListenForSourcePowerShortageFinish extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForSourcePowerShortageFinish.class);
	private static final EnumSet<JobStatusEnum> POWER_SHORTAGE_STATUSES = EnumSet.of(
			JobStatusEnum.IN_PROGRESS_BACKUP_ENERGY,
			JobStatusEnum.ON_HOLD_SOURCE_SHORTAGE);

	private ServerAgent myServerAgent;
	private String guid;

	/**
	 * Method casts the abstract agent to agent of type Server Agent
	 */
	@Override
	public void onStart() {
		super.onStart();
		this.myServerAgent = (ServerAgent) myAgent;
		this.guid = myServerAgent.getName();
	}

	/**
	 * Method listens for the message coming from the Green Source informing that the power
	 * shortage has finished and that given power job can be supplied again using green source power.
	 */
	@Override
	public void action() {
		final ACLMessage inform = myAgent.receive(SOURCE_POWER_SHORTAGE_FINISH_TEMPLATE);

		if (Objects.nonNull(inform)) {
			final ClientJob job = getJobFromMessage(inform);

			if (Objects.nonNull(job) && POWER_SHORTAGE_STATUSES.contains(myServerAgent.getServerJobs().get(job))) {
				logger.info(GS_SHORTAGE_FINISH_LOG, guid, job.getJobId());
				final AID cloudNetwork = myServerAgent.getOwnerCloudNetworkAgent();
				final ACLMessage informationToCNA =
						preparePowerShortageFinishInformation(JobMapper.mapToJobInstanceId(job), cloudNetwork);

				myServerAgent.getServerJobs().replace(job, getNewJobStatus(job));
				myServerAgent.manage().updateServerGUI();
				displayMessageArrow(myServerAgent, cloudNetwork);
				myServerAgent.send(informationToCNA);
			}
		} else {
			block();
		}
	}

	private ClientJob getJobFromMessage(final ACLMessage message) {
		try {
			final JobInstanceIdentifier jobInstanceIdentifier = getMapper().readValue(message.getContent(),
					JobInstanceIdentifier.class);
			return myServerAgent.manage().getJobByIdAndStartDate(jobInstanceIdentifier);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private JobStatusEnum getNewJobStatus(final ClientJob job) {
		return job.getStartTime().isAfter(getCurrentTime()) ?
				JobStatusEnum.ACCEPTED :
				JobStatusEnum.IN_PROGRESS;
	}
}
