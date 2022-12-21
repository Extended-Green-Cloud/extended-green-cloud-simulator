package org.greencloud.managingsystem.service.monitoring.logs;

/**
 * Class storing log messages used in monitoring services of managing system
 */
public class ManagingAgentMonitoringLog {

	// MONITORING SERVICES LOG MESSAGES
	public static final String READ_ADAPTATION_GOALS_LOG = "Reading adaptation goals from the database...";

	// SUCCESS RATIO
	public static final String READ_SUCCESS_RATIO_CLIENTS_LOG =
			"Reading success ratio for clients from the database...";
	public static final String READ_SUCCESS_RATIO_COMPONENTS_LOG =
			"Reading success ratio for individual components from the database...";
	public static final String READ_SUCCESS_RATIO_CLIENT_NO_DATA_YET_LOG =
			"There is no client success ratio data available yet.";
	public static final String READ_SUCCESS_RATIO_NETWORK_DATA_YET_LOG =
			"There is no cloud network success ratio data available yet.";
	public static final String SUCCESS_RATIO_UNSATISFIED_COMPONENT_LOG =
			"Component {} does not satisfy job success ratio threshold! Job success ratio for component: {}.";
	public static final String SUCCESS_RATIO_CLIENT_LOG =
			"Current job success ratio: {}, Aggregated job success ratio: {}.";

	// BACKUP POWER
	public static final String READ_BACKUP_POWER_QUALITY_LOG =
			"Reading backup power quality from the database.";
	public static final String READ_BACKUP_POWER_QUALITY_NO_DATA_YET_LOG =
			"There is no backup power quality data available yet.";
	public static final String BACKUP_POWER_LOG =
			"Back up power quality ratio: current {}; aggregated: {}.";

	// JOB DISTRIBUTION
	public static final String READ_JOB_DSTRIBUTION_LOG =
			"Reading job distribution coefficients from database...";
	public static final String READ_JOB_DSTRIBUTION_LOG_NO_DATA_YET =
			"Job distribution data is not available yet";
	public static final String JOB_DISTRIBUTION_LOG =
			"Current job distribution: {}";
	public static final String JOB_DISTRIBUTION_UNSATISFIED_LOG =
			"Job distribution is not satisfied! Current job distribution: {}";
}
