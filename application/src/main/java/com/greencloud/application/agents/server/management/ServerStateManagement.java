package com.greencloud.application.agents.server.management;

import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static com.greencloud.application.agents.server.constants.ServerAgentConstants.MAX_AVAILABLE_POWER_DIFFERENCE;
import static com.greencloud.application.agents.server.management.logs.ServerManagementLog.COUNT_JOB_ACCEPTED_LOG;
import static com.greencloud.application.agents.server.management.logs.ServerManagementLog.COUNT_JOB_FINISH_LOG;
import static com.greencloud.application.agents.server.management.logs.ServerManagementLog.COUNT_JOB_PROCESS_LOG;
import static com.greencloud.application.agents.server.management.logs.ServerManagementLog.COUNT_JOB_START_LOG;
import static com.greencloud.application.agents.server.management.logs.ServerManagementLog.SUPPLY_JOB_WITH_BACK_UP;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.messages.factory.JobStatusMessageFactory.prepareJobFinishMessage;
import static com.greencloud.application.utils.JobUtils.getJobById;
import static com.greencloud.application.utils.JobUtils.getJobCount;
import static com.greencloud.application.utils.JobUtils.getJobSuccessRatio;
import static com.greencloud.application.utils.JobUtils.isJobStarted;
import static com.greencloud.application.utils.JobUtils.isJobUnique;
import static com.greencloud.application.utils.TimeUtils.differenceInHours;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.application.utils.TimeUtils.isWithinTimeStamp;
import static com.greencloud.commons.constants.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.commons.domain.job.enums.JobExecutionResultEnum.ACCEPTED;
import static com.greencloud.commons.domain.job.enums.JobExecutionResultEnum.FAILED;
import static com.greencloud.commons.domain.job.enums.JobExecutionResultEnum.FINISH;
import static com.greencloud.commons.domain.job.enums.JobExecutionResultEnum.STARTED;
import static com.greencloud.commons.domain.job.enums.JobExecutionStateEnum.EXECUTING_ON_BACK_UP;
import static com.greencloud.commons.domain.job.enums.JobExecutionStateEnum.EXECUTING_ON_HOLD_SOURCE;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.ACCEPTED_JOB_STATUSES;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.IN_PROGRESS_BACKUP_ENERGY;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.JOB_ON_HOLD_STATUSES;
import static com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum.ON_HOLD_SOURCE_SHORTAGE;
import static java.lang.Math.signum;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.Serializable;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;

import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.MDC;

import com.database.knowledge.domain.agent.server.ImmutableServerMonitoringData;
import com.database.knowledge.domain.agent.server.ServerMonitoringData;
import com.greencloud.application.agents.AbstractStateManagement;
import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.agents.server.behaviour.adaptation.handler.HandleServerDisabling;
import com.greencloud.application.agents.server.behaviour.jobexecution.handler.HandleJobFinish;
import com.greencloud.application.agents.server.behaviour.jobexecution.handler.HandleJobStart;
import com.greencloud.application.domain.agent.GreenSourceData;
import com.greencloud.application.domain.job.JobCounter;
import com.greencloud.application.domain.job.JobDivided;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.job.JobPowerShortageTransfer;
import com.greencloud.application.exception.JobNotFoundException;
import com.greencloud.commons.domain.job.ClientJob;
import com.greencloud.commons.domain.job.PowerJob;
import com.greencloud.commons.domain.job.enums.JobExecutionResultEnum;
import com.greencloud.commons.domain.job.enums.JobExecutionStateEnum;
import com.greencloud.commons.domain.job.enums.JobExecutionStatusEnum;
import com.greencloud.commons.domain.resources.HardwareResources;
import com.gui.agents.ServerAgentNode;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/**
 * Set of utilities used to manage the internal state of the server agent
 */
public class ServerStateManagement extends AbstractStateManagement implements Serializable {

	private static final Logger logger = getLogger(ServerStateManagement.class);
	private final ServerAgent serverAgent;

	public ServerStateManagement(ServerAgent serverAgent) {
		this.serverAgent = serverAgent;
	}

