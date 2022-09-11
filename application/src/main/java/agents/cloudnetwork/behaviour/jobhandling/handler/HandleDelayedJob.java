package agents.cloudnetwork.behaviour.jobhandling.handler;

import static agents.cloudnetwork.behaviour.jobhandling.handler.logs.JobHandlingHandlerLog.JOB_DELAY_LOG;
import static messages.domain.factory.JobStatusMessageFactory.prepareJobStartStatusRequestMessage;

import java.util.Date;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agents.cloudnetwork.CloudNetworkAgent;
import agents.cloudnetwork.behaviour.jobhandling.initiator.InitiateJobStartCheck;
import domain.job.ClientJob;
import domain.job.JobStatusEnum;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour passes to the client the information that the job execution has some delay.
 */
public class HandleDelayedJob extends WakerBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(HandleDelayedJob.class);

	private final String jobId;
	private final CloudNetworkAgent myCloudNetworkAgent;
	private final String guid;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent     agent which is executing the behaviour
	 * @param startTime time when the behaviour execution should start
	 * @param jobId     unique job identifier
	 */
	public HandleDelayedJob(Agent agent, Date startTime, String jobId) {
		super(agent, startTime);
		this.myCloudNetworkAgent = (CloudNetworkAgent) agent;
		this.jobId = jobId;
		this.guid = myCloudNetworkAgent.getName();
	}

	/**
	 * Method verifies if the job execution has started at the correct time. If there is some delay - it sends the request
	 * to the server to provide information about the job start
	 */
	@Override
	protected void onWake() {
		final ClientJob job = myCloudNetworkAgent.manage().getJobById(jobId);

		if (Objects.nonNull(job) && myCloudNetworkAgent.getServerForJobMap().containsKey(jobId)
				&& !myCloudNetworkAgent.getNetworkJobs().get(job).equals(JobStatusEnum.IN_PROGRESS)) {
			logger.error(JOB_DELAY_LOG, guid);
			final AID server = myCloudNetworkAgent.getServerForJobMap().get(jobId);
			final ACLMessage checkMessage = prepareJobStartStatusRequestMessage(jobId, server);

			myAgent.addBehaviour(new InitiateJobStartCheck(myCloudNetworkAgent, checkMessage, jobId));
		}
	}
}
