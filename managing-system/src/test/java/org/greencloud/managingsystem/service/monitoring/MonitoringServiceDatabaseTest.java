package org.greencloud.managingsystem.service.monitoring;

import static com.database.knowledge.domain.agent.DataType.HEALTH_CHECK;
import static com.greencloud.commons.agent.AgentType.GREEN_SOURCE;
import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.List;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.HealthCheck;
import com.database.knowledge.domain.goal.AdaptationGoal;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.gui.agents.ManagingAgentNode;

class MonitoringServiceDatabaseTest {

	@Mock
	private ManagingAgent mockManagingAgent;
	@Mock
	private ManagingAgentNode mockAgentNode;

	private MonitoringService monitoringService;
	private TimescaleDatabase database;

	@BeforeEach
	void init() {
		database = new TimescaleDatabase();
		database.initDatabase();

		mockManagingAgent = spy(ManagingAgent.class);
		mockAgentNode = mock(ManagingAgentNode.class);
		monitoringService = new MonitoringService(mockManagingAgent);

		doReturn(mockAgentNode).when(mockManagingAgent).getAgentNode();
		doReturn(database).when(mockAgentNode).getDatabaseClient();
		doNothing().when(mockAgentNode).registerManagingAgent(anyList());
	}

	@AfterEach
	void cleanUp() {
		database.close();
	}

	@Test
	@DisplayName("Test get adaptation goals from the database")
	void testReadSystemAdaptationGoals() {
		var expectedResult = List.of(
				new AdaptationGoal(1, "Maximize job success ratio", 0.8, true, 0.6),
				new AdaptationGoal(2, "Minimize used backup power", 0.2, false, 0.2),
				new AdaptationGoal(3, "Distribute traffic evenly", 0.7, false, 0.2)
		);
		monitoringService.readSystemAdaptationGoals();

		var result = mockManagingAgent.getAdaptationGoalList();

		assertThat(result)
				.as("Managing agent should have 3 goals")
				.hasSize(3)
				.as("Data of the goals should equal to the expected result")
				.usingRecursiveFieldByFieldElementComparator()
				.containsExactlyInAnyOrderElementsOf(expectedResult);
	}

	@Test
	@DisplayName("Test get alive agents for green sources")
	void testGetAliveAgentsForGreenSources() {
		mockHealthCheckData();
		var result = monitoringService.getAliveAgents(GREEN_SOURCE);

		assertThat(result)
				.hasSize(3)
				.matches((data) -> List.of("test_gs1", "test_gs2", "test_gs3").containsAll(data));
	}

	private void mockHealthCheckData() {
		var healthCheck1 = new HealthCheck(true, GREEN_SOURCE);
		var healthCheck2 = new HealthCheck(true, GREEN_SOURCE);
		var healthCheck3 = new HealthCheck(true, GREEN_SOURCE);

		var mockData = List.of(
				new AgentData(now(), "test_gs1", HEALTH_CHECK, healthCheck1),
				new AgentData(now(), "test_gs2", HEALTH_CHECK, healthCheck2),
				new AgentData(now(), "test_gs3", HEALTH_CHECK, healthCheck3)
		);

		mockData.forEach(data -> database.writeMonitoringData(data.aid(), data.dataType(), data.monitoringData()));
	}
}
