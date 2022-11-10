package com.database.knowledge.timescale;

import static com.database.knowledge.timescale.TimescaleCommands.CREATE_HYPERTABLE;
import static com.database.knowledge.timescale.TimescaleCommands.CREATE_MONITORING_DATA;
import static com.database.knowledge.timescale.TimescaleCommands.DROP_MONITORING_DATA;
import static com.database.knowledge.timescale.TimescaleCommands.SET_HYPERTABLE_CHUNK_TO_15_SEC;
import static com.database.knowledge.timescale.TimescaleQueries.GET_LAST_5_SEC_DATA;
import static com.database.knowledge.timescale.TimescaleQueries.INSERT_MONITORING_DATA;
import static java.lang.String.format;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.postgresql.util.PGobject;

import com.database.knowledge.domain.AgentData;
import com.database.knowledge.domain.DataType;
import com.database.knowledge.domain.MonitoringData;
import com.database.knowledge.timescale.exception.ClosingDatabaseException;
import com.database.knowledge.timescale.exception.ConnectDatabaseException;
import com.database.knowledge.timescale.exception.InitDatabaseException;
import com.database.knowledge.timescale.exception.ReadDataException;
import com.database.knowledge.timescale.exception.WriteDataException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class TimescaleDatabase implements Closeable {

	private static final String DATABASE_NAME = "postgres";
	private static final String USER = "postgres";
	private static final String PASSWORD = "password";
	private static final String DATABASE_HOST_NAME = "127.0.0.1";

	private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

	private final Connection sqlConnection;

	public TimescaleDatabase() {
		try {
			sqlConnection = connect();
		} catch (SQLException exception) {
			throw new ConnectDatabaseException(exception);
		}
	}

	@Override
	public void close() {
		try {
			sqlConnection.close();
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
		} catch (SQLException exception) {
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
	public void writeData(String aid, DataType dataType, MonitoringData data) {
		try {
			executeWriteStatement(aid, dataType, data);
		} catch (SQLException | JsonProcessingException exception) {
			throw new WriteDataException(exception);
		}
	}

	public void executeWriteStatement(String aid, DataType dataType, MonitoringData data) throws SQLException,
			JsonProcessingException {
		var jsonObject = new PGobject();
		jsonObject.setType("json");
		jsonObject.setValue(objectMapper.writeValueAsString(data));
		try (var statement = sqlConnection.prepareStatement(INSERT_MONITORING_DATA)) {
			statement.setString(1, aid);
			statement.setString(2, dataType.toString());
			statement.setObject(3, jsonObject);
			statement.executeUpdate();
		}
	}

	/**
	 * Provides reading capability for Managing Agent. Provides data saved to database by agents in the last 5 seconds.
	 *
	 * @return List of {@link AgentData}, which are immutable java records which represent in 1:1 relation read rows.
	 */
	public List<AgentData> readData() {
		try {
			return executeReadStatement();
		} catch (SQLException | JsonProcessingException exception) {
			throw new ReadDataException(exception);
		}
	}

	private List<AgentData> executeReadStatement() throws SQLException, JsonProcessingException {
		try (var statement = sqlConnection.prepareStatement(GET_LAST_5_SEC_DATA)) {
			try (var resultSet = statement.executeQuery()) {
				var result = new ArrayList<AgentData>();
				while (resultSet.next()) {
					var type = DataType.valueOf(resultSet.getString(3));
					var agentData = new AgentData(
							resultSet.getTimestamp(1).toInstant(),
							resultSet.getString(2),
							type,
							objectMapper.readValue(resultSet.getObject(4).toString(), type.getDataTypeClass())
					);
					result.add(agentData);
				}
				return result;
			}
		}
	}

	private Connection connect() throws SQLException {
		String url = format("jdbc:postgresql://%s:5432/%s?user=%s&password=%s", DATABASE_HOST_NAME, DATABASE_NAME, USER,
				PASSWORD);
		var properties = new Properties();
		properties.setProperty("rewriteBatchedInserts", "true");
		return DriverManager.getConnection(url, properties);
	}

	private void dropTableIfExists(Connection sqlConnection) throws SQLException {
		try (var statement = sqlConnection.createStatement()) {
			statement.execute(DROP_MONITORING_DATA);
		}
	}

	private void createSchema(Connection sqlConnection) throws SQLException {
		try (var statement = sqlConnection.createStatement()) {
			statement.execute(CREATE_MONITORING_DATA);
		}

		try (var statement = sqlConnection.createStatement()) {
			statement.execute(CREATE_HYPERTABLE);
			statement.execute(SET_HYPERTABLE_CHUNK_TO_15_SEC);
		}
	}
}
