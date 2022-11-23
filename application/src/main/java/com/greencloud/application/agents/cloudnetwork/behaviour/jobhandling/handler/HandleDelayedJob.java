package com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.handler;

import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.handler.logs.JobHandlingHandlerLog.JOB_DELAY_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.IN_PROGRESS;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobStartStatusRequestMessage;
import static com.greencloud.application.utils.JobUtils.getJobById;

import java.util.Date;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator.InitiateJobStartCheck;
import com.greencloud.commons.job.ClientJob;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour verifies if the job execution has started on time
 */
public class HandleDelayedJob extends WakerBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(HandleDelayedJob.class);

	private final String jobId;
	private final CloudNetworkAgent myCloudNetworkAgent;

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
	}

	/**
	 * Method verifies if the job execution has started at the correct time. If there is some delay - it sends the request
	 * to the Server to provide information about the job start
	 */
	@Override
	protected void onWake() {
		final ClientJob job = getJobById(jobId, myCloudNetworkAgent.getNetworkJobs());
		MDC.put(MDC_JOB_ID, jobId);

		if (Objects.nonNull(job) && myCloudNetworkAgent.getServerForJobMap().containsKey(jobId)
				&& !myCloudNetworkAgent.getNetworkJobs().get(job).equals(IN_PROGRESS)) {
			logger.error(JOB_DELAY_LOG);
			final AID server = myCloudNetworkAgent.getServerForJobMap().get(jobId);
			final ACLMessage checkMessage = prepareJobStartStatusRequestMessage(jobId, server);

			myAgent.addBehaviour(new InitiateJobStartCheck(myCloudNetworkAgent, checkMessage, jobId));
		}
	}
}
