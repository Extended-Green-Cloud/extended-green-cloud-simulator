package agents.cloudnetwork.behaviour.jobhandling.initiator;

import static agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.JOB_HAS_NOT_STARTED_LOG;
import static agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.JOB_HAS_STARTED_LOG;
import static agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.JOB_STATUS_IS_CHECKED_LOG;
import static domain.job.JobStatusEnum.IN_PROGRESS;
import static messages.domain.constants.MessageProtocolConstants.DELAYED_JOB_PROTOCOL;
import static messages.domain.constants.MessageProtocolConstants.STARTED_JOB_PROTOCOL;
import static messages.domain.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agents.cloudnetwork.CloudNetworkAgent;
import domain.job.ClientJob;
import domain.job.JobStatusEnum;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Behaviour retrieves the job start status from the server agent
 */
public class InitiateJobStartCheck extends AchieveREInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiateJobStartCheck.class);

	private final CloudNetworkAgent myCloudNetwork;
	private final String guid;
	private final String jobId;

	/**
	 * Behaviour constructor
	 *
	 * @param agent agent executing the behaviour
	 * @param msg   request that is to be sent to the server agent
	 * @param jobId unique identifier of the job of interest
	 */
	public InitiateJobStartCheck(Agent agent, ACLMessage msg, String jobId) {
		super(agent, msg);
		this.myCloudNetwork = (CloudNetworkAgent) agent;
		this.jobId = jobId;
		this.guid = myCloudNetwork.getName();
	}

	/**
	 * Method handles the AGREE message informing that the request is being processed by the server
	 *
	 * @param agree server agreement message
	 */
	@Override
	protected void handleAgree(ACLMessage agree) {
		logger.info(JOB_STATUS_IS_CHECKED_LOG, guid, jobId);
	}

	/**
	 * Method handles the INFORM message confirming that the job execution has started.
	 * It sends the confirmation to the client
	 *
	 * @param inform server inform message
	 */
	@Override
	protected void handleInform(ACLMessage inform) {
		final ClientJob job = myCloudNetwork.manage().getJobById(jobId);
		if (Objects.nonNull(job) && !myCloudNetwork.getNetworkJobs().get(job).equals(JobStatusEnum.IN_PROGRESS)) {
			logger.info(JOB_HAS_STARTED_LOG, guid, jobId);

			myCloudNetwork.getNetworkJobs().replace(myCloudNetwork.manage().getJobById(jobId), IN_PROGRESS);
			myCloudNetwork.manage().incrementStartedJobs(jobId);
			myAgent.send(prepareJobStatusMessageForClient(job.getClientIdentifier(), STARTED_JOB_PROTOCOL));
		}
	}

	/**
	 * Method handles the FAILURE message informing that the job execution has not started.
	 * It sends the delay message to the client
	 *
	 * @param failure failure message
	 */
	@Override
	protected void handleFailure(ACLMessage failure) {
		final ClientJob job = myCloudNetwork.manage().getJobById(jobId);
		if (Objects.nonNull(job) && !myCloudNetwork.getNetworkJobs().get(job).equals(JobStatusEnum.IN_PROGRESS)) {
			logger.error(JOB_HAS_NOT_STARTED_LOG, guid, jobId);
			myAgent.send(prepareJobStatusMessageForClient(job.getClientIdentifier(), DELAYED_JOB_PROTOCOL));
		}
	}
}