	/**
	 * Method defines comparator used to evaluate offers for job execution proposed by 2 Green Sources
	 *
	 * @return method comparator returns:
	 * <p> val > 0 - if the offer1 is better</p>
	 * <p> val = 0 - if both offers are equivalently good</p>
	 * <p> val < 0 - if the offer2 is better</p>
	 */
	public BiFunction<ACLMessage, ACLMessage, Integer> offerComparator() {
		return (BiFunction<ACLMessage, ACLMessage, Integer> & Serializable) (offer1, offer2) -> {
			if (serverAgent.getWeightsForGreenSourcesMap().containsKey(offer1.getSender()) &&
					serverAgent.getWeightsForGreenSourcesMap().containsKey(offer2.getSender())) {
				final int weight1 = serverAgent.getWeightsForGreenSourcesMap().get(offer1.getSender());
				final int weight2 = serverAgent.getWeightsForGreenSourcesMap().get(offer2.getSender());

				final Comparator<GreenSourceData> comparator = (gs1Data, gs2Data) -> {
					double powerDifference =
							gs1Data.getAvailablePowerInTime() * weight2 - gs2Data.getAvailablePowerInTime() * weight1;
					double errorDifference = (gs1Data.getPowerPredictionError() - gs2Data.getPowerPredictionError());
					int priceDifference = (int) (gs1Data.getPriceForEnergySupply() - gs2Data.getPriceForEnergySupply());

					return (int) (errorDifference != 0 ? signum(errorDifference) :
							MAX_AVAILABLE_POWER_DIFFERENCE.isValidValue((long) powerDifference) ?
									priceDifference :
									signum(powerDifference));
				};
				return compareReceivedOffers(offer1, offer2, GreenSourceData.class, comparator);
			}
			return 0;
		};
	}

	/**
	 * Method calculates the price for executing the job by given green source and server
	 *
	 * @param greenSourceData green source executing the job
	 * @return full price
	 */
	public double calculateServicePrice(final GreenSourceData greenSourceData) {
		final ClientJob job = getJobById(greenSourceData.getJobId(), serverAgent.getServerJobs());

		if (nonNull(job)) {
			final double computingCost =
					differenceInHours(job.getStartTime(), job.getEndTime()) * serverAgent.getPricePerHour();
			return greenSourceData.getPriceForEnergySupply() + computingCost;
		} else {
			throw new JobNotFoundException();
		}
	}

	/**
	 * Method creates new instances for given server job that will be affected by the internal server error and executes
	 * the post job division handler.
	 *
	 * @param job                job that is to be divided into instances
	 * @param powerShortageStart time when the server failure will start
	 * @return Pair consisting of previous job instance and job instance for transfer (if there is only job instance
	 * * for transfer then previous job instance element is null)
	 */
	public JobDivided<ClientJob> divideJobForTransfer(final ClientJob job, final Instant powerShortageStart) {
		return super.divideJobForPowerShortage(job, powerShortageStart, serverAgent.getServerJobs());
	}

	/**
	 * Method substitutes existing job instance with new instances associated with internal server error job transfer
	 *
	 * @param jobTransfer job transfer information
	 * @param originalJob original job that is to be divided
	 * @return Pair of new job instances
	 */
	public JobDivided<ClientJob> divideJobForTransfer(final JobPowerShortageTransfer jobTransfer,
			final ClientJob originalJob) {
		return super.divideJobForPowerShortage(jobTransfer, originalJob, serverAgent.getServerJobs());
	}

	/**
	 * Method updates the client number
	 */
	public void updateClientNumberGUI() {
		final ServerAgentNode serverAgentNode = (ServerAgentNode) serverAgent.getAgentNode();
		if (nonNull(serverAgentNode)) {
			serverAgentNode.updateClientNumber(getJobCount(serverAgent.getServerJobs(), ACCEPTED_JOB_STATUSES));
		}
	}

	/**
	 * Method retrieves the addresses of green sources that are marked as active
	 *
	 * @return set of active green sources
	 */
	public Set<AID> getOwnedActiveGreenSources() {
		return serverAgent.getOwnedGreenSources().entrySet().stream()
				.filter(Map.Entry::getValue)
				.map(Map.Entry::getKey)
				.collect(toSet());
	}

