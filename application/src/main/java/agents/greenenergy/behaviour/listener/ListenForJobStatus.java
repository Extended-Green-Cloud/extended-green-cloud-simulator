package agents.greenenergy.behaviour.listener;

import static utils.TimeUtils.getCurrentTime;
import static utils.JobMapUtils.getJobByIdAndStartDate;
import static messages.domain.constants.MessageProtocolConstants.FINISH_JOB_PROTOCOL;
import static messages.domain.constants.MessageProtocolConstants.STARTED_JOB_PROTOCOL;
import static domain.job.JobStatusEnum.ACCEPTED;
import static domain.job.JobStatusEnum.IN_PROGRESS;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.or;
import static java.util.Objects.nonNull;
import static mapper.JsonMapper.getMapper;

import agents.greenenergy.GreenEnergyAgent;
import domain.job.JobInstanceIdentifier;
import domain.job.PowerJob;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Behaviour which listens for the information that the execution of the given job starts or finishes
 */
public class ListenForJobStatus extends CyclicBehaviour {
	private static final Logger logger = LoggerFactory.getLogger(ListenForJobStatus.class);
	private static final MessageTemplate messageTemplate = MessageTemplate.and(MatchPerformative(INFORM),
			or(MatchProtocol(FINISH_JOB_PROTOCOL), MatchProtocol(STARTED_JOB_PROTOCOL)));

	private final GreenEnergyAgent myGreenEnergyAgent;
	private final String guid;

	/**
	 * Behaviour constructor.
	 *
	 * @param myGreenEnergyAgent agent which is executing the behaviour
	 */
	public ListenForJobStatus(final GreenEnergyAgent myGreenEnergyAgent) {
		this.myGreenEnergyAgent = myGreenEnergyAgent;
		this.guid = myGreenEnergyAgent.getName();
	}

	/**
	 * Method which listens for the information that the job execution has started/finished. It is responsible
	 * for updating the current green energy source state.
	 */
	@Override
	public void action() {
		final ACLMessage message = myGreenEnergyAgent.receive(messageTemplate);
		if (nonNull(message)) {
			if (message.getProtocol().equals(FINISH_JOB_PROTOCOL)) {
				try {
					final JobInstanceIdentifier jobInstanceId = getMapper().readValue(message.getContent(),
							JobInstanceIdentifier.class);
					if (nonNull(getJobByIdAndStartDate(myGreenEnergyAgent.getPowerJobs(), jobInstanceId))) {
						logger.info("[{}] Finish the execution of the job with id {}", guid, jobInstanceId.getJobId());
						final PowerJob powerJob = getJobByIdAndStartDate(myGreenEnergyAgent.getPowerJobs(), jobInstanceId);
						if (powerJob.getStartTime().isBefore(getCurrentTime())) {
							myGreenEnergyAgent.manage().incrementFinishedJobs(jobInstanceId.getJobId());
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (message.getProtocol().equals(STARTED_JOB_PROTOCOL)) {
				try {
					final JobInstanceIdentifier jobInstanceId = getMapper().readValue(message.getContent(),
							JobInstanceIdentifier.class);
					if (nonNull(getJobByIdAndStartDate(myGreenEnergyAgent.getPowerJobs(), jobInstanceId))) {
						final PowerJob powerJob = getJobByIdAndStartDate(myGreenEnergyAgent.getPowerJobs(), jobInstanceId);
						myGreenEnergyAgent.getPowerJobs().replace(powerJob, ACCEPTED, IN_PROGRESS);
						logger.info("[{}] Started the execution of the job with id {}", guid, jobInstanceId.getJobId());
						myGreenEnergyAgent.manage().incrementStartedJobs(jobInstanceId.getJobId());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			block();
		}
	}
}
