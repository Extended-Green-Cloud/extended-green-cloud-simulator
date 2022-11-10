package timescale;

import static com.database.knowledge.domain.DataType.PROCESSED_API_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.database.knowledge.domain.AgentData;
import com.database.knowledge.domain.DataType;
import com.database.knowledge.domain.MonitoringData;
import com.database.knowledge.timescale.TimescaleDatabase;

import domain.agent.monitoring.ImmutableProcessedApiRequest;

@ExtendWith(MockitoExtension.class)
class TimescaleDatabaseIntegrationTest {

	private TimescaleDatabase database;

	@BeforeEach
	void init() {
		database = new TimescaleDatabase();
		database.initDatabase();
	}

	@AfterEach
	void cleanUp() {
		database.close();
	}

	@Test
	void shouldSuccessfullySaveDataToDatabase() {
		// given
		String aid = "test_agent";
		DataType dataType = PROCESSED_API_REQUEST;
		MonitoringData monitoringData = ImmutableProcessedApiRequest.builder()
				.callType("testCallType")
				.requestedTimeslot("testTimeslot")
				.requestedType("testRequestType")
				.build();

		// when
		database.writeData(aid, dataType, monitoringData);

		// then
		var result = database.readData();
		assertThat(result)
				.as("After insertion there should be data in the timescaledb.")
				.isNotEmpty();
		assertThat(result.get(0))
				.as("Returned data should be of AgentData record type")
				.isInstanceOf(AgentData.class)
				.as("Returned data should be equal to the saved one")
				.matches(agentData -> agentData.aid().equals(aid))
				.matches(agentData -> agentData.dataType().equals(PROCESSED_API_REQUEST))
				.matches(agentData -> agentData.monitoringData() instanceof ImmutableProcessedApiRequest)
				.matches(agentData -> agentData.monitoringData().equals(monitoringData));
	}
}
