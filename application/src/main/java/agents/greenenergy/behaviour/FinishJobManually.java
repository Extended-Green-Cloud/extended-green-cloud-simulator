package agents.greenenergy.behaviour;

import static utils.GUIUtils.displayMessageArrow;
import static utils.JobMapUtils.getJobByIdAndStartDate;
import static domain.job.JobStatusEnum.ACCEPTED_JOB_STATUSES;
import static messages.domain.factory.JobStatusMessageFactory.prepareManualFinishMessageForServer;

import agents.greenenergy.GreenEnergyAgent;
import domain.job.JobInstanceIdentifier;
import domain.job.PowerJob;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Objects;

/**
 * Behaviour responsible for verifying if the job was finished at the correct time.
 */
public class FinishJobManually extends WakerBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(FinishJobManually.class);

	private final JobInstanceIdentifier jobInstanceId;
	private final GreenEnergyAgent myGreenEnergyAgent;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent         agent which is executing the behaviour
	 * @param endDate       date when the job execution should finish
	 * @param jobInstanceId unique job instance identifier
	 */
	public FinishJobManually(Agent agent, Date endDate, JobInstanceIdentifier jobInstanceId) {
		super(agent, endDate);
		this.myGreenEnergyAgent = (GreenEnergyAgent) agent;
		this.jobInstanceId = jobInstanceId;
	}

	/**
	 * Method verifies if the job execution finished correctly. If there was no information about
	 * job finish execution but the timout passed - the Green Source finishes the power supply manually and sends
	 * the warning to the Server Agent.
	 */
	@Override
	protected void onWake() {
		final PowerJob job = getJobByIdAndStartDate(myGreenEnergyAgent.getPowerJobs(), jobInstanceId.getJobId(), jobInstanceId.getStartTime());
		if (Objects.nonNull(job) && ACCEPTED_JOB_STATUSES.contains(myGreenEnergyAgent.getPowerJobs().get(job))) {
			logger.error("[{}] The power delivery should be finished! Finishing power delivery by hand.",
					myAgent.getName());
			myGreenEnergyAgent.getPowerJobs().remove(job);
			myGreenEnergyAgent.manage().incrementFinishedJobs(job.getJobId());
			displayMessageArrow(myGreenEnergyAgent, myGreenEnergyAgent.getOwnerServer());
			myAgent.send(prepareManualFinishMessageForServer(jobInstanceId, myGreenEnergyAgent.getOwnerServer()));
		}
		myAgent.removeBehaviour(this);
	}
}
