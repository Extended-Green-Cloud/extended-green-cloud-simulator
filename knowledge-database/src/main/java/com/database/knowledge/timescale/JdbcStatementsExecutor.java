package com.database.knowledge.timescale;

import static com.database.knowledge.domain.action.AdaptationActionEnum.getAdaptationActionEnumByName;
import static com.database.knowledge.timescale.DmlQueries.DISABLE_ADAPTATION_ACTION;
import static com.database.knowledge.timescale.DmlQueries.GET_ADAPTATION_ACTION;
import static com.database.knowledge.timescale.DmlQueries.GET_ADAPTATION_ACTIONS;
import static com.database.knowledge.timescale.DmlQueries.GET_ADAPTATION_GOALS;
import static com.database.knowledge.timescale.DmlQueries.GET_ALL_RECORDS_DATA_FOR_DATA_TYPES_AND_TIME;
import static com.database.knowledge.timescale.DmlQueries.GET_AMS_DATA;
import static com.database.knowledge.timescale.DmlQueries.GET_DATA_FOR_DATA_TYPE_AND_AIDS_AND_TIME;
import static com.database.knowledge.timescale.DmlQueries.GET_LAST_1_SEC_DATA;
import static com.database.knowledge.timescale.DmlQueries.GET_LAST_N_QUALITY_DATA_RECORDS_FOR_GOAL;
import static com.database.knowledge.timescale.DmlQueries.GET_LATEST_N_ROWS_FOR_DATA_TYPE_AND_AIDS;
import static com.database.knowledge.timescale.DmlQueries.GET_NEXT_CLIENT_ID;
import static com.database.knowledge.timescale.DmlQueries.GET_UNIQUE_LAST_RECORDS_DATA_FOR_DATA_TYPES;
import static com.database.knowledge.timescale.DmlQueries.GET_UNIQUE_LAST_RECORDS_DATA_FOR_DATA_TYPES_AND_TIME;
import static com.database.knowledge.timescale.DmlQueries.INSERT_ADAPTATION_ACTION;
import static com.database.knowledge.timescale.DmlQueries.INSERT_AMS_DATA;
import static com.database.knowledge.timescale.DmlQueries.INSERT_MONITORING_DATA;
import static com.database.knowledge.timescale.DmlQueries.INSERT_SYSTEM_QUALITY_DATA;
import static com.database.knowledge.timescale.DmlQueries.READ_SYSTEM_START_TIME;
import static com.database.knowledge.timescale.DmlQueries.RELEASE_ADAPTATION_ACTION;
import static com.database.knowledge.timescale.DmlQueries.UPDATE_ADAPTATION_ACTION;
import static org.greencloud.commons.mapper.JsonMapper.getMapper;

import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.util.PGobject;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.domain.action.AdaptationActionTypeEnum;
import com.database.knowledge.domain.agent.AMSData;
import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.DataType;
import com.database.knowledge.domain.agent.MonitoringData;
import com.database.knowledge.domain.goal.AdaptationGoal;
import com.database.knowledge.domain.goal.GoalEnum;
import com.database.knowledge.domain.systemquality.SystemQuality;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Statements executor methods are to be used ONLY INTERNALLY by the TimescaleDatabase
 */
public class JdbcStatementsExecutor {

	private final Connection sqlConnection;

	public JdbcStatementsExecutor(Connection sqlConnection) {
		this.sqlConnection = sqlConnection;
	}

	int executeSequenceStatement() throws SQLException {
		try (var statement = sqlConnection.prepareStatement(GET_NEXT_CLIENT_ID)) {
			var result = statement.executeQuery();
			result.next();
			return result.getInt(1);
		}
	}

	void executeWriteStatement(String aid, DataType dataType, MonitoringData data) throws SQLException,
			JsonProcessingException {
		var jsonObject = new PGobject();
		jsonObject.setType("json");
		jsonObject.setValue(getMapper().writeValueAsString(data));
		try (var statement = sqlConnection.prepareStatement(INSERT_MONITORING_DATA)) {
			statement.setString(1, aid);
			statement.setString(2, dataType.toString());
			statement.setObject(3, jsonObject);
			statement.executeUpdate();
		}
	}

	void executeWriteStatement(AdaptationAction adaptationAction) throws JsonProcessingException, SQLException {
		var jsonObject = new PGobject();
		jsonObject.setType("json");
		jsonObject.setValue(getMapper().writeValueAsString(adaptationAction.getActionResults()));
		try (var statement = sqlConnection.prepareStatement(INSERT_ADAPTATION_ACTION)) {
			statement.setInt(1, adaptationAction.getActionId());
			statement.setString(2, adaptationAction.getAction().getName());
			statement.setString(3, adaptationAction.getType().toString());
			statement.setInt(4, adaptationAction.getGoal().getAdaptationGoalId());
			statement.setObject(5, jsonObject);
			statement.setBoolean(6, adaptationAction.getAvailable());
			statement.setInt(7, adaptationAction.getRuns());
			statement.setDouble(8, adaptationAction.getExecutionDuration());
			statement.executeUpdate();
		}
	}

