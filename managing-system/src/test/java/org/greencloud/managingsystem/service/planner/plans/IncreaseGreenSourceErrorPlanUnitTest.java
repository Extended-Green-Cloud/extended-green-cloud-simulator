package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.agent.DataType.GREEN_SOURCE_MONITORING;
import static com.database.knowledge.domain.agent.DataType.HEALTH_CHECK;
import static com.database.knowledge.domain.agent.DataType.WEATHER_SHORTAGES;
import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.greencloud.managingsystem.service.planner.plans.IncrementGreenSourceErrorPlan.PERCENTAGE_DIFFERENCE;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.HealthCheck;
import com.database.knowledge.domain.agent.greensource.ImmutableGreenSourceMonitoringData;
import com.database.knowledge.domain.agent.greensource.WeatherShortages;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.greencloud.commons.agent.AgentType;
import com.greencloud.commons.managingsystem.planner.IncrementGreenSourceErrorParameters;
import com.gui.agents.ManagingAgentNode;

class IncreaseGreenSourceErrorPlanUnitTest {

	@Mock
	private ManagingAgent mockManagingAgent;
	@Mock
	private ManagingAgentNode mockManagingAgentNode;
	@Mock
	private TimescaleDatabase mockDatabase;

	private IncrementGreenSourceErrorPlan incrementGreenSourceErrorPlan;

	@BeforeEach
	void init() {
		mockManagingAgent = mock(ManagingAgent.class);
		mockManagingAgentNode = mock(ManagingAgentNode.class);
		mockDatabase = mock(TimescaleDatabase.class);

		incrementGreenSourceErrorPlan = new IncrementGreenSourceErrorPlan(mockManagingAgent);
		doReturn(mockManagingAgentNode).when(mockManagingAgent).getAgentNode();
		doReturn(mockDatabase).when(mockManagingAgentNode).getDatabaseClient();
	}

	@Test
	@DisplayName("Test is plan executable")
	void testIsPlanExecutable() {
		doReturn(prepareAgentData()).when(mockDatabase)
				.readMonitoringDataForDataTypes(eq(List.of(HEALTH_CHECK, GREEN_SOURCE_MONITORING)), anyDouble());
		doReturn(prepareWeatherShortageData()).when(mockDatabase)
				.readMonitoringDataForDataTypeAndAID(eq(WEATHER_SHORTAGES), anyList(), anyDouble());

		assertThat(incrementGreenSourceErrorPlan.isPlanExecutable()).isTrue();
	}

	@Test
	@DisplayName("Test is plan executable - no agents being alive/with correct percentage")
	void testIsPlanExecutableForNoCorrectPercentage() {
		doReturn(Collections.emptyList()).when(mockDatabase)
				.readMonitoringDataForDataTypes(eq(List.of(HEALTH_CHECK, GREEN_SOURCE_MONITORING)), anyDouble());

		assertThat(incrementGreenSourceErrorPlan.isPlanExecutable()).isFalse();
	}

	@Test
	@DisplayName("Test is plan executable - no agents with correct weather shortage count")
	void testIsPlanExecutableForNoCorrectWeatherShortageCount() {
		doReturn(prepareAgentData()).when(mockDatabase)
				.readMonitoringDataForDataTypes(eq(List.of(HEALTH_CHECK, GREEN_SOURCE_MONITORING)), anyDouble());
		doReturn(prepareEmptyWeatherShortageData()).when(mockDatabase)
				.readMonitoringDataForDataTypeAndAID(eq(WEATHER_SHORTAGES), anyList(), anyDouble());

		assertThat(incrementGreenSourceErrorPlan.isPlanExecutable()).isFalse();
	}

	@Test
	@DisplayName("Test constructing adaptation plan for empty map")
	void testConstructAdaptationPlanEmpty() {
		incrementGreenSourceErrorPlan.setGreenSourcesPowerShortages(Collections.emptyMap());

		assertThat(incrementGreenSourceErrorPlan.constructAdaptationPlan()).isNull();
	}

	@Test
	@DisplayName("Test constructing adaptation plan")
	void testConstructAdaptationPlan() {
		incrementGreenSourceErrorPlan.setGreenSourcesPowerShortages(prepareGreenSourceWeatherShortageData());

		assertThat(incrementGreenSourceErrorPlan.constructAdaptationPlan())
				.matches((data) -> data.getTargetAgent().getName().equals("test_gs2")
						&& data.getActionParameters() instanceof IncrementGreenSourceErrorParameters
						&& ((IncrementGreenSourceErrorParameters) data.getActionParameters()).getPercentageChange()
						== PERCENTAGE_DIFFERENCE
				);
	}

