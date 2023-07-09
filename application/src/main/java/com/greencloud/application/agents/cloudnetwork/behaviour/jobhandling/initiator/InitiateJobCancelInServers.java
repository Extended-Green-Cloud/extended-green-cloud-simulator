package com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator;

import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.CANCEL_JOB_ALL_RESPONSES;
import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.CANCEL_JOB_IN_CNA;
import static com.greencloud.application.agents.cloudnetwork.behaviour.jobhandling.initiator.logs.JobHandlingInitiatorLog.CANCEL_JOB_IN_CNA_NOT_FOUND;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.messages.factory.JobStatusMessageFactory.prepareJobCancellationMessage;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.domain.job.enums.JobExecutionResultEnum.FINISH;
import static com.greencloud.commons.domain.job.enums.JobExecutionStateEnum.PRE_EXECUTION;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.ACCEPTED;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.greencloud.application.agents.cloudnetwork.CloudNetworkAgent;
import com.greencloud.application.behaviours.initiator.AbstractCancelInitiator;
import com.greencloud.commons.domain.job.ClientJob;
import com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour initiates job cancellation in owned Servers
 */
public class InitiateJobCancelInServers extends AbstractCancelInitiator<ClientJob> {

	private static final Logger logger = getLogger(InitiateJobCancelInServers.class);

	private final CloudNetworkAgent myCloudNetwork;

	public InitiateJobCancelInServers(final CloudNetworkAgent agent, final ACLMessage cancel,
			final List<ClientJob> jobsToCancel, final ACLMessage originalRequest) {
		super(agent, cancel, jobsToCancel, originalRequest);

		this.myCloudNetwork = agent;
	}

	/**
	 * Method creates the behaviour
	 *
	 * @param agent         agent executing the behaviour
	 * @param originalJobId identifier of job before split
	 * @param jobsToCancel  list of jobs that are to be cancelled
	 * @return InitiateJobCancelInServers
	 */
	public static InitiateJobCancelInServers create(final CloudNetworkAgent agent, final String originalJobId,
			final List<ClientJob> jobsToCancel, final ACLMessage originalRequest) {
		final ACLMessage cancel = prepareJobCancellationMessage(originalJobId,
				agent.getOwnedServers().keySet().toArray(new AID[0]));
		return new InitiateJobCancelInServers(agent, cancel, new ArrayList<>(jobsToCancel), originalRequest);
	}

	/**
	 * Method logs message after receiving all responses
	 */
	@Override
	public void postCancellation() {
		logger.info(CANCEL_JOB_ALL_RESPONSES, jobsToCancel.size());
	}

	/**
	 * Method removes job from the Cloud Network and updates its internal state.
	 *
	 * @param job job that is to be cancelled
	 */
	@Override
	public void handleJobCanceling(final ClientJob job) {
		MDC.put(MDC_JOB_ID, job.getJobId());
		logger.info(CANCEL_JOB_IN_CNA, job.getJobId());

		final JobExecutionStatusEnum jobStatus = myCloudNetwork.getNetworkJobs().get(job);
		myCloudNetwork.getNetworkJobs().remove(job);
		myCloudNetwork.getServerForJobMap().remove(job.getJobId());

		try {
			if (!PRE_EXECUTION.getStatuses().contains(jobStatus)) {
				if (!jobStatus.equals(ACCEPTED)) {
					myCloudNetwork.manage().incrementJobCounter(mapToJobInstanceId(job), FINISH);
				} else {
					myCloudNetwork.getGuiController().updateAllJobsCountByValue(-1);
				}
			}
		} catch (NullPointerException e) {
			logger.error(CANCEL_JOB_IN_CNA_NOT_FOUND, job.getJobId());
		}
	}
}
