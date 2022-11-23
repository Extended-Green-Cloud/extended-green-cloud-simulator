package org.greencloud.managingsystem.domain;

import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;

import java.util.List;

import com.database.knowledge.domain.agent.DataType;

/**
 * Class stores all constants related with the Managing System:
 * <p/>
 * <p> MONITOR_SYSTEM_TIMEOUT - time in ms between consecutive monitoring calls </p>
 * <p> MONITOR_SYSTEM_DATA_TIME_PERIOD - time period of last saved data that is taken into account </p>
 * <p> DATA_NOT_AVAILABLE_INDICATOR - indicator stating that the data is currently unavailable </p>
 * <p> NETWORK_AGENT_DATA_TYPES - list of data types used in monitoring data for cloud network agents </p>
 */
public class ManagingSystemConstants {

	public static final int MONITOR_SYSTEM_TIMEOUT = 500;
	public static final int MONITOR_SYSTEM_DATA_TIME_PERIOD = 5;
	public static final int DATA_NOT_AVAILABLE_INDICATOR = -1;
	public static final List<DataType> NETWORK_AGENT_DATA_TYPES = List.of(SERVER_MONITORING);
}