	@Test
	@DisplayName("Test get alive green sources")
	void testGetAliveGreenSources() {
		var mockData = prepareAgentData();
		var result = incrementGreenSourceErrorPlan.getAliveGreenSources(mockData);

		assertThat(result)
				.hasSize(3)
				.matches((data) -> List.of("test_gs1", "test_gs2", "test_gs3").containsAll(data));
	}

	@Test
	@DisplayName("Test getting green sources with prediction errors")
	void testGetGreenSourcesWithErrors() {
		var mockData = prepareAgentData();
		var mockAliveAgents = List.of("test_gs1", "test_gs3");

		var result = incrementGreenSourceErrorPlan.getGreenSourcesWithErrors(mockData, mockAliveAgents);

		assertThat(result)
				.hasSize(1)
				.containsEntry("test_gs1", 0.02);
	}

	@Test
	@DisplayName("Test get green source with power shortage map")
	void testGetGreenSourcesWithPowerShortages() {
		var agentsOfInterest = Set.of("test_gs1", "test_gs3", "test_gs2");
		doReturn(prepareWeatherShortageData()).when(mockDatabase)
				.readMonitoringDataForDataTypeAndAID(eq(WEATHER_SHORTAGES), eq(agentsOfInterest.stream().toList()),
						anyDouble());
		var expectedResult = Map.of("test_gs1", 10, "test_gs2", 7, "test_gs3", 18);

		var result = incrementGreenSourceErrorPlan.getGreenSourcesWithPowerShortages(agentsOfInterest);

		assertThat(result)
				.hasSize(3)
				.containsExactlyInAnyOrderEntriesOf(expectedResult);
	}

	private Map<String, Integer> prepareGreenSourceWeatherShortageData() {
		return Map.of(
				"test_gs1", 10,
				"test_gs2", 12,
				"test_gs3", 4
		);
	}

	private List<AgentData> prepareWeatherShortageData() {
		return List.of(
				new AgentData(now(), "test_gs1", WEATHER_SHORTAGES, new WeatherShortages(1, 1000)),
				new AgentData(now(), "test_gs1", WEATHER_SHORTAGES, new WeatherShortages(2, 1000)),
				new AgentData(now(), "test_gs2", WEATHER_SHORTAGES, new WeatherShortages(3, 1000)),
				new AgentData(now(), "test_gs2", WEATHER_SHORTAGES, new WeatherShortages(4, 1000)),
				new AgentData(now(), "test_gs3", WEATHER_SHORTAGES, new WeatherShortages(5, 1000)),
				new AgentData(now(), "test_gs1", WEATHER_SHORTAGES, new WeatherShortages(7, 1000)),
				new AgentData(now(), "test_gs3", WEATHER_SHORTAGES, new WeatherShortages(3, 1000)),
				new AgentData(now(), "test_gs3", WEATHER_SHORTAGES, new WeatherShortages(10, 1000))
		);
	}

	private List<AgentData> prepareEmptyWeatherShortageData() {
		return List.of(
				new AgentData(now(), "test_gs1", WEATHER_SHORTAGES, new WeatherShortages(0, 1000)),
				new AgentData(now(), "test_gs2", WEATHER_SHORTAGES, new WeatherShortages(0, 1000)),
				new AgentData(now(), "test_gs3", WEATHER_SHORTAGES, new WeatherShortages(0, 1000))
		);
	}

	private List<AgentData> prepareAgentData() {
		var healthCheck1 = new HealthCheck(true, AgentType.GREEN_SOURCE);
		var healthCheck2 = new HealthCheck(true, AgentType.GREEN_SOURCE);
		var healthCheck3 = new HealthCheck(true, AgentType.GREEN_SOURCE);
		var data1 = ImmutableGreenSourceMonitoringData.builder()
				.currentMaximumCapacity(10)
				.currentTraffic(0.8)
				.successRatio(0.7)
				.weatherPredictionError(0.02)
				.build();
		var data2 = ImmutableGreenSourceMonitoringData.builder()
				.currentMaximumCapacity(10)
				.currentTraffic(0.8)
				.successRatio(0.7)
				.weatherPredictionError(0.05)
				.build();
		var data3 = ImmutableGreenSourceMonitoringData.builder()
				.currentMaximumCapacity(10)
				.currentTraffic(0.8)
				.successRatio(0.7)
				.weatherPredictionError(1.0)
				.build();

		return List.of(
				new AgentData(now(), "test_gs1", HEALTH_CHECK, healthCheck1),
				new AgentData(now(), "test_gs2", HEALTH_CHECK, healthCheck2),
				new AgentData(now(), "test_gs3", HEALTH_CHECK, healthCheck3),
				new AgentData(now(), "test_gs1", GREEN_SOURCE_MONITORING, data1),
				new AgentData(now(), "test_gs2", GREEN_SOURCE_MONITORING, data2),
				new AgentData(now(), "test_gs3", GREEN_SOURCE_MONITORING, data3)
		);
	}
}
