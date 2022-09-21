package com.greencloud.application.domain.job;

import java.time.Instant;

public interface AbstractJob {

    /**
     * @return unique job identifier
     */
    String getJobId();

    /**
     * @return time when the power delivery should start
     */
    Instant getStartTime();

    /**
     * @return time when the power delivery should finish
     */
    Instant getEndTime();

    /**
     * @return power that is to be delivered
     */
    int getPower();
}
