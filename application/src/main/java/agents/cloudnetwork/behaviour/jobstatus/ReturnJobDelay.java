package agents.cloudnetwork.behaviour.jobstatus;

import static messages.domain.factory.JobStatusMessageFactory.prepareJobStartStatusRequestMessage;
import static utils.JobMapUtils.getJobById;

import agents.cloudnetwork.CloudNetworkAgent;
import domain.job.Job;
import domain.job.JobStatusEnum;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;

import java.util.Date;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Behaviour which is responsible for passing to the client the information that the job execution has some delay.
 */
public class ReturnJobDelay extends WakerBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ReturnJobDelay.class);

	private final String jobId;
	private final CloudNetworkAgent myCloudNetworkAgent;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent     agent which is executing the behaviour
	 * @param startTime time when the behaviour execution should start
	 * @param jobId     unique job identifier
	 */
	public ReturnJobDelay(Agent agent, Date startTime, String jobId) {
		super(agent, startTime);
		this.myCloudNetworkAgent = (CloudNetworkAgent) agent;
		this.jobId = jobId;
	}

	/**
	 * Method verifies if the job execution has started at the correct time. If there is some delay - it sends the request
	 * to the server to provide information about the job start
	 */
	@Override
	protected void onWake() {
		final Job job = getJobById(myCloudNetworkAgent.getNetworkJobs(), jobId);
		if (Objects.nonNull(job) && !myCloudNetworkAgent.getNetworkJobs().get(job).equals(JobStatusEnum.IN_PROGRESS) &&
				myCloudNetworkAgent.getServerForJobMap().containsKey(jobId)) {
			final AID server = myCloudNetworkAgent.getServerForJobMap().get(job.getJobId());
			logger.error("[{}] There is no message regarding the job start. Sending request to the server",
					myAgent.getName());
			myAgent.addBehaviour(
					new RequestJobStartStatus(myCloudNetworkAgent, prepareJobStartStatusRequestMessage(jobId, server),
							jobId));
			myAgent.removeBehaviour(this);
		}
	}
}
