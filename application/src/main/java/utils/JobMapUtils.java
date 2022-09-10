package utils;

import domain.job.JobId;
import domain.job.JobStatusEnum;

import java.util.Map;

public class JobMapUtils {

    /**
     * Method finds the Job or powerJob in the given map with given id
     *
     * @param map map that is being searched
     * @param jobId unique job identifier
     * @return Found Job or PowerJob
     */
    public static <T extends JobId> T getJobById(final Map<T, JobStatusEnum> map, final String jobId){
        return map.keySet().stream().filter(job -> job.getJobId().equals(jobId)).findFirst()
                .orElse(null);
    }
}
