package com.greencloud.application.agents.server.domain;

import java.time.temporal.ValueRange;

/**
 * Class stores all predefined constants for Server Agent
 *
 * <p> JOB_PROCESSING_LIMIT                 - number of jobs that can be processed at once </p>
 * <p> SERVER_ENVIRONMENT_SENSOR_TIMEOUT    - times between consecutive environments checks </p>
 * <p> SERVER_CHECK_POWER_SHORTAGE_JOBS     - times between consecutive checks of jobs affected by source power shortage </p>
 * <p> MAX_AVAILABLE_POWER_DIFFERENCE       - range describing the negligible difference in available power of Green Sources </p>
 */
public class ServerAgentConstants {

    public static final ValueRange MAX_AVAILABLE_POWER_DIFFERENCE = ValueRange.of(-10, 10);
    public static final long JOB_PROCESSING_LIMIT = 20;
    public static final long SERVER_ENVIRONMENT_SENSOR_TIMEOUT = 100;
    public static final long SERVER_CHECK_POWER_SHORTAGE_JOBS = 2000L;
}

