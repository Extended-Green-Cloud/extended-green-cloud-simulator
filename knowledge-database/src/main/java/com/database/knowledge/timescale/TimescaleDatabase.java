package com.database.knowledge.timescale;

import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationActions;
import static com.database.knowledge.timescale.DdlCommands.CREATE_ADAPTATION_ACTIONS;
import static com.database.knowledge.timescale.DdlCommands.CREATE_ADAPTATION_GOALS;
import static com.database.knowledge.timescale.DdlCommands.CREATE_HYPERTABLE;
import static com.database.knowledge.timescale.DdlCommands.CREATE_MONITORING_DATA;
import static com.database.knowledge.timescale.DdlCommands.CREATE_MONITORING_INDEX;
import static com.database.knowledge.timescale.DdlCommands.CREATE_SYSTEM_QUALITY;
import static com.database.knowledge.timescale.DdlCommands.CREATE_SYSTEM_QUALITY_HYPERTABLE;
import static com.database.knowledge.timescale.DdlCommands.DROP_ADAPTATION_ACTIONS;
import static com.database.knowledge.timescale.DdlCommands.DROP_ADAPTATION_GOALS;
import static com.database.knowledge.timescale.DdlCommands.DROP_MONITORING_DATA;
import static com.database.knowledge.timescale.DdlCommands.DROP_SYSTEM_QUALITY;
import static com.database.knowledge.timescale.DdlCommands.INSERT_ADAPTATION_GOALS;
import static com.database.knowledge.timescale.DdlCommands.SET_HYPERTABLE_CHUNK_TO_5_SEC;
import static com.database.knowledge.timescale.DdlCommands.SET_SYSTEM_QUALITY_HYPERTABLE_CHUNK_TO_5_SEC;
import static java.lang.String.format;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.DataType;
import com.database.knowledge.domain.agent.MonitoringData;
import com.database.knowledge.domain.goal.AdaptationGoal;
import com.database.knowledge.domain.goal.GoalEnum;
import com.database.knowledge.domain.systemquality.SystemQuality;
import com.database.knowledge.timescale.exception.ClosingDatabaseException;
import com.database.knowledge.timescale.exception.ConnectDatabaseException;
import com.database.knowledge.timescale.exception.InitDatabaseException;
import com.database.knowledge.timescale.exception.ReadDataException;
import com.database.knowledge.timescale.exception.WriteDataException;
import com.fasterxml.jackson.core.JsonProcessingException;

public class TimescaleDatabase implements Closeable {

	private static final String DATABASE_NAME = "postgres";
	private static final String USER = "postgres";
	private static final String PASSWORD = "password";
	private static final String LOCAL_DATABASE_HOST_NAME = "127.0.0.1";

	private static Connection sqlConnection;
	private static JdbcStatementsExecutor statementsExecutor;

	public TimescaleDatabase() {
		this(LOCAL_DATABASE_HOST_NAME);
	}

	public TimescaleDatabase(String hostName) {
		if (sqlConnection == null) {
			try {
				sqlConnection = connect(hostName);
			} catch (SQLException exception) {
				throw new ConnectDatabaseException(exception);
			}
			statementsExecutor = new JdbcStatementsExecutor(sqlConnection);
		}
	}

	@Override
	public void close() {
		try {
			sqlConnection.close();
			sqlConnection = null;
		} catch (SQLException exception) {
			throw new ClosingDatabaseException(exception);
		}
	}

	/**
	 * Initializes the database, must be used only by the EngineRunner when the system is started.
	 */
	public void initDatabase() {
		try {
			dropTableIfExists(sqlConnection);
			createSchema(sqlConnection);
		} catch (SQLException | JsonProcessingException exception) {
			throw new InitDatabaseException(exception);
		}
	}

	/**
	 * Provides writing capability to the TimeScaleDB for agents.
	 *
	 * @param aid      aid of the agent writing to the database
	 * @param dataType type of the data to be written
	 * @param data     data to be written, must be serializable to JSON
	 */
	public void writeMonitoringData(String aid, DataType dataType, MonitoringData data) {
		try {
			statementsExecutor.executeWriteStatement(aid, dataType, data);
		} catch (SQLException | JsonProcessingException exception) {
			throw new WriteDataException(exception);
		}
	}

