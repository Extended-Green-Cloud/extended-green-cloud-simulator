package com.database.knowledge.timescale;

public final class TimescaleQueries {

	/**
	 * Monitoring data table queries
	 */
	static final String INSERT_MONITORING_DATA =
			"INSERT INTO monitoring_data (time, aid, data_type, data) VALUES (now(), ?, ?, ?)";
	static final String GET_LAST_5_SEC_DATA =
			"SELECT * FROM monitoring_data WHERE time > now() - INTERVAL '5s'";

	private TimescaleQueries() {
	}
}
