package com.database.knowledge.timescale;

/**
 * Data Definition Language Commands
 */
public final class DdlCommands {

	/**
	 * System constants table commands
	 */
	static final String DROP_SYSTEM_CONSTANTS = "DROP TABLE IF EXISTS system_constants";
	static final String CREATE_SYSTEM_CONSTANTS = """
			CREATE TABLE system_constants (
			start_time TIMESTAMPTZ NOT NULL)
			""";

	/**
	 * Client generation sequence
	 */
	static final String DROP_CLIENT_SEQUENCE = "DROP SEQUENCE IF EXISTS clients_generator";
	static final String CREATE_CLIENT_SEQUENCE = """
			CREATE SEQUENCE clients_generator START 1
			""";

	/**
	 * Monitoring data table commands
	 */
	static final String DROP_MONITORING_DATA = "DROP TABLE IF EXISTS monitoring_data";
	static final String CREATE_MONITORING_DATA = """
			CREATE TABLE monitoring_data (
			time TIMESTAMPTZ NOT NULL,
			aid VARCHAR(50) NOT NULL,
			data_type VARCHAR(50) NOT NULL,
			data JSON NOT NULL)
			""";
	static final String CREATE_HYPERTABLE = "SELECT create_hypertable('monitoring_data', 'time')";
	static final String SET_HYPERTABLE_CHUNK_TO_5_SEC = "SELECT set_chunk_time_interval('monitoring_data', 5000000)";
	static final String CREATE_MONITORING_INDEX =
			"CREATE INDEX monitoring_data_aid ON monitoring_data (aid, data_type, time DESC)";

	/**
	 * Agents configuration data
	 */
	static final String DROP_JOBS = "DROP TABLE IF EXISTS jobs";
	static final String CREATE_JOBS = """
			CREATE TABLE jobs (
			client_aid VARCHAR(50) NOT NULL,
			job_id VARCHAR(50) NOT NULL,
			type VARCHAR(50) NOT NULL,
			budget DOUBLE PRECISION NOT NULL,
			resource_requirements JSON NOT NULL)
			""";

	/**
	 * Agents configuration data
	 */
	static final String DROP_AGENTS = "DROP TABLE IF EXISTS agents";
	static final String CREATE_AGENTS = """
			CREATE TABLE agents (
			aid VARCHAR(50) NOT NULL,
			address VARCHAR(50) NOT NULL,
			data_type VARCHAR(50) NOT NULL,
			data JSON NOT NULL)
			""";

	/**
	 * System AMS agents table
	 */
	static final String DROP_AMS_AGENTS = "DROP TABLE IF EXISTS ams_agents";
	static final String CREATE_AMS_AGENTS = """
			CREATE TABLE ams_agents (
			name VARCHAR(50) NOT NULL,
			address VARCHAR(50) NOT NULL)
			""";

	/**
	 * Adaptation goals table
	 */
	static final String DROP_ADAPTATION_GOALS = "DROP TABLE IF EXISTS adaptation_goals";
	static final String CREATE_ADAPTATION_GOALS = """
			CREATE TABLE adaptation_goals (
			goal_id INTEGER PRIMARY KEY,
			goal_name VARCHAR(200) UNIQUE NOT NULL,
			threshold DOUBLE PRECISION NOT NULL,
			is_above_threshold BOOLEAN NOT NULL,
			weight DOUBLE PRECISION NOT NULL)
			""";
	static final String INSERT_ADAPTATION_GOALS = """
			INSERT INTO adaptation_goals (goal_id, goal_name, threshold, is_above_threshold, weight) VALUES
			(1, 'Maximize job success ratio', 0.8, true, 0.6),
			(2, 'Minimize used backup power', 0.2, false, 0.2),
			(3, 'Distribute traffic evenly', 1, false, 0.2)
			""";

	/**
	 * Adaptation actions table
	 */
	static final String DROP_ADAPTATION_ACTIONS = "DROP TABLE IF EXISTS adaptation_actions";
	static final String CREATE_ADAPTATION_ACTIONS = """
			CREATE TABLE adaptation_actions (
			action_id INTEGER PRIMARY KEY,
			action_name VARCHAR(50) NOT NULL,
			type VARCHAR(50) NOT NULL,
			dedicated_goal_id INTEGER NOT NULL,
			action_results JSON NOT NULL,
			is_available BOOLEAN NOT NULL,
			runs INTEGER NOT NULL,
			average_duration DOUBLE PRECISION NOT NULL,
			FOREIGN KEY (dedicated_goal_id) REFERENCES adaptation_goals (goal_id),
			UNIQUE (action_id, dedicated_goal_id))
			""";

	/**
	 * System quality table
	 */
	static final String DROP_SYSTEM_QUALITY = "DROP TABLE IF EXISTS system_quality";
	static final String CREATE_SYSTEM_QUALITY = """
			CREATE TABLE system_quality (
			time TIMESTAMPTZ NOT NULL,
			goal_id INTEGER NOT NULL,
			quality DOUBLE PRECISION NOT NULL)
			""";
	static final String CREATE_SYSTEM_QUALITY_HYPERTABLE = "SELECT create_hypertable('system_quality', 'time')";
	static final String SET_SYSTEM_QUALITY_HYPERTABLE_CHUNK_TO_5_SEC = "SELECT set_chunk_time_interval('system_quality', 5000000)";

	private DdlCommands() {
	}
}
