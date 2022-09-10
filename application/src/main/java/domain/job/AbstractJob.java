package domain.job;

import java.time.Instant;

public interface AbstractJob {

    /**
     * @return unique job identifier
     */
    String getJobId();

    Instant getStartTime();
}
