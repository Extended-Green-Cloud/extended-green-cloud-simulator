package com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.handler;

import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.handler.logs.JobHandlingHandlerLog.JOB_DELAY_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.domain.job.JobStatusEnum.IN_PROGRESS;
import static com.greencloud.application.messages.domain.factory.JobStatusMessageFactory.prepareJobStartStatusRequestMessage;

import java.util.Date;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator.InitiateJobStartCheck;
import com.greencloud.application.domain.job.ClientJob;

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
	 * to the server to provide information about the job start
	 */
	@Override
	protected void onWake() {
		final ClientJob job = myCloudNetworkAgent.manage().getJobById(jobId);
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
