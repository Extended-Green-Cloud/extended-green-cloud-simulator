package org.greencloud.managingsystem.domain;

import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;

import java.util.List;

import com.database.knowledge.domain.agent.DataType;

/**
 * Class stores all constants related with the Managing System:
 * <p/>
 * <p> MONITOR_SYSTEM_TIMEOUT - time in ms between consecutive monitoring calls </p>
 * <p> NETWORK_AGENT_DATA_TYPES - list of data types used in monitoring data for cloud network agents </p>
 */
public class ManagingSystemConstants {

	public static final int MONITOR_SYSTEM_TIMEOUT = 500;
	public static final List<DataType> NETWORK_AGENT_DATA_TYPES = List.of(SERVER_MONITORING);
}
