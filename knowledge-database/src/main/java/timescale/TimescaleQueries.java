package timescale;

public final class TimescaleQueries {

	/**
	 * Regular queries
	 */
	static final String INSERT_MONITORING_DATA =
			"INSERT INTO monitoring_data (time, aid, data_type, data) VALUES (now(), ?, ?, ?)";
	static final String GET_LAST_5_SEC_DATA =
			"SELECT * FROM monitoring_data WHERE time > now() - INTERVAL '5s'";

	/**
	 * Technical queries - to set up database
	 */
	static final String DROP_MONITORING_DATA = "DROP TABLE IF EXISTS monitoring_data";
	static final String CREATE_MONITORING_DATA = """
			CREATE TABLE monitoring_data (
			time TIMESTAMPTZ NOT NULL,
			aid TEXT NOT NULL,
			data_type TEXT NOT NULL,
			data JSON NOT NULL)
			""";
	static final String CREATE_HYPERTABLE = "SELECT create_hypertable('monitoring_data', 'time')";
	static final String SET_HYPERTABLE_CHUNK_TO_15_SEC = "SELECT set_chunk_time_interval('monitoring_data', 15000000)";

	private TimescaleQueries() {
	}
}
