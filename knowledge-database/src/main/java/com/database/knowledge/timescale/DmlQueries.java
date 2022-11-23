package com.database.knowledge.timescale;

/**
 * Data Manipulation Language queries
 */
public final class DmlQueries {

	/**
	 * Monitoring data table queries
	 */
	static final String INSERT_MONITORING_DATA =
			"INSERT INTO monitoring_data (time, aid, data_type, data) VALUES (now(), ?, ?, ?)";
	static final String GET_LAST_1_SEC_DATA =
			"SELECT * FROM monitoring_data WHERE time > now() - INTERVAL '1s'";
	static final String GET_LAST_RECORDS_DATA_FOR_DATA_TYPES =
			"SELECT DISTINCT ON (aid) * FROM monitoring_data "
					+ "where data_type = ANY(?) and time > now() - ? * INTERVAL '1' SECOND";

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
			"UPDATE adaptation_actions SET action_results = ?, is_available = FALSE, runs = ? WHERE action_id = ?";
	static final String RELEASE_ADAPTATION_ACTION =
			"UPDATE adaptation_actions SET is_available = TRUE WHERE action_id = ?";

	private DmlQueries() {
	}
}
