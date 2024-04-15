package org.greencloud.commons.args.agent.client.agent;

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.commons.args.agent.EGCSAgentType.CLIENT;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;

import java.time.Instant;
import java.util.Map;

import org.greencloud.commons.args.agent.EGCSAgentProps;
import org.greencloud.commons.args.job.JobArgs;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.basic.ImmutableClientJob;
import org.greencloud.commons.domain.timer.Timer;
import org.greencloud.commons.enums.job.JobClientStatusEnum;
import org.greencloud.commons.utils.time.TimeConverter;

import jade.core.AID;
import lombok.Getter;
import lombok.Setter;

/**
 * Arguments representing internal properties of Client Agent
 */
@Getter
@Setter
public class ClientAgentProps extends EGCSAgentProps {

	protected final Timer jobExecutionTimer = new Timer();
	protected boolean isAnnounced;
	protected String jobType;
	protected ClientJob job;
	protected Instant jobSimulatedStart;
	protected Instant jobSimulatedEnd;
	protected Instant jobSimulatedDeadline;
	protected JobClientStatusEnum jobStatus;
	protected Map<JobClientStatusEnum, Long> jobDurationMap;

	/**
	 * Constructor that initialize job state to initial values
	 *
	 * @param agentName name of the agent
	 */
	public ClientAgentProps(final String agentName) {
		super(CLIENT, agentName);

		this.jobDurationMap = stream(JobClientStatusEnum.values()).collect(
				toMap(statusEnum -> statusEnum, statusEnum -> 0L));
		this.jobExecutionTimer.startTimeMeasure(getCurrentTime());
		this.jobStatus = JobClientStatusEnum.CREATED;
	}

	/**
	 * constructor
	 *
	 * @param agentName name of the agent
	 * @param clientAID identifier of the client
	 * @param start     job execution start time (converted to simulation time)
	 * @param end       job execution end time (converted to simulation time)
	 * @param deadline  job execution deadline (converted to simulation time)
	 * @param jobArgs   arguments of the client job
	 * @param jobId     job identifier
	 */
	public ClientAgentProps(final String agentName, final AID clientAID, final Instant start, final Instant end,
			final Instant deadline, final JobArgs jobArgs, final String jobId) {
		this(agentName);
		final Instant currentTime = getCurrentTime();
		final Instant simulatedJobStart = parseJobTimeFrame(start, currentTime);
		final Instant simulatedJobEnd = parseJobTimeFrame(end, currentTime);
		final Instant simulatedJobDeadline = parseJobTimeFrame(deadline, currentTime);

		final ClientJob clientJob = ImmutableClientJob.builder()
				.jobId(jobId)
				.clientIdentifier(clientAID.getName())
				.clientAddress(clientAID.getAddressesArray()[0])
				.startTime(simulatedJobStart)
				.endTime(simulatedJobEnd)
				.deadline(simulatedJobDeadline)
				.requiredResources(jobArgs.getResources())
				.jobSteps(jobArgs.getJobSteps())
				.selectionPreference(jobArgs.getSelectionPreference())
				.build();

		this.jobType = jobArgs.getProcessorName();
		this.job = clientJob;
		this.jobSimulatedStart = simulatedJobStart;
		this.jobSimulatedEnd = simulatedJobEnd;
		this.jobSimulatedDeadline = simulatedJobDeadline;
	}

	private static Instant parseJobTimeFrame(final Instant jobRealTime, final Instant currentTime) {
		final long timeDifference = TimeConverter.convertToSimulationTime(SECONDS.between(currentTime, jobRealTime));
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
}