	/**
	 * Method sends an update of the state of the server in the database
	 */
	public void writeStateToDatabase() {
		final double greenPowerUsage = serverAgent.resources().getCPUUsage(null);
		final double backPowerUsage = serverAgent.resources().getCPUUsage(singleton(IN_PROGRESS_BACKUP_ENERGY));
		final int jobsNo = serverAgent.getServerJobs().size() - getJobCount(serverAgent.getServerJobs(),
				JOB_ON_HOLD_STATUSES);
		final double successRatio = getJobSuccessRatio(jobCounters.get(ACCEPTED).getCount(),
				jobCounters.get(FAILED).getCount());

		final ServerMonitoringData serverMonitoringData = ImmutableServerMonitoringData.builder()
				.idlePowerConsumption(serverAgent.getIdlePowerConsumption())
				.currentPowerConsumption(serverAgent.resources().getCurrentPowerConsumption())
				.currentTraffic(greenPowerUsage)
				.currentBackUpPowerTraffic(backPowerUsage)
				.serverJobs(jobsNo)
				.successRatio(successRatio)
				.isDisabled(serverAgent.isDisabled())
				.build();

		serverAgent.writeMonitoringData(SERVER_MONITORING, serverMonitoringData);
	}

	/**
	 * Method performs job finishing action
	 *
	 * @param jobToFinish job to be finished
	 * @param informCNA   flag indicating whether cloud network should be informed about the job finish
	 */
	public void finishJobExecution(final ClientJob jobToFinish, final boolean informCNA) {
		finishJobExecutionWithResult(jobToFinish, informCNA, FINISH);
	}

	/**
	 * Method performs job finishing action with a specified state
	 *
	 * @param jobToFinish job to be finished
	 * @param informCNA   flag indicating whether cloud network should be informed about the job finish
	 */
	public void finishJobExecutionWithResult(final ClientJob jobToFinish, final boolean informCNA,
			final JobExecutionResultEnum resultType) {
		if (nonNull(serverAgent.getGreenSourceForJobMap().get(jobToFinish.getJobId()))) {
			final JobExecutionStatusEnum jobStatus = serverAgent.getServerJobs().get(jobToFinish);

			sendFinishInformation(jobToFinish, informCNA);
			updateStateAfterJobIsDone(jobToFinish, resultType);

			if (EXECUTING_ON_BACK_UP.getStatuses().contains(jobStatus)) {
				supplyJobsWithBackupPower();
			}
		}
	}

