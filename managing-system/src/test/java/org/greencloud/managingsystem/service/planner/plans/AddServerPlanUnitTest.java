package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static com.google.common.collect.ImmutableList.of;
import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_TIME_PERIOD;
import static org.greencloud.managingsystem.service.planner.plans.AddServerPlan.TRAFFIC_LOAD_THRESHOLD;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.server.ImmutableServerMonitoringData;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.greencloud.commons.args.agent.server.ImmutableServerAgentArgs;
import com.greencloud.commons.args.agent.server.ServerAgentArgs;
import com.greencloud.commons.managingsystem.planner.AddServerActionParameters;
import com.greencloud.commons.scenario.ScenarioStructureArgs;
import com.gui.agents.ManagingAgentNode;

import jade.core.Location;

@ExtendWith(MockitoExtension.class)
class AddServerPlanUnitTest {

	@Mock
	private ManagingAgent managingAgent;
	@Mock
	private ManagingAgentNode managingAgentNode;
	@Mock
	private TimescaleDatabase timescaleDatabase;

	private AddServerPlan addServerPlan;
	private ScenarioStructureArgs greenCloudStructure;

	@BeforeEach
	void init() {
		addServerPlan = new AddServerPlan(managingAgent);
		ServerAgentArgs serverAgentArgs = ImmutableServerAgentArgs.builder()
				.jobProcessingLimit("200")
				.name("Server1")
				.latitude("latitude")
				.longitude("longitude")
				.maximumCapacity("200")
				.ownerCloudNetwork("CNA1")
				.price("5.0")
				.build();
		greenCloudStructure = new ScenarioStructureArgs(null, null, emptyList(), List.of(serverAgentArgs), emptyList(),
				emptyList());

		when(managingAgent.getAgentNode()).thenReturn(managingAgentNode);
		when((managingAgentNode.getDatabaseClient())).thenReturn(timescaleDatabase);
	}

	@ParameterizedTest
	@ValueSource(doubles = { 0.8, 0.9, 0.95 })
	void shouldReturnThatPlanIsNotExecutable(double trafficValue) {
		// given
		when(timescaleDatabase.readMonitoringDataForDataTypes(of(SERVER_MONITORING), MONITOR_SYSTEM_DATA_TIME_PERIOD))
				.thenReturn(generateTestDataForTrafficValue(trafficValue));

		// when
		boolean result = addServerPlan.isPlanExecutable();

		// then
		assertThat(result).isEqualTo(trafficValue >= TRAFFIC_LOAD_THRESHOLD);
	}

	@Test
	void shouldConstructPlan() {
		// given
		var trafficValue = 0.9;
		when(managingAgent.getGreenCloudStructure()).thenReturn(greenCloudStructure);
		when(managingAgent.getContainerLocations("CNA1")).thenReturn(null);
		when(managingAgent.getContainerLocations("Main-Container")).thenReturn(mock(Location.class));
		when(timescaleDatabase.readMonitoringDataForDataTypes(of(SERVER_MONITORING), MONITOR_SYSTEM_DATA_TIME_PERIOD))
				.thenReturn(generateTestDataForTrafficValue(trafficValue));
		addServerPlan.isPlanExecutable();

		// when
		var result = addServerPlan.constructAdaptationPlan();

		// then
		assertThat(result)
				.isNotNull()
				.matches(plan -> plan.getActionParameters() instanceof AddServerActionParameters);
	}

	private List<AgentData> generateTestDataForTrafficValue(Double trafficValue) {
		return of(new AgentData(now(), "Server1", SERVER_MONITORING, ImmutableServerMonitoringData.builder()
				.currentlyExecutedJobs(2)
				.currentlyProcessedJobs(2)
				.successRatio(1.0)
				.currentMaximumCapacity(200)
				.jobProcessingLimit(200)
				.serverPricePerHour(5.0)
				.currentTraffic(trafficValue)
				.build()));
	}
}
