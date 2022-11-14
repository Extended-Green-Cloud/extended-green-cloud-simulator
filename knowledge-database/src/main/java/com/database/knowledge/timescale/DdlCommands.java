package com.database.knowledge.timescale;

/**
 * Data Definition Language Commands
 */
public final class DdlCommands {

	/**
	 * Monitoring data table commands
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
	static final String SET_HYPERTABLE_CHUNK_TO_5_SEC = "SELECT set_chunk_time_interval('monitoring_data', 5000000)";

	/**
	 * Adaptation goals table
	 */
	static final String DROP_ADAPTATION_GOALS = "DROP TABLE IF EXISTS adaptation_goals";
	static final String CREATE_ADAPTATION_GOALS = """
			CREATE TABLE adaptation_goals (
			goal_name TEXT NOT NULL,
			goal_id INTEGER NOT NULL UNIQUE)
			""";
	static final String INSERT_ADAPTATION_GOALS = """
			INSERT INTO adaptation_goals (goal_name, goal_id) VALUES
			('Maximize job success ratio', 1),
			('Minimize used backup power', 2),
			('Distribute traffic evenly', 3)
			""";

	/**
	 * Adaptation actions table
	 */
	static final String DROP_ADAPTATION_ACTIONS = "DROP TABLE IF EXISTS adaptation_actions";
	static final String CREATE_ADAPTATION_ACTIONS = """
			CREATE TABLE adaptation_actions (
			action_id INTEGER NOT NULL UNIQUE,
			action_name TEXT NOT NULL,
			dedicated_goal_id INTEGER NOT NULL,
			action_results JSON NOT NULL,
			is_available BOOLEAN NOT NULL,
			runs INTEGER NOT NULL)
			""";

	private DdlCommands() {
	}
}
