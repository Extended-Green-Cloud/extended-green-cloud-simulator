package utils;

import domain.job.AbstractJob;
import domain.job.JobStatusEnum;

import java.time.Instant;
import java.util.Map;

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
    public static <T extends AbstractJob> T getJobByIdAnStartDate(final Map<T, JobStatusEnum> map, final String jobId, final Instant startDate) {
        return  map.keySet().stream()
                .filter(job -> job.getJobId().equals(jobId) && job.getStartTime().equals(startDate))
                .findFirst()
                .orElse(null);
    }
}