	void executeWriteStatement(Integer adaptationGoalId, Double quality) throws SQLException {
		try (var statement = sqlConnection.prepareStatement(INSERT_SYSTEM_QUALITY_DATA)) {
			statement.setInt(1, adaptationGoalId);
			statement.setDouble(2, quality);
			statement.executeUpdate();
		}
	}

	void executeWriteStatement(String amsName, String amsAddress) throws SQLException {
		try (var statement = sqlConnection.prepareStatement(INSERT_AMS_DATA)) {
			statement.setString(1, amsName);
			statement.setString(2, amsAddress);
			statement.executeUpdate();
		}
	}

	Instant executeReadSystemStartStatement() throws SQLException {
		try (var statement = sqlConnection.prepareStatement(READ_SYSTEM_START_TIME)) {
			var result = statement.executeQuery();
			result.next();
			return result.getTimestamp("start_time").toInstant();
		}
	}

	void executeUpdateActionStatement(AdaptationAction adaptationAction)
			throws JsonProcessingException, SQLException {
		var jsonObject = new PGobject();
		jsonObject.setType("json");
		jsonObject.setValue(getMapper().writeValueAsString(adaptationAction.getActionResults()));
		try (var statement = sqlConnection.prepareStatement(UPDATE_ADAPTATION_ACTION)) {
			statement.setObject(1, jsonObject);
			statement.setInt(2, adaptationAction.getRuns());
			statement.setDouble(3, adaptationAction.getExecutionDuration());
			statement.setInt(4, adaptationAction.getActionId());
			statement.executeUpdate();
		}
	}

	void executeSetAvailabilityActionStatement(Integer actionId, boolean isAvailable) throws SQLException {
		try (var statement = sqlConnection.prepareStatement(isAvailable
				? RELEASE_ADAPTATION_ACTION
				: DISABLE_ADAPTATION_ACTION)) {
			statement.setInt(1, actionId);
			statement.executeUpdate();
		}
	}

	List<AgentData> executeReadMonitoringDataStatement() throws SQLException, JsonProcessingException {
		try (var statement = sqlConnection.prepareStatement(GET_LAST_1_SEC_DATA)) {
			var resultSet = statement.executeQuery();
			return readAgentDataFromResultSet(resultSet);
		}
	}

	List<AgentData> executeReadMonitoringDataForDataTypesStatement(List<DataType> dataTypes, double seconds)
			throws SQLException, JsonProcessingException {
		try (var statement = sqlConnection.prepareStatement(GET_ALL_RECORDS_DATA_FOR_DATA_TYPES_AND_TIME)) {
			final Object[] dataTypeNames = dataTypes.stream().map(DataType::toString).toArray();
			final Array array = statement.getConnection().createArrayOf("text", dataTypeNames);
			statement.setArray(1, array);
			statement.setDouble(2, seconds);
			var resultSet = statement.executeQuery();
			return readAgentDataFromResultSet(resultSet);
		}
	}

	List<AgentData> executeReadLastMonitoringDataForDataTypesStatement(List<DataType> dataTypes, double seconds)
			throws SQLException, JsonProcessingException {
		try (var statement = sqlConnection.prepareStatement(GET_UNIQUE_LAST_RECORDS_DATA_FOR_DATA_TYPES_AND_TIME)) {
			final Object[] dataTypeNames = dataTypes.stream().map(DataType::toString).toArray();
			final Array array = statement.getConnection().createArrayOf("text", dataTypeNames);
			statement.setArray(1, array);
			statement.setDouble(2, seconds);
			var resultSet = statement.executeQuery();
			return readAgentDataFromResultSet(resultSet);
		}
	}

	List<AgentData> executeReadLastMonitoringDataForDataTypesStatement(List<DataType> dataTypes)
			throws SQLException, JsonProcessingException {
		try (var statement = sqlConnection.prepareStatement(GET_UNIQUE_LAST_RECORDS_DATA_FOR_DATA_TYPES)) {
			final Object[] dataTypeNames = dataTypes.stream().map(DataType::toString).toArray();
			final Array array = statement.getConnection().createArrayOf("text", dataTypeNames);
			statement.setArray(1, array);
			var resultSet = statement.executeQuery();
			return readAgentDataFromResultSet(resultSet);
		}
	}

	List<AgentData> executeReadMonitoringDataForDataTypeAndAIDStatement(DataType type, List<String> aid, double seconds)
			throws SQLException, JsonProcessingException {
		try (var statement = sqlConnection.prepareStatement(GET_DATA_FOR_DATA_TYPE_AND_AIDS_AND_TIME)) {
			final Array array = statement.getConnection().createArrayOf("text", aid.toArray());
			statement.setString(1, type.toString());
			statement.setArray(2, array);
			statement.setDouble(3, seconds);
			var resultSet = statement.executeQuery();
			return readAgentDataFromResultSet(resultSet);
		}
	}

