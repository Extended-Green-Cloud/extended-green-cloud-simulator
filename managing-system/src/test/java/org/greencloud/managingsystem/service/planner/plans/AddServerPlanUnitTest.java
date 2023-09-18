package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static com.database.knowledge.domain.goal.GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO;
import static com.google.common.collect.ImmutableList.of;
import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_TIME_PERIOD;
import static org.greencloud.managingsystem.service.planner.plans.AddServerPlan.TRAFFIC_LOAD_THRESHOLD;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.AbstractMap;
import java.util.List;

import org.greencloud.commons.args.agent.server.factory.ImmutableServerArgs;
import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.service.mobility.MobilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.server.ImmutableServerMonitoringData;
import com.database.knowledge.timescale.TimescaleDatabase;
import org.greencloud.commons.args.agent.server.factory.ServerArgs;
import org.greencloud.commons.args.adaptation.system.AddServerActionParameters;
import org.greencloud.commons.args.scenario.ScenarioStructureArgs;
import com.gui.agents.managing.ManagingAgentNode;

import jade.core.AID;
import jade.core.Location;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AddServerPlanUnitTest {

	@Mock
	private ManagingAgent managingAgent;
	@Mock
	private ManagingAgentNode managingAgentNode;
	@Mock
	private TimescaleDatabase timescaleDatabase;

	private MobilityService mobilityService;
	private AddServerPlan addServerPlan;
	private ScenarioStructureArgs greenCloudStructure;

	@BeforeEach
	void init() {
		mobilityService = spy(new MobilityService(managingAgent));
		addServerPlan = new AddServerPlan(managingAgent, MAXIMIZE_JOB_SUCCESS_RATIO);
		ServerArgs serverAgentArgs = ImmutableServerArgs.builder()
				.jobProcessingLimit(200)
				.name("Server1")
				.ownerCloudNetwork("CNA1")
				.price(5.0)
				.build();
		greenCloudStructure = new ScenarioStructureArgs(null, null, emptyList(), List.of(serverAgentArgs), emptyList(),
				emptyList());

		when(managingAgent.getAgentNode()).thenReturn(managingAgentNode);
		when((managingAgentNode.getDatabaseClient())).thenReturn(timescaleDatabase);
		when(managingAgent.move()).thenReturn(mobilityService);
	}

	@ParameterizedTest
	@ValueSource(doubles = { 0.8, 0.9, 0.95 })
	void shouldReturnThatPlanIsNotExecutable(double trafficValue) {
		// given
		when(timescaleDatabase.readLastMonitoringDataForDataTypes(of(SERVER_MONITORING),
				MONITOR_SYSTEM_DATA_TIME_PERIOD))
				.thenReturn(generateTestDataForTrafficValue(trafficValue));

		// when
		boolean result = addServerPlan.isPlanExecutable();

		// then
		assertThat(result).isEqualTo(trafficValue >= TRAFFIC_LOAD_THRESHOLD);
	}

	@Test
	@Disabled
	void shouldConstructPlan() {
		// given
		var trafficValue = 0.9;
		var aid = new AID("test", AID.ISGUID);
		when(managingAgent.getGreenCloudStructure()).thenReturn(greenCloudStructure);
		when(mobilityService.getContainerLocations("CNA1")).thenReturn(null);
		when(mobilityService.getContainerLocations("Main-Container")).thenReturn(
				new AbstractMap.SimpleEntry<>(mock(Location.class), aid));
		when(timescaleDatabase.readLastMonitoringDataForDataTypes(of(SERVER_MONITORING),
				MONITOR_SYSTEM_DATA_TIME_PERIOD))
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
				.successRatio(1.0)
				.currentTraffic(trafficValue)
				.isDisabled(false)
				.serverJobs(10)
				.build()));
	}
}
