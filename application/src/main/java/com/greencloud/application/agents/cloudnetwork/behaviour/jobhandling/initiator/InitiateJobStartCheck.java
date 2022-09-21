package com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator;

import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.JOB_HAS_NOT_STARTED_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.JOB_HAS_STARTED_LOG;
import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.JOB_STATUS_IS_CHECKED_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.domain.job.JobStatusEnum.IN_PROGRESS;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.DELAYED_JOB_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.STARTED_JOB_PROTOCOL;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import com.greencloud.application.domain.job.Job;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Behaviour retrieves the job start status from the server agent
 */
public class InitiateJobStartCheck extends AchieveREInitiator {

	private static final Logger logger = LoggerFactory.getLogger(InitiateJobStartCheck.class);

	private final CloudNetworkAgent myCloudNetwork;
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
	}

	/**
	 * Method handles the AGREE message informing that the request is being processed by the server
	 *
	 * @param agree server agreement message
	 */
	@Override
	protected void handleAgree(ACLMessage agree) {
		MDC.put(MDC_JOB_ID, jobId);
		logger.info(JOB_STATUS_IS_CHECKED_LOG, jobId);
	}

	/**
	 * Method handles the INFORM message confirming that the job execution has started.
	 * It sends the confirmation to the client
	 *
	 * @param inform server inform message
	 */
	@Override
	protected void handleInform(ACLMessage inform) {
		final Job job = myCloudNetwork.manage().getJobById(jobId);
		MDC.put(MDC_JOB_ID, jobId);
		if (Objects.nonNull(job) && !myCloudNetwork.getNetworkJobs().get(job).equals(IN_PROGRESS)) {
			logger.info(JOB_HAS_STARTED_LOG, jobId);

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
		final Job job = myCloudNetwork.manage().getJobById(jobId);
		MDC.put(MDC_JOB_ID, jobId);
		if (Objects.nonNull(job) && !myCloudNetwork.getNetworkJobs().get(job).equals(IN_PROGRESS)) {
			logger.error(JOB_HAS_NOT_STARTED_LOG, jobId);
			myAgent.send(prepareJobStatusMessageForClient(job.getClientIdentifier(), DELAYED_JOB_PROTOCOL));
		}
	}
}