	List<AgentData> executeMultipleRowsReadMonitoringDataForDataTypeAndAID(DataType type, List<String> aid, int rows)
			throws SQLException, JsonProcessingException {
		try (var statement = sqlConnection.prepareStatement(GET_LATEST_N_ROWS_FOR_DATA_TYPE_AND_AIDS)) {
			final Array array = statement.getConnection().createArrayOf("text", aid.toArray());
			statement.setString(1, type.toString());
			statement.setArray(2, array);
			statement.setInt(3, rows);
			var resultSet = statement.executeQuery();
			return readAgentDataFromResultSet(resultSet);
		}
	}

	List<AdaptationGoal> executeReadAdaptationGoalsStatement() throws SQLException {
		try (var statement = sqlConnection.prepareStatement(GET_ADAPTATION_GOALS)) {
			try (var resultSet = statement.executeQuery()) {
				var result = new ArrayList<AdaptationGoal>();
				while (resultSet.next()) {
					var adaptationGoal = new AdaptationGoal(
							resultSet.getInt(1), // adaptation goal id,
							resultSet.getString(2), // adaptation goal name
							resultSet.getDouble(3), // adaptation goal threshold
							resultSet.getBoolean(4), // adaptation goal is above threshold
							resultSet.getDouble(5) // adaptation goal weight
					);
					result.add(adaptationGoal);
				}
				return result;
			}
		}
	}

	List<AdaptationAction> executeReadAdaptationActionsStatement() throws SQLException, JsonProcessingException {
		try (var statement = sqlConnection.prepareStatement(GET_ADAPTATION_ACTIONS)) {
			try (var resultSet = statement.executeQuery()) {
				var result = new ArrayList<AdaptationAction>();
				while (resultSet.next()) {
					result.add(readAdaptationActionFromResultSet(resultSet));
				}
				return result;
			}
		}
	}

	List<AMSData> executeReadAMSDataStatement() throws SQLException {
		try (var statement = sqlConnection.prepareStatement(GET_AMS_DATA)) {
			var resultSet = statement.executeQuery();
			return readAMSDataFromResultSet(resultSet);
		}
	}

	AdaptationAction executeReadAdaptationActionStatement(Integer actionId)
			throws SQLException, JsonProcessingException {
		try (var statement = sqlConnection.prepareStatement(GET_ADAPTATION_ACTION)) {
			statement.setInt(1, actionId);
			try (var resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return readAdaptationActionFromResultSet(resultSet);
				}
			}
		}
		return null;
	}

	List<SystemQuality> executeReadSystemQualityDataStatement(Integer goalId, Integer limit)
			throws SQLException {
		try (var statement = sqlConnection.prepareStatement(GET_LAST_N_QUALITY_DATA_RECORDS_FOR_GOAL)) {
			statement.setInt(1, goalId);
			statement.setInt(2, limit);
			var resultSet = statement.executeQuery();
			return readSystemQualityDataFromResultSet(resultSet);
		}
	}

	private AdaptationAction readAdaptationActionFromResultSet(ResultSet resultSet)
			throws SQLException, JsonProcessingException {
		return new AdaptationAction(
				resultSet.getInt(1), // action id
				getAdaptationActionEnumByName(resultSet.getString(2)), // action name
				AdaptationActionTypeEnum.valueOf(resultSet.getObject(3).toString()), // action type
				GoalEnum.getByGoalId(resultSet.getInt(4)), // action's goal id
				getMapper().readValue(resultSet.getObject(5).toString(), new TypeReference<>() {
				}), // action_results
				resultSet.getBoolean(6), // availability
				resultSet.getInt(7), // runs
				resultSet.getDouble(8) // execution duration
		);
	}

	private List<AgentData> readAgentDataFromResultSet(ResultSet resultSet) throws SQLException,
			JsonProcessingException {
		var result = new ArrayList<AgentData>();
		while (resultSet.next()) {
			var type = DataType.valueOf(resultSet.getString("data_type"));
			var agentData = new AgentData(
					resultSet.getTimestamp("time").toInstant(),
					resultSet.getString("aid"),
					type,
					getMapper().readValue(resultSet.getObject("data").toString(), type.getDataTypeClass())
			);
			result.add(agentData);
		}
		return result;
	}

	private List<SystemQuality> readSystemQualityDataFromResultSet(ResultSet resultSet) throws SQLException {
		var result = new ArrayList<SystemQuality>();
		while (resultSet.next()) {
			var agentData = new SystemQuality(
					resultSet.getTimestamp(1).toInstant(), // record time
					resultSet.getInt(2), // goal id
					resultSet.getDouble(3) // quality
			);
			result.add(agentData);
		}
		return result;
	}

	private List<AMSData> readAMSDataFromResultSet(ResultSet resultSet) throws SQLException {
		var result = new ArrayList<AMSData>();
		while (resultSet.next()) {
			var amsData = new AMSData(resultSet.getString("name"), resultSet.getString("address"));
			result.add(amsData);
		}
		return result;
	}
}
