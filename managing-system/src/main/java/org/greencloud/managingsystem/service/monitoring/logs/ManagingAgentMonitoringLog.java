package org.greencloud.managingsystem.service.monitoring.logs;

public class ManagingAgentMonitoringLog {

	// MONITORING SERVICES LOG MESSAGES
	public static final String READ_SUCCESS_RATIO_CLIENTS_LOG =
			"Reading success ratio for clients from the database...";
	public static final String READ_SUCCESS_RATIO_COMPONENTS_LOG =
			"Reading success ratio for individual components from the database...";
	public static final String READ_SUCCESS_RATIO_CLIENT_NO_DATA_YET_LOG =
			"There is no client success ratio data available yet";
	public static final String READ_SUCCESS_RATIO_NETWORK_DATA_YET_LOG =
			"There is no cloud network success ratio data available yet";
	public static final String SUCCESS_RATIO_UNSATISFIED_COMPONENT_LOG =
			"Component {} does not satisfy job success ratio threshold! Job success ratio for component: {}";
	public static final String SUCCESS_RATIO_UNSATISFIED_CLIENT_LOG =
			"Overall job success ratio does not satisfy the threshold! Job success ratio: {}";
	public static final String READ_ADAPTATION_GOALS_LOG = "Reading adaptation goals from the database...";
}