	/**
	 * Provides writing capability to the TimeScaleDB for Managing Agent to insert new information
	 * regarding current system quality
	 *
	 * @param goalId      identifier of adaptation goal
	 * @param goalQuality current quality of the goal
	 */
	public void writeSystemQualityData(Integer goalId, Double goalQuality) {
		try {
			statementsExecutor.executeWriteStatement(goalId, goalQuality);
		} catch (SQLException exception) {
			throw new WriteDataException(exception);
		}
	}

	/**
	 * Updates given adaptation action with additional goals changes data
	 *
	 * @param actionId    id of the adaptation action to update
	 * @param goalChanges additional goals changes data for the given adaptation action
	 * @return updated {@link AdaptationAction}
	 */
	public AdaptationAction updateAdaptationAction(Integer actionId, Map<GoalEnum, Double> goalChanges) {
		try {
			var action = readAdaptationAction(actionId);
			action.mergeActionResults(goalChanges);
			action.increaseRuns();
			statementsExecutor.executeUpdateActionStatement(action);
			return readAdaptationAction(actionId);
		} catch (SQLException | JsonProcessingException exception) {
			throw new WriteDataException(exception);
		}
	}

	/**
	 * Sets given adaptation action as again available
	 *
	 * @param actionId id of the adaption action to update
	 * @return updated {@link AdaptationAction}
	 */
	public AdaptationAction setAdaptationActionAvailability(Integer actionId, boolean isAvailable) {
		try {
			statementsExecutor.executeSetAvailabilityActionStatement(actionId, isAvailable);
			return readAdaptationAction(actionId);
		} catch (SQLException exception) {
			throw new WriteDataException(exception);
		}
	}

	/**
	 * Provides reading capability for Managing Agent. Provides data saved to database by agents in the last 5 seconds.
	 *
	 * @return List of {@link AgentData}, which are immutable java records which represent in 1:1 relation read rows.
	 */
	public List<AgentData> readMonitoringData() {
		try {
			return statementsExecutor.executeReadMonitoringDataStatement();
		} catch (SQLException | JsonProcessingException exception) {
			throw new ReadDataException(exception);
		}
	}

	/**
	 * Provides reading capability for Managing Agent. Provides data records from last, specified by parameter,
	 * seconds that were saved to database for given data types.
	 *
	 * @param dataTypes types of the data to be retrieved
	 * @param seconds   number of seconds for which the data is retrieved
	 * @return List of {@link AgentData}, which are immutable java records which represent in 1:1 relation read rows.
	 */
	public List<AgentData> readMonitoringDataForDataTypes(List<DataType> dataTypes, double seconds) {
		try {
			return statementsExecutor.executeReadMonitoringDataForDataTypesStatement(dataTypes, seconds);
		} catch (SQLException | JsonProcessingException exception) {
			throw new ReadDataException(exception);
		}
	}

	/**
	 * Provides reading capability for Managing Agent. Provides unique data records from last, specified by parameter,
	 * seconds that were saved to database for given data types.
	 *
	 * @param dataTypes types of the data to be retrieved
	 * @param seconds   number of seconds for which the data is retrieved
	 * @return List of {@link AgentData}, which are immutable java records which represent in 1:1 relation read rows.
	 */
	public List<AgentData> readLastMonitoringDataForDataTypes(List<DataType> dataTypes, double seconds) {
		try {
			return statementsExecutor.executeReadLastMonitoringDataForDataTypesStatement(dataTypes, seconds);
		} catch (SQLException | JsonProcessingException exception) {
			throw new ReadDataException(exception);
		}
	}

	/**
	 * Provides reading capability for Managing Agent. Provides unique data records from last records
	 * that were saved to database for given data types.
	 *
	 * @param dataTypes types of the data to be retrieved
	 * @return List of {@link AgentData}, which are immutable java records which represent in 1:1 relation read rows.
	 */
	public List<AgentData> readLastMonitoringDataForDataTypes(List<DataType> dataTypes) {
		try {
			return statementsExecutor.executeReadLastMonitoringDataForDataTypesStatement(dataTypes);
		} catch (SQLException | JsonProcessingException exception) {
			throw new ReadDataException(exception);
		}
	}

