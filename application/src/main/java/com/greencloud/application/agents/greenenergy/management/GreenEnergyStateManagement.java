package com.greencloud.application.agents.greenenergy.management;

import static com.database.knowledge.domain.agent.DataType.GREEN_SOURCE_MONITORING;
import static com.greencloud.application.agents.greenenergy.management.logs.GreenEnergyManagementLog.POWER_JOB_ACCEPTED_LOG;
import static com.greencloud.application.agents.greenenergy.management.logs.GreenEnergyManagementLog.POWER_JOB_FAILED_LOG;
import static com.greencloud.application.agents.greenenergy.management.logs.GreenEnergyManagementLog.POWER_JOB_FINISH_LOG;
import static com.greencloud.application.agents.greenenergy.management.logs.GreenEnergyManagementLog.POWER_JOB_START_LOG;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.utils.JobUtils.calculateExpectedJobEndTime;
import static com.greencloud.application.utils.JobUtils.getJobCount;
import static com.greencloud.application.utils.JobUtils.getJobSuccessRatio;
import static com.greencloud.commons.domain.job.enums.JobExecutionResultEnum.ACCEPTED;
import static com.greencloud.commons.domain.job.enums.JobExecutionResultEnum.FAILED;
import static com.greencloud.commons.domain.job.enums.JobExecutionResultEnum.FINISH;
import static com.greencloud.commons.domain.job.enums.JobExecutionResultEnum.STARTED;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.JOB_ON_HOLD_STATUSES;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.database.knowledge.domain.agent.greensource.GreenSourceMonitoringData;
import com.database.knowledge.domain.agent.greensource.ImmutableGreenSourceMonitoringData;
import com.greencloud.application.agents.AbstractStateManagement;
import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.greenenergy.behaviour.adaptation.InitiateGreenSourceDisconnection;
import com.greencloud.application.agents.greenenergy.behaviour.powersupply.handler.HandleManualPowerSupplyFinish;
import com.greencloud.application.domain.job.JobCounter;
import com.greencloud.application.domain.job.JobDivided;
import com.greencloud.application.domain.job.JobPowerShortageTransfer;
import com.greencloud.commons.domain.job.PowerJob;
import com.greencloud.commons.domain.job.ServerJob;
import com.greencloud.commons.domain.job.enums.JobExecutionResultEnum;
import com.gui.agents.GreenEnergyAgentNode;

import jade.core.AID;

/**
 * Set of methods used to manage the internal state of the green energy agent
 */
public class GreenEnergyStateManagement extends AbstractStateManagement {

	private static final Logger logger = getLogger(GreenEnergyStateManagement.class);

	private final AtomicInteger shortagesAccumulator;
	private final AtomicInteger weatherShortagesCounter;
	private final GreenEnergyAgent greenEnergyAgent;

	/**
	 * Default constructor
	 *
	 * @param greenEnergyAgent - agent representing given source
	 */
	public GreenEnergyStateManagement(GreenEnergyAgent greenEnergyAgent) {
		this.greenEnergyAgent = greenEnergyAgent;
		this.shortagesAccumulator = new AtomicInteger(0);
		this.weatherShortagesCounter = new AtomicInteger(0);
	}

	/**
	 * Method removes a job from Green Source map.
	 * Then it performs post-removal actions that verify if the given Green Source is undergoing disconnection, and
	 * if so - checks if the Green Source can be fully disconnected
	 *
	 * @param job job to be removed
	 */
	public void removeJob(final ServerJob job) {
		greenEnergyAgent.getServerJobs().remove(job);

		if (greenEnergyAgent.adapt().getDisconnectionState().isBeingDisconnectedFromServer()) {
			final AID server = greenEnergyAgent.adapt().getDisconnectionState().getServerToBeDisconnected();
			final boolean isLastJobRemoved = greenEnergyAgent.getServerJobs().keySet().stream()
					.noneMatch(serverJob -> serverJob.getServer().equals(server));

			if (isLastJobRemoved) {
				greenEnergyAgent.addBehaviour(InitiateGreenSourceDisconnection.create(greenEnergyAgent, server));
			}
		}
	}

	/**
	 * Method creates new instances for given server job that will be affected by the power shortage and executes
	 * the post job division handler.
	 *
	 * @param job                job that is to be divided into instances
	 * @param powerShortageStart time when the power shortage will start
	 * @return Pair consisting of previous job instance and job instance for transfer (if there is only job instance
	 * * for transfer then previous job instance element is null)
	 */
	public JobDivided<ServerJob> divideJobForPowerShortage(final ServerJob job, final Instant powerShortageStart) {
		return super.divideJobForPowerShortage(job, powerShortageStart, greenEnergyAgent.getServerJobs());
	}

