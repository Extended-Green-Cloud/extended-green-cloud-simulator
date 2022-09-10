package agents.cloudnetwork.behaviour.jobstatus;

import static messages.domain.constants.MessageProtocolConstants.DELAYED_JOB_PROTOCOL;
import static messages.domain.constants.MessageProtocolConstants.STARTED_JOB_PROTOCOL;
import static domain.job.JobStatusEnum.IN_PROGRESS;
import static messages.domain.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static utils.JobMapUtils.getJobById;

import agents.cloudnetwork.CloudNetworkAgent;
import domain.job.Job;
import domain.job.JobStatusEnum;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Behaviour is responsible for retreiving the start job status from the server agent
 */
public class RequestJobStartStatus extends AchieveREInitiator {

	private static final Logger logger = LoggerFactory.getLogger(RequestJobStartStatus.class);

	private final CloudNetworkAgent myCloudNetwork;
	private final String jobId;

	/**
	 * Behaviour constructor
	 *
	 * @param agent agent executing the behaviour
	 * @param msg   request that is to be sent to the server agent
	 * @param jobId unique identifier of the job of interest
	 */
	public RequestJobStartStatus(Agent agent, ACLMessage msg, String jobId) {
		super(agent, msg);
		this.myCloudNetwork = (CloudNetworkAgent) agent;
		this.jobId = jobId;
	}

	/**
	 * Method handles the agreement message confirming that the job execution has started. It sends the confirmation
	 * to the client
	 *
	 * @param agree server agreement message
	 */
	@Override
	protected void handleAgree(ACLMessage agree) {
		final Job job = getJobById(myCloudNetwork.getNetworkJobs(), jobId);
		if (Objects.nonNull(job) && !myCloudNetwork.getNetworkJobs().get(job).equals(JobStatusEnum.IN_PROGRESS)) {
			logger.info(
					"[{}] Received job started confirmation. Sending information that the job {} execution has started",
					myAgent.getName(), jobId);
			myCloudNetwork.getNetworkJobs().replace(getJobById(myCloudNetwork.getNetworkJobs(), jobId), IN_PROGRESS);
			myCloudNetwork.manage().incrementStartedJobs(jobId);
			myAgent.send(prepareJobStatusMessageForClient(job.getClientIdentifier(), STARTED_JOB_PROTOCOL));
		}
	}

	/**
	 * Method handles the refusal message informing that the job execution has not started. It sends the delay message
	 * to the client
	 *
	 * @param refuse server refusal message
	 */
	@Override
	protected void handleRefuse(ACLMessage refuse) {
		final Job job = getJobById(myCloudNetwork.getNetworkJobs(), jobId);
		if (Objects.nonNull(job) && !myCloudNetwork.getNetworkJobs().get(job).equals(JobStatusEnum.IN_PROGRESS)) {
			logger.error("[{}] The job {} execution hasn't started yet. Sending delay information to client",
					myAgent.getName(), jobId);
			myAgent.send(prepareJobStatusMessageForClient(job.getClientIdentifier(), DELAYED_JOB_PROTOCOL));
		}
	}
}
