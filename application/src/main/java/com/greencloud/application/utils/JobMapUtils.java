package com.greencloud.application.utils;
import com.greencloud.application.domain.job.*;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import static com.greencloud.application.domain.job.JobStatusEnum.ACCEPTED_JOB_STATUSES;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.application.utils.TimeUtils.isWithinTimeStamp;

/**
 * A set of utility methods that help manage and get information from the maps
 * with structure : AbstractJob -> JobStatusEnum
 */
public class JobMapUtils {

    /**
     * Method finds the Job or powerJob in the given map with given id
     *
     * @param map map that is being searched
     * @param jobId unique job identifier
     * @return Found Job or PowerJob
     */
    public static <T extends AbstractJob> T getJobById(final Map<T, JobStatusEnum> map, final String jobId) {
        return map.keySet().stream().filter(job -> job.getJobId().equals(jobId)).findFirst()
                .orElse(null);
    }

    /**
     * Method finds the job or powerJob in the given map with given id and startDate
     * @param map map that is being searched
     * @param jobId unique job identifier
     * @param startDate start date to filter by
     * @return Found Job or PowerJob
     */
    public static <T extends AbstractJob> T getJobByIdAndStartDate(final Map<T, JobStatusEnum> map, final String jobId, final Instant startDate) {
        return  map.keySet().stream()
                .filter(job -> job.getJobId().equals(jobId) && job.getStartTime().equals(startDate))
                .findFirst()
                .orElse(null);
    }

    /**
     * Method finds the job or powerJob in the given map with given id and startDate
     * @param map map that is being searched
     * @param jobInstanceId unique identifier of the job instance
     * @return Found Job or PowerJob
     */
    public static <T extends AbstractJob> T getJobByIdAndStartDate(final Map<T, JobStatusEnum> map, final JobInstanceIdentifier jobInstanceId) {
        return map.keySet().stream()
                .filter(job -> job.getJobId().equals(jobInstanceId.getJobId())
                        && job.getStartTime().equals(jobInstanceId.getStartTime()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Method verifies if there is only 1 instance of the given job
     *
     * @param map map on which the operations are executed
     * @param jobId unique job identifier
     * @return boolean
     */
    public static <T extends AbstractJob> boolean isJobUnique(final Map<T, JobStatusEnum> map, final String jobId) {
        return  map.keySet().stream()
                .filter(job -> job.getJobId().equals(jobId))
                .toList()
                .size()
                == 1;
    }

    /**
     *  Method return the amount of all jobs in the map that are currently accepted
     *
     * @param map map on which the operations are executed
     */
    public static <T extends AbstractJob> int getJobCount(final Map<T, JobStatusEnum> map) {
        return map.entrySet().stream()
                .filter(job -> ACCEPTED_JOB_STATUSES.contains(job.getValue())
                        && isWithinTimeStamp(
                        job.getKey().getStartTime(), job.getKey().getEndTime(), getCurrentTime()))
                .map(Map.Entry::getKey)
                .map(T::getJobId)
                .collect(Collectors.toSet())
                .size();
    }

    /**
     * Method returns the instance of the job for current time
     *
     * @param map map on which the operations are executed
     * @param jobId unique job identifier
     * @return pair of job and current status
     */
    public static <T extends AbstractJob> Map.Entry<T, JobStatusEnum> getCurrentJobInstance(final Map<T, JobStatusEnum> map, final String jobId) {
        final Instant currentTime = getCurrentTime();
        return map.entrySet().stream().filter(jobEntry -> {
            final T job = jobEntry.getKey();
            return job.getJobId().equals(jobId) && (
                    (job.getStartTime().isBefore(currentTime) && job.getEndTime().isAfter(currentTime))
                            || job.getEndTime().equals(currentTime));
        }).findFirst().orElse(null);
    }
}
