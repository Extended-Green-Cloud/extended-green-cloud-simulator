package org.greencloud.commons.args.agent.client.agent;

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.commons.args.agent.EGCSAgentType.CLIENT;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.CREATED;
import static org.greencloud.commons.utils.time.TimeConverter.convertToInstantTime;
import static org.greencloud.commons.utils.time.TimeConverter.convertToSimulationTime;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;

import java.time.Instant;
import java.util.Map;

import org.greencloud.commons.args.agent.EGCSAgentProps;
import org.greencloud.commons.args.agent.client.factory.ClientArgs;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.basic.ImmutableClientJob;
import org.greencloud.commons.domain.timer.Timer;
import org.greencloud.commons.enums.job.JobClientStatusEnum;

import jade.core.AID;
import lombok.Getter;
import lombok.Setter;

/**
 * Arguments representing internal properties of Client Agent
 */
@Getter
@Setter
public class ClientAgentProps extends EGCSAgentProps<ClientArgs> {

	protected final Timer jobExecutionTimer = new Timer();
	protected boolean isAnnounced;
	protected Long expectedExecutionDuration;
	protected String jobType;
	protected ClientJob job;
	protected Instant jobSimulatedDeadline;
	protected JobClientStatusEnum jobStatus;
	protected Map<JobClientStatusEnum, Long> jobDurationMap;

	/**
	 * Constructor
	 *
	 * @param agentName  name of the agent
	 * @param clientAID  identifier of the client
	 * @param clientArgs client controller arguments
	 */
	public ClientAgentProps(final String agentName, final AID clientAID, final ClientArgs clientArgs) {
		super(CLIENT, agentName, clientArgs);
		this.config = clientArgs;

		this.jobDurationMap = stream(JobClientStatusEnum.values()).collect(
				toMap(statusEnum -> statusEnum, statusEnum -> 0L));
		this.jobExecutionTimer.startTimeMeasure(getCurrentTime());
		this.jobStatus = CREATED;
		this.jobType = clientArgs.getJob().getProcessorName();
		this.job = prepareClientJob(clientAID);
		this.jobSimulatedDeadline = job.getDeadline();
		this.expectedExecutionDuration = clientArgs.getJob().getDuration();
	}

	private static Instant parseJobTimeFrame(final Instant jobRealTime, final Instant currentTime) {
		final long timeDifference = convertToSimulationTime(SECONDS.between(currentTime, jobRealTime));
		return currentTime.plus(timeDifference, MILLIS);
	}

	/**
	 * Method updates data in job status duration map (i.e. updates time during which the job was at the given status)
	 *
	 * @param newStatus new status of the job
	 * @param time      time when the job execution has changed the status
	 */
	public synchronized void updateJobStatusDuration(final JobClientStatusEnum newStatus, final Instant time) {
		final long elapsedTime = jobExecutionTimer.stopTimeMeasure(time);
		jobExecutionTimer.startTimeMeasure(time);
		jobDurationMap.computeIfPresent(jobStatus, (key, val) -> val + elapsedTime);
		jobStatus = newStatus;
	}

	/**
	 * Method estimates if job execution is within the budget limit.
	 *
	 * @param jobPrice price of job execution
	 * @return information if job is within a budget
	 */
	public boolean isJobWithinBudget(final Double jobPrice) {
		return ofNullable(job.getBudgetLimit())
				.map(limit -> ofNullable(jobPrice).orElse(0D) <= limit * expectedExecutionDuration)
				.orElse(true);
	}

	private ClientJob prepareClientJob(final AID clientAID) {
		final Instant currentTime = getCurrentTime();
		final Instant deadline = convertToInstantTime(getConfig().formatClientDeadline());
		final Instant simulatedJobDeadline = parseJobTimeFrame(deadline, currentTime);

		return ImmutableClientJob.builder()
				.jobId(getConfig().getJobId())
				.clientIdentifier(clientAID.getName())
				.clientAddress(clientAID.getAddressesArray()[0])
				.deadline(simulatedJobDeadline)
				.requiredResources(getConfig().getJob().getResources())
				.jobSteps(getConfig().getJob().getJobSteps())
				.priority(getConfig().getJob().getPriority())
				.jobType(getConfig().getJob().getProcessorName())
				.duration(convertToSimulationTime(getConfig().getJob().getDuration()))
				.selectionPreference(getConfig().getJob().getSelectionPreference())
				.budgetLimit(getConfig().getJob().getBudgetLimit())
				.build();
	}
}
