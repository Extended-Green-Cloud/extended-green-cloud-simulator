package com.greencloud.application.agents.cloudnetwork.domain;

import java.time.temporal.ValueRange;

/**
 * Class storing Cloud Network Agent constants:
 * <p> MAX_POWER_DIFFERENCE 	- value range describing the maximum difference in free power of the Server Agents that can be neglected in Server selection </p>
 * <p> TRANSFER_EXPIRATION_TIME - deadline for receiving response regarding job transfer </p>
 * <p> MAX_ERROR_IN_JOB_START 	- time error added to the time after which job execution should start </p>
 */
public class CloudNetworkAgentConstants {

	public static final ValueRange MAX_POWER_DIFFERENCE = ValueRange.of(-10, 10);
	public static final long TRANSFER_EXPIRATION_TIME = 20000;
	public static final Long MAX_ERROR_IN_JOB_START = 1L;
}
