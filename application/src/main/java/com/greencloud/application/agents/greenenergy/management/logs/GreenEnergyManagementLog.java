package com.greencloud.application.agents.greenenergy.management.logs;

/**
 * Class contains all constants used in logging information in green energy agent's managers
 */
public class GreenEnergyManagementLog {

	// STATE MANAGEMENT LOG MESSAGES
	public static final String UNIQUE_POWER_JOB_START_LOG = "Started job {}. Number of unique started jobs is {}";
	public static final String DUPLICATED_POWER_JOB_START_LOG =
			"Started job instance {}. Number of started job instances is {}";
	public static final String UNIQUE_POWER_JOB_FINISH_LOG =
			"Finished job {}. Number of unique finished jobs is {} out of {} started";
	public static final String DUPLICATED_POWER_JOB_FINISH_LOG =
			"Finished job instance {}. Number of finished job instances is {} out of {} started";
	public static final String AVERAGE_POWER_LOG = "Calculated available {} average power {} between {} and {}";
	public static final String CURRENT_AVAILABLE_POWER_LOG = "Calculated available {} power {} at {}";

	// POWER MANAGEMENT LOG MESSAGES
	public static final String SOLAR_FARM_SHUTDOWN_LOG = "SOLAR farm is shutdown at {}, sunrise at {} & sunset at {}";
}
