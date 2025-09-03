package org.greencloud.managingsystem.domain;

import static com.database.knowledge.types.DataType.GREEN_SOURCE_MONITORING;
import static com.database.knowledge.types.DataType.SERVER_MONITORING;

import java.util.List;

import com.database.knowledge.types.DataType;

/**
 * Class stores all constants related with the Managing System:
 * <p> NETWORK_AGENT_DATA_TYPES - list of data types used in monitoring data for regional manager agents </p>
 */
public final class ManagingSystemConstants {

	public static final List<DataType> NETWORK_AGENT_DATA_TYPES = List.of(SERVER_MONITORING, GREEN_SOURCE_MONITORING);
}