	/**
	 * Provides reading capability for Managing Agent. Provides data records from last, specified by parameter, seconds
	 * that were saved to database for given data type and agents.
	 *
	 * @param type    type of the data to be retrieved
	 * @param aidList aid list of the agents of interest
	 * @param seconds number of seconds for which the data is retrieved
	 * @return List of {@link AgentData}, which are immutable java records which represent in 1:1 relation read rows.
	 */
	public List<AgentData> readMonitoringDataForDataTypeAndAID(DataType type, List<String> aidList, double seconds) {
		try {
			return statementsExecutor.executeReadMonitoringDataForDataTypeAndAIDStatement(type, aidList, seconds);
		} catch (SQLException | JsonProcessingException exception) {
			throw new ReadDataException(exception);
		}
	}

	/**
	 * Provides reading capability of predefined and hardcoded into the database adaptation goals
	 *
	 * @return List of {@link AdaptationGoal}s
	 */
	public List<AdaptationGoal> readAdaptationGoals() {
		try {
			return statementsExecutor.executeReadAdaptationGoalsStatement();
		} catch (SQLException exception) {
			throw new ReadDataException(exception);
		}
	}

	/**
	 * Provides reading capability of predefined and hardcoded into the database adaptation actions
	 *
	 * @return List of {@link AdaptationAction}
	 */
	public List<AdaptationAction> readAdaptationActions() {
		try {
			return statementsExecutor.executeReadAdaptationActionsStatement();
		} catch (SQLException | JsonProcessingException exception) {
			throw new ReadDataException(exception);
		}
	}

	/**
	 * Provides reading capability of predefined and hardcoded into the database adaptation action
	 *
	 * @param actionId id of the adaptation action to be returned
	 * @return List of {@link AdaptationAction}
	 */
	public AdaptationAction readAdaptationAction(Integer actionId) {
		try {
			return statementsExecutor.executeReadAdaptationActionStatement(actionId);
		} catch (SQLException | JsonProcessingException exception) {
			throw new ReadDataException(exception);
		}
	}

	/**
	 * Provides reading capability of last N rows of system quality data for given goal id
	 *
	 * @param goalId      id of the adaptation goal
	 * @param recordLimit limit of records number
	 * @return List of {@link SystemQuality}
	 */
	public List<SystemQuality> readSystemQualityData(Integer goalId, Integer recordLimit) {
		try {
			return statementsExecutor.executeReadSystemQualityDataStatement(goalId, recordLimit);
		} catch (SQLException exception) {
			throw new ReadDataException(exception);
		}
	}

	private Connection connect(String hostName) throws SQLException {
		String url = format("jdbc:postgresql://%s:5432/%s?user=%s&password=%s", hostName, DATABASE_NAME, USER,
				PASSWORD);
		var properties = new Properties();
		properties.setProperty("rewriteBatchedInserts", "true");
		return DriverManager.getConnection(url, properties);
	}

	private void dropTableIfExists(Connection sqlConnection) throws SQLException {
		try (var statement = sqlConnection.createStatement()) {
			statement.execute(DROP_MONITORING_DATA);
			statement.execute(DROP_ADAPTATION_ACTIONS);
			statement.execute(DROP_ADAPTATION_GOALS);
			statement.execute(DROP_SYSTEM_QUALITY);
		}
	}

	private void createSchema(Connection sqlConnection) throws SQLException, JsonProcessingException {
		try (var statement = sqlConnection.createStatement()) {
			statement.execute(CREATE_MONITORING_DATA);
			statement.execute(CREATE_ADAPTATION_GOALS);
			statement.execute(CREATE_ADAPTATION_ACTIONS);
			statement.execute(CREATE_SYSTEM_QUALITY);
			statement.execute(CREATE_HYPERTABLE);
			statement.execute(SET_HYPERTABLE_CHUNK_TO_5_SEC);
			statement.execute(CREATE_MONITORING_INDEX);
			statement.execute(CREATE_SYSTEM_QUALITY_HYPERTABLE);
			statement.execute(SET_SYSTEM_QUALITY_HYPERTABLE_CHUNK_TO_5_SEC);
			statement.executeUpdate(INSERT_ADAPTATION_GOALS);
			for (var action : getAdaptationActions()) {
				statementsExecutor.executeWriteStatement(action);
			}
		}
	}
}
