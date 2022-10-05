package com.greencloud.application.agents.server.domain;

/**
 * Class stores all predefined constants for Server Agent
 *
 * <p> JOB_PROCESSING_LIMIT                 - number of jobs that can be processed at once </p>
 * <p> SERVER_ENVIRONMENT_SENSOR_TIMEOUT    - times between consecutive environments checks </p>
 * <p> SERVER_CHECK_POWER_SHORTAGE_JOBS     - times between consecutive checks of jobs affected by source power shortage </p>
 */
public class ServerAgentConstants {
    public static final long JOB_PROCESSING_LIMIT = 20;
    public static final long SERVER_ENVIRONMENT_SENSOR_TIMEOUT = 100;
    public static final long SERVER_CHECK_POWER_SHORTAGE_JOBS = 2000L;
}