	/**
	 * Method updates the job state based on the given state fields that include:
	 * <p> first field - new state enum </p>
	 * <p> second field - message to be logged </p>
	 * <p> third field - status information to be passed to CNA</p>
	 *
	 * @param newStateFields triple containing fields associated with new state
	 * @param job            job of interest
	 */
	public void handleJobStateChange(final Triple<JobExecutionStateEnum, String, String> newStateFields,
			final ClientJob job) {
		final boolean hasStarted = isJobStarted(job, serverAgent.getServerJobs());

		logger.info(newStateFields.getMiddle(), job.getJobId());
		serverAgent.getServerJobs().replace(job, newStateFields.getLeft().getStatus(hasStarted));

		if (hasStarted) {
			serverAgent.message().informCNAAboutStatusChange(mapToJobInstanceId(job), newStateFields.getRight());
		}
		serverAgent.manage().updateGUI();
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
	protected <T extends PowerJob> void processJobDivision(T affectedJob, T nonAffectedJob) {
		incrementJobCounter(mapToJobInstanceId(affectedJob), ACCEPTED);
		serverAgent.addBehaviour(HandleJobStart.createFor(serverAgent, (ClientJob) affectedJob, false, true));
		serverAgent.addBehaviour(HandleJobFinish.createFor(serverAgent, (ClientJob) nonAffectedJob, false));
		if (!isJobStarted((ClientJob) nonAffectedJob, serverAgent.getServerJobs())) {
			serverAgent.addBehaviour(
					HandleJobStart.createFor(serverAgent, (ClientJob) nonAffectedJob, true, false));
		}
	}

	@Override
	protected <T extends PowerJob> void processJobSubstitution(final boolean hasStarted, T newJobInstance) {
		if (hasStarted) {
			serverAgent.addBehaviour(HandleJobFinish.createFor(serverAgent, (ClientJob) newJobInstance, true));
		} else {
			serverAgent.addBehaviour(
					HandleJobStart.createFor(serverAgent, (ClientJob) newJobInstance, true, true));
		}
	}

	@Override
	public void updateGUI() {
		final ServerAgentNode serverAgentNode = (ServerAgentNode) serverAgent.getAgentNode();

		if (nonNull(serverAgentNode)) {
			final double successRatio =
					getJobSuccessRatio(jobCounters.get(ACCEPTED).getCount(), jobCounters.get(FAILED).getCount());
			final double backUpTraffic = serverAgent.resources().getCPUUsage(singleton(IN_PROGRESS_BACKUP_ENERGY));
			final HardwareResources inUseResources = serverAgent.resources().getInUseResources();
			final double powerConsumption = serverAgent.resources().getCurrentPowerConsumption();
			final double powerConsumptionBackUp = serverAgent.resources().getCurrentPowerConsumptionBackUp();

			serverAgentNode.updateTraffic(serverAgent.resources().getCPUUsage(null));
			serverAgentNode.updateBackUpTraffic(backUpTraffic);
			serverAgentNode.updateResources(inUseResources, powerConsumption, powerConsumptionBackUp);
			serverAgentNode.updateJobsCount(getJobCount(serverAgent.getServerJobs()));
			serverAgentNode.updateJobsOnHoldCount(getJobCount(serverAgent.getServerJobs(), JOB_ON_HOLD_STATUSES));
			serverAgentNode.updateClientNumber(getJobCount(serverAgent.getServerJobs(), ACCEPTED_JOB_STATUSES));
			serverAgentNode.updateIsActive(getIsActiveState());
			serverAgentNode.updateCurrentJobSuccessRatio(successRatio);
			writeStateToDatabase();
		}
	}

	private void sendFinishInformation(final ClientJob job, final boolean informCNA) {
		final AID greenSource = serverAgent.getGreenSourceForJobMap().get(job.getJobId());
		final List<AID> receivers = informCNA ?
				List.of(greenSource, serverAgent.getOwnerCloudNetworkAgent()) :
				singletonList(greenSource);
		serverAgent.send(prepareJobFinishMessage(job, receivers.toArray(new AID[0])));
	}

	private void updateStateAfterJobIsDone(final ClientJob jobToBeDone, final JobExecutionResultEnum result) {
		final JobInstanceIdentifier jobInstance = mapToJobInstanceId(jobToBeDone);
		final boolean hasJobStarted = result.equals(FINISH) && isJobStarted(jobToBeDone, serverAgent.getServerJobs());

		if (result.equals(FAILED) || hasJobStarted) {
			incrementJobCounter(jobInstance, result);
		}

		if (isJobUnique(jobToBeDone.getJobId(), serverAgent.getServerJobs())) {
			serverAgent.getGreenSourceForJobMap().remove(jobToBeDone.getJobId());
			updateClientNumberGUI();
		}
		serverAgent.getServerJobs().remove(jobToBeDone);

		if (serverAgent.isDisabled() && serverAgent.getServerJobs().size() == 0) {
			serverAgent.addBehaviour(new HandleServerDisabling());
		}
		updateGUI();
	}

	private void supplyJobsWithBackupPower() {
		final Map<ClientJob, JobExecutionStatusEnum> jobEntries = serverAgent.getServerJobs().entrySet().stream()
				.filter(job -> isWithinTimeStamp(job.getKey().getStartTime(), job.getKey().getEndTime(),
						getCurrentTime()))
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

		jobEntries.entrySet().stream()
				.filter(job -> EXECUTING_ON_HOLD_SOURCE.getStatuses().contains(job.getValue()))
				.forEach(jobEntry -> {
					final ClientJob job = jobEntry.getKey();

					if (serverAgent.resources()
							.getAvailableResources(job.getStartTime(), job.getEndTime(), mapToJobInstanceId(job),
									EXECUTING_ON_BACK_UP.getStatuses()).areSufficient(job.getEstimatedResources())) {
						final boolean hasStarted = jobEntry.getValue().equals(ON_HOLD_SOURCE_SHORTAGE);

						MDC.put(MDC_JOB_ID, job.getJobId());
						logger.info(SUPPLY_JOB_WITH_BACK_UP, job.getJobId());
						serverAgent.getServerJobs().replace(job, EXECUTING_ON_BACK_UP.getStatus(hasStarted));
						updateGUI();
					}
				});
	}

	private boolean getIsActiveState() {
		return serverAgent.resources().getCPUUsage(null) > 0
				|| serverAgent.resources().getCPUUsage(singleton(IN_PROGRESS_BACKUP_ENERGY)) > 0;
	}
}
