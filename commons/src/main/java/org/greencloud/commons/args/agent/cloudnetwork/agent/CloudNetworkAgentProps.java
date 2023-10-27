package org.greencloud.commons.args.agent.cloudnetwork.agent;

import static org.greencloud.commons.args.agent.AgentType.CLOUD_NETWORK;
import static org.greencloud.commons.args.agent.cloudnetwork.agent.logs.CloudNetworkAgentPropsLog.COUNT_JOB_ACCEPTED_LOG;
import static org.greencloud.commons.args.agent.cloudnetwork.agent.logs.CloudNetworkAgentPropsLog.COUNT_JOB_FINISH_LOG;
import static org.greencloud.commons.args.agent.cloudnetwork.agent.logs.CloudNetworkAgentPropsLog.COUNT_JOB_PROCESS_LOG;
import static org.greencloud.commons.args.agent.cloudnetwork.agent.logs.CloudNetworkAgentPropsLog.COUNT_JOB_START_LOG;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.ACCEPTED;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FAILED;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.FINISH;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.STARTED;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.greencloud.commons.args.agent.egcs.agent.EGCSAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.counter.JobCounter;
import org.greencloud.commons.enums.job.JobExecutionResultEnum;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.slf4j.Logger;

import jade.core.AID;
import lombok.Getter;
import lombok.Setter;

/**
 * Arguments representing internal properties of Cloud Network Agent
 */
@Getter
@Setter
public class CloudNetworkAgentProps extends EGCSAgentProps {

	private static final Logger logger = getLogger(CloudNetworkAgentProps.class);

	protected ConcurrentMap<ClientJob, JobExecutionStatusEnum> networkJobs;
	protected ConcurrentMap<String, Integer> strategyForJob;
	protected ConcurrentMap<String, AID> serverForJobMap;
	protected ConcurrentMap<AID, Boolean> ownedServers;
	protected ConcurrentMap<AID, Integer> weightsForServersMap;
	protected AID scheduler;

	/**
	 * Constructor that initialize Cloud Network Agent state to initial values
	 *
	 * @param agentName name of the agent
	 */
	public CloudNetworkAgentProps(final String agentName) {
		super(CLOUD_NETWORK, agentName);

		this.serverForJobMap = new ConcurrentHashMap<>();
		this.networkJobs = new ConcurrentHashMap<>();
		this.ownedServers = new ConcurrentHashMap<>();
		this.strategyForJob = new ConcurrentHashMap<>();
		this.weightsForServersMap = new ConcurrentHashMap<>();
	}

	/**
	 * Method retrieves list of owned servers that are active
	 *
	 * @return list of server AIDs
	 */
	public List<AID> getOwnedActiveServers() {
		return ownedServers.entrySet().stream()
				.filter(Map.Entry::getValue)
				.map(Map.Entry::getKey)
				.toList();
	}

	/**
	 * Method adds new client job
	 *
	 * @param job      job that is to be added
	 * @param strategy strategy with which the job is to be handled
	 * @param status   status of the job
	 */
	public void addJob(final ClientJob job, final Integer strategy, final JobExecutionStatusEnum status) {
		networkJobs.put(job, status);
		strategyForJob.put(job.getJobInstanceId(), strategy);
	}

	/**
	 * Method removes client job
	 *
	 * @param job job that is to be removed
	 */
	public int removeJob(final ClientJob job) {
		networkJobs.remove(job);
		return strategyForJob.remove(job.getJobInstanceId());
	}

	@Override
	protected ConcurrentMap<JobExecutionResultEnum, JobCounter> getJobCountersMap() {
		return new ConcurrentHashMap<>(Map.of(
				FAILED, new JobCounter(jobId ->
						logger.info(COUNT_JOB_PROCESS_LOG, jobCounters.get(FAILED).getCount())),
				ACCEPTED, new JobCounter(jobId ->
						logger.info(COUNT_JOB_ACCEPTED_LOG, jobCounters.get(ACCEPTED).getCount())),
				STARTED, new JobCounter(jobId ->
						logger.info(COUNT_JOB_START_LOG, jobId, jobCounters.get(STARTED).getCount(),
								jobCounters.get(ACCEPTED).getCount())),
				FINISH, new JobCounter(jobId ->
						logger.info(COUNT_JOB_FINISH_LOG, jobId, jobCounters.get(FINISH).getCount(),
								jobCounters.get(STARTED).getCount()))
		));
	}

	@Override
	public void updateGUI() {
		super.updateGUI();
		saveMonitoringData();
	}
}
