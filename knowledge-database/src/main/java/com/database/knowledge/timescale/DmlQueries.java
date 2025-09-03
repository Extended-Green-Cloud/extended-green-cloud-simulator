package com.database.knowledge.timescale;

/**
 * Data Manipulation Language queries
 */
public final class DmlQueries {

	private static final String ORDER_BY = "order by aid, data_type, time desc";

	/**
	 * Monitoring data table queries
	 */
	static final String INSERT_MONITORING_DATA =
			"INSERT INTO monitoring_data (time, aid, data_type, data) VALUES (?, ?, ?, ?)";
	static final String GET_LAST_1_SEC_DATA =
			"SELECT * FROM monitoring_data WHERE time > ? ";
	static final String GET_ALL_RECORDS_DATA_FOR_DATA_TYPES_AND_TIME =
			"SELECT * FROM monitoring_data "
			+ "where data_type = ANY(?) and time > ? order by data_type, time desc";
	static final String GET_UNIQUE_LAST_RECORDS_DATA_FOR_DATA_TYPES_AND_TIME =
			"SELECT DISTINCT ON (aid, data_type) * FROM monitoring_data "
			+ "where data_type = ANY(?) and time > ? " + ORDER_BY;
	static final String GET_UNIQUE_LAST_RECORDS_DATA_FOR_DATA_TYPES =
			"SELECT DISTINCT ON (aid, data_type) * FROM monitoring_data where data_type = ANY(?) " + ORDER_BY;
	static final String GET_DATA_FOR_DATA_TYPE_AND_AIDS_AND_TIME =
			"SELECT * FROM monitoring_data where data_type = ? and aid = ANY(?) and time > ? ";
	static final String GET_LATEST_N_ROWS_FOR_DATA_TYPE_AND_AIDS =
			"SELECT * FROM (SELECT ROW_NUMBER() OVER (PARTITION BY aid ORDER BY time DESC) AS row, m.*"
					+ "FROM monitoring_data m WHERE m.data_type = ? AND m.aid = ANY(?)) rows "
					+ "WHERE rows.row <= ? ORDER BY rows.aid";

	/**
	 * Job table queries
	 */
	static final String INSERT_JOB_DATA = "INSERT INTO jobs (client_aid, job_id, type, budget, resource_requirements) VALUES (?, ?, ?, ?, ?)";

	/**
	 * Agents table queries
	 */
	static final String INSERT_AGENT_DATA = "INSERT INTO agents (aid, address, data_type, data) VALUES (?, ?, ?, ?)";

	/**
	 * AMS agents table queries
	 */
	static final String INSERT_AMS_DATA = "INSERT INTO ams_agents (name, address) VALUES (?, ?)";
	static final String GET_AMS_DATA = "SELECT * FROM ams_agents";

	/**
	 * System quality table queries
	 */
	static final String INSERT_SYSTEM_QUALITY_DATA =
			"INSERT INTO system_quality (time, goal_id, quality) VALUES (?, ?, ?)";
	static final String GET_LAST_N_QUALITY_DATA_RECORDS_FOR_GOAL =
			"SELECT * FROM system_quality WHERE goal_id=? ORDER BY time DESC LIMIT ?";

	/**
	 * Adaptation goals table query
	 */
	static final String GET_ADAPTATION_GOALS = "SELECT * FROM adaptation_goals";

	/**
	 * Adaptation actions table queries
	 */
	static final String GET_ADAPTATION_ACTIONS = "SELECT * FROM adaptation_actions";
	static final String GET_ADAPTATION_ACTION = "SELECT * FROM adaptation_actions WHERE action_id = ?";
	static final String INSERT_ADAPTATION_ACTION = """
			INSERT INTO adaptation_actions
			(action_id, action_name, type, dedicated_goal_id, action_results, is_available, runs, average_duration)
			VALUES (?, ?, ?, ?, ?, ? ,?, ?)
			""";
	static final String UPDATE_ADAPTATION_ACTION =
			"UPDATE adaptation_actions SET action_results = ?, runs = ?, average_duration = ? WHERE action_id = ?";
	static final String DISABLE_ADAPTATION_ACTION =
			"UPDATE adaptation_actions SET is_available = FALSE WHERE action_id = ?";
	static final String RELEASE_ADAPTATION_ACTION =
			"UPDATE adaptation_actions SET is_available = TRUE WHERE action_id = ?";

	/**
	 * System constants table queries
	 */
	static final String INSERT_DEFAULT_SYSTEM_CONSTANTS =
			"INSERT INTO system_constants (start_time) VALUES (?)";
	static final String READ_SYSTEM_START_TIME =
			"SELECT start_time FROM system_constants LIMIT 1 ";

	/**
	 * Client sequence queries
	 */
	static final String GET_NEXT_CLIENT_ID = "SELECT nextval('clients_generator')";

	private DmlQueries() {
	}
}