	/**
	 * Method substitutes existing job instance with new instances associated with power shortage transfer
	 *
	 * @param jobTransfer job transfer information
	 * @param originalJob original job that is to be divided
	 */
	public void divideJobForPowerShortage(final JobPowerShortageTransfer jobTransfer,
			final ServerJob originalJob) {
		super.divideJobForPowerShortage(jobTransfer, originalJob, greenEnergyAgent.getServerJobs());
	}

	@Override
	protected ConcurrentMap<JobExecutionResultEnum, JobCounter> getJobCountersMap() {
		return new ConcurrentHashMap<>(Map.of(
				FAILED, new JobCounter(jobId ->
						logger.info(POWER_JOB_FAILED_LOG, jobCounters.get(FAILED).getCount())),
				ACCEPTED, new JobCounter(jobId ->
						logger.info(POWER_JOB_ACCEPTED_LOG, jobCounters.get(ACCEPTED).getCount())),
				STARTED, new JobCounter(jobId ->
						logger.info(POWER_JOB_START_LOG, jobId, jobCounters.get(STARTED).getCount(),
								jobCounters.get(ACCEPTED).getCount())),
				FINISH, new JobCounter(jobId ->
						logger.info(POWER_JOB_FINISH_LOG, jobId, jobCounters.get(FINISH).getCount(),
								jobCounters.get(STARTED).getCount()))
		));
	}

	@Override
	protected <T extends PowerJob> void processJobDivision(T affectedJob, T nonAffectedJob) {
		incrementJobCounter(mapToJobInstanceId(affectedJob), ACCEPTED);
		greenEnergyAgent.addBehaviour(HandleManualPowerSupplyFinish.create(greenEnergyAgent,
				calculateExpectedJobEndTime(nonAffectedJob), (ServerJob) nonAffectedJob));
	}

	@Override
	protected <T extends PowerJob> void processJobSubstitution(boolean hasStarted, T newJobInstance) {
		greenEnergyAgent.addBehaviour(HandleManualPowerSupplyFinish.create(greenEnergyAgent,
				calculateExpectedJobEndTime(newJobInstance), (ServerJob) newJobInstance));
	}

	@Override
	public void updateGUI() {
		final GreenEnergyAgentNode greenEnergyAgentNode = (GreenEnergyAgentNode) greenEnergyAgent.getAgentNode();

		if (nonNull(greenEnergyAgentNode)) {
			final double successRatio = getJobSuccessRatio(jobCounters.get(ACCEPTED).getCount(),
					jobCounters.get(FAILED).getCount());
			final double energyInUse = greenEnergyAgent.power().getCurrentEnergyInUse();
			final double traffic = energyInUse / greenEnergyAgent.getMaximumGeneratorCapacity();
			final int jobsOnHold = getJobCount(greenEnergyAgent.getServerJobs(), JOB_ON_HOLD_STATUSES);

			greenEnergyAgentNode.updateJobsCount(getJobCount(greenEnergyAgent.getServerJobs()));
			greenEnergyAgentNode.updateJobsOnHoldCount(jobsOnHold);
			greenEnergyAgentNode.updateTraffic(traffic);
			greenEnergyAgentNode.updateEnergyInUse(energyInUse);
			greenEnergyAgentNode.updateIsActive(getIsActiveState());
			greenEnergyAgentNode.updateCurrentJobSuccessRatio(successRatio);
			saveMonitoringData();
		}
	}

	public AtomicInteger getWeatherShortagesCounter() {
		return weatherShortagesCounter;
	}

	public AtomicInteger getShortagesAccumulator() {
		return shortagesAccumulator;
	}

	private void saveMonitoringData() {
		final double successRatio = getJobSuccessRatio(jobCounters.get(ACCEPTED).getCount(),
				jobCounters.get(FAILED).getCount());

		final GreenSourceMonitoringData greenSourceMonitoring = ImmutableGreenSourceMonitoringData.builder()
				.weatherPredictionError(greenEnergyAgent.getWeatherPredictionError())
				.successRatio(successRatio)
				.currentTraffic(greenEnergyAgent.power().getCurrentEnergyInUse()
						/ greenEnergyAgent.getMaximumGeneratorCapacity())
				.isBeingDisconnected(greenEnergyAgent.adapt().getDisconnectionState().isBeingDisconnected())
				.build();
		greenEnergyAgent.writeMonitoringData(GREEN_SOURCE_MONITORING, greenSourceMonitoring);
	}

	private boolean getIsActiveState() {
		return greenEnergyAgent.power().getCurrentEnergyInUse() > 0
				|| getJobCount(greenEnergyAgent.getServerJobs(), JOB_ON_HOLD_STATUSES) > 0;
	}
}
