package org.greencloud.commons.mapper;

import static java.util.Objects.requireNonNull;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;

import org.greencloud.commons.domain.job.basic.PowerJob;
import org.greencloud.commons.domain.job.extended.ImmutableJobWithStatus;
import org.greencloud.commons.domain.job.extended.JobWithStatus;

/**
 * Class provides set of methods mapping job status object classes
 */
public class JobStatusMapper {

	/**
	 * @param job job for which status message is to be sent
	 * @return JobWithStatus
	 */
	public static <T extends PowerJob> JobWithStatus mapToJobWithStatusForCurrentTime(final T job) {
		return ImmutableJobWithStatus.builder()
				.jobId(job.getJobId())
				.jobInstanceId(job.getJobInstanceId())
				.changeTime(getCurrentTime())
				.build();
	}

	/**
	 * @param job    job for which status message is to be sent
	 * @param server server assigned to the job
	 * @return JobWithStatus
	 */
	public static <T extends PowerJob> JobWithStatus mapToJobWithStatusForServer(final T job, final String server) {
		return ImmutableJobWithStatus.builder()
				.jobId(job.getJobId())
				.jobInstanceId(job.getJobInstanceId())
				.changeTime(requireNonNull(job.getStartTime()))
				.serverName(server)
				.build();
	}
}
