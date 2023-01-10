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
			"INSERT INTO monitoring_data (time, aid, data_type, data) VALUES (now(), ?, ?, ?)";
	static final String GET_LAST_1_SEC_DATA =
			"SELECT * FROM monitoring_data WHERE time > now() - INTERVAL '1s'";
	static final String GET_ALL_RECORDS_DATA_FOR_DATA_TYPES_AND_TIME =
			"SELECT * FROM monitoring_data "
			+ "where data_type = ANY(?) and time > now() - ? * INTERVAL '1s' order by data_type, time desc";
	static final String GET_UNIQUE_LAST_RECORDS_DATA_FOR_DATA_TYPES_AND_TIME =
			"SELECT DISTINCT ON (aid, data_type) * FROM monitoring_data "
			+ "where data_type = ANY(?) and time > now() - ? * INTERVAL '1' SECOND " + ORDER_BY;
	static final String GET_UNIQUE_LAST_RECORDS_DATA_FOR_DATA_TYPES =
			"SELECT DISTINCT ON (aid, data_type) * FROM monitoring_data where data_type = ANY(?) " + ORDER_BY;
	static final String GET_DATA_FOR_DATA_TYPE_AND_AIDS_AND_TIME =
			"SELECT * FROM monitoring_data where data_type = ? and aid = ANY(?) and time > now() - ? * INTERVAL '1' SECOND";

	static final String GET_LATEST_N_ROWS_FOR_DATA_TYPE_AND_AIDS =
			"SELECT * FROM (SELECT ROW_NUMBER() OVER (PARTITION BY aid ORDER BY time DESC) AS row, m.*"
					+ "FROM monitoring_data m WHERE m.data_type = ? AND m.aid = ANY(?)) rows "
					+ "WHERE rows.row <= ? ORDER BY rows.aid";

	/**
	 * System quality table queries
	 */
	static final String INSERT_SYSTEM_QUALITY_DATA =
			"INSERT INTO system_quality (time, goal_id, quality) VALUES (now(), ?, ?)";
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
			(action_id, action_name, type, dedicated_goal_id, action_results, is_available, runs)
			VALUES (?, ?, ?, ?, ?, ? ,?)
			""";
	static final String UPDATE_ADAPTATION_ACTION =
			"UPDATE adaptation_actions SET action_results = ?, runs = ? WHERE action_id = ?";
	static final String DISABLE_ADAPTATION_ACTION =
			"UPDATE adaptation_actions SET is_available = FALSE WHERE action_id = ?";
	static final String RELEASE_ADAPTATION_ACTION =
			"UPDATE adaptation_actions SET is_available = TRUE WHERE action_id = ?";

	private DmlQueries() {
	}
}
