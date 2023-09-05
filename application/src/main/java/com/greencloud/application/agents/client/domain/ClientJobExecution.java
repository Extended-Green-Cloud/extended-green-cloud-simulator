package com.greencloud.application.agents.client.domain;

import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.commons.domain.job.enums.JobClientStatusEnum.CREATED;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.greencloud.commons.domain.job.ClientJob;
import com.greencloud.commons.domain.job.ImmutableClientJob;
import com.greencloud.commons.domain.job.JobStep;
import com.greencloud.commons.domain.job.enums.JobClientStatusEnum;
import com.greencloud.commons.domain.resources.HardwareResources;
import com.greencloud.commons.time.Timer;

import jade.core.AID;

/**
 * Class containing data and method associated with state of the execution of the client's job
 */
public class ClientJobExecution {

	protected final Timer timer = new Timer();
	protected String jobType;
	protected ClientJob job;
	protected Instant jobSimulatedStart;
	protected Instant jobSimulatedEnd;
	protected Instant jobSimulatedDeadline;
	protected JobClientStatusEnum jobStatus;
	protected Map<JobClientStatusEnum, Long> jobDurationMap;

	ClientJobExecution() {
		this.jobDurationMap = stream(JobClientStatusEnum.values()).collect(
				toMap(statusEnum -> statusEnum, statusEnum -> 0L));
		this.timer.startTimeMeasure(getCurrentTime());
	}

	/**
	 * Class constructor
	 *
	 * @param job                  job assigned for execution
	 * @param jobSimulatedStart    job execution start time (converted to simulation time)
	 * @param jobSimulatedEnd      job execution end time (converted to simulation time)
	 * @param jobSimulatedDeadline job execution deadline (converted to simulation time)
	 * @param jobStatus            current job status
	 */
	public ClientJobExecution(final ClientJob job, final Instant jobSimulatedStart, final Instant jobSimulatedEnd,
			final Instant jobSimulatedDeadline, final JobClientStatusEnum jobStatus, final String jobType) {
		this();
		this.job = job;
		this.jobSimulatedStart = jobSimulatedStart;
		this.jobSimulatedEnd = jobSimulatedEnd;
		this.jobSimulatedDeadline = jobSimulatedDeadline;
		this.jobStatus = jobStatus;
		this.jobType = jobType;
	}

	/**
	 * Class constructor
	 *
	 * @param clientAID       identifier of the client
	 * @param start           job execution start time (converted to simulation time)
	 * @param end             job execution end time (converted to simulation time)
	 * @param deadline        job execution deadline (converted to simulation time)
	 * @param jobRequirements requirements of the job sent by the client
	 * @param jobSteps list of job steps
	 * @param jobId           job identifier
	 */
	public ClientJobExecution(final AID clientAID, final Instant start, final Instant end,
			final Instant deadline, final HardwareResources jobRequirements, final List<JobStep> jobSteps,
			final String jobId, final String jobType) {
		this(ImmutableClientJob.builder()
						.jobId(jobId)
						.startTime(start)
						.endTime(end)
						.deadline(deadline)
						.estimatedResources(jobRequirements)
						.clientIdentifier(clientAID.getName())
						.jobSteps(jobSteps)
						.clientAddress(clientAID.getAddressesArray()[0])
						.build(),
				start, end, deadline, CREATED, jobType);
	}

	public ClientJob getJob() {
		return job;
	}

	public void setJob(ClientJob job) {
		this.job = job;
	}

	public Instant getJobSimulatedStart() {
		return jobSimulatedStart;
	}

	public void setJobSimulatedStart(Instant jobSimulatedStart) {
		this.jobSimulatedStart = jobSimulatedStart;
	}

	public Instant getJobSimulatedEnd() {
		return jobSimulatedEnd;
	}

	public void setJobSimulatedEnd(Instant jobSimulatedEnd) {
		this.jobSimulatedEnd = jobSimulatedEnd;
	}

	public Instant getJobSimulatedDeadline() {
		return jobSimulatedDeadline;
	}

	public JobClientStatusEnum getJobStatus() {
		return jobStatus;
	}

	public void setJobStatus(JobClientStatusEnum jobStatus) {
		this.jobStatus = jobStatus;
	}

	public Map<JobClientStatusEnum, Long> getJobDurationMap() {
		return jobDurationMap;
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobDurationMap(
			Map<JobClientStatusEnum, Long> jobDurationMap) {
		this.jobDurationMap = jobDurationMap;
	}

	public Timer getTimer() {
		return timer;
	}

	/**
	 * Method updates data in job status duration map (i.e. updates time during which the job was at the given status)
	 *
	 * @param newStatus new status of the job
	 * @param time      time when the job execution has changed the status
	 */
	public synchronized void updateJobStatusDuration(final JobClientStatusEnum newStatus, final Instant time) {
		final long elapsedTime = timer.stopTimeMeasure(time);
		timer.startTimeMeasure(time);
		jobDurationMap.computeIfPresent(jobStatus, (key, val) -> val + elapsedTime);
		jobStatus = newStatus;
	}
}
