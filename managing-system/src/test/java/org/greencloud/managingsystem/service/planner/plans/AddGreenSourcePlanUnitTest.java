package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static com.google.common.collect.ImmutableList.of;
import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_TIME_PERIOD;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.server.ImmutableServerMonitoringData;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.greencloud.commons.args.agent.greenenergy.GreenEnergyAgentArgs;
import com.greencloud.commons.args.agent.server.ImmutableServerAgentArgs;
import com.greencloud.commons.args.agent.server.ServerAgentArgs;
import com.greencloud.commons.managingsystem.planner.AddGreenSourceActionParameters;
import com.greencloud.commons.scenario.ScenarioStructureArgs;
import com.gui.agents.ManagingAgentNode;

import jade.core.Location;

@ExtendWith(MockitoExtension.class)
class AddGreenSourcePlanUnitTest {

	private static final double CURRENT_TRAFFIC = 100;

	@Mock
	private ManagingAgent managingAgent;
	@Mock
	private ManagingAgentNode managingAgentNode;
	@Mock
	private TimescaleDatabase timescaleDatabase;

	private AddGreenSourcePlan addGreenSourcePlan;
	private ScenarioStructureArgs greenCloudStructure;
	private String serverName;

	@BeforeEach
	void init() {
		addGreenSourcePlan = new AddGreenSourcePlan(managingAgent);
		ServerAgentArgs server1AgentArgs = ImmutableServerAgentArgs.builder()
				.jobProcessingLimit("200")
				.name("Server1")
				.latitude("latitude")
				.longitude("longitude")
				.maximumCapacity("200")
				.ownerCloudNetwork("CNA1")
				.price("5.0")
				.build();
		ServerAgentArgs server2AgentArgs = ImmutableServerAgentArgs.builder()
				.jobProcessingLimit("200")
				.name("Server2")
				.latitude("latitude")
				.longitude("longitude")
				.maximumCapacity("200")
				.ownerCloudNetwork("CNA1")
				.price("5.0")
				.build();
		greenCloudStructure = new ScenarioStructureArgs(null, null, emptyList(),
				List.of(server1AgentArgs, server2AgentArgs), emptyList(), emptyList());

		when(managingAgent.getAgentNode()).thenReturn(managingAgentNode);
		when((managingAgentNode.getDatabaseClient())).thenReturn(timescaleDatabase);
	}

	@ParameterizedTest
	@CsvSource({ "0,0,false", "10,10,false", "15,15,false", "10,30,true", "25,10,true", "50,0,true" })
	void shouldReturnThatPlanIsNotExecutable(int backUpPowerValue1, int backUpPowerValue2, boolean expectedResult) {
		// given
		serverName = "Server1";
		when(timescaleDatabase.readMonitoringDataForDataTypes(of(SERVER_MONITORING), MONITOR_SYSTEM_DATA_TIME_PERIOD))
				.thenReturn(generateTestDataForTrafficValue(backUpPowerValue1, backUpPowerValue2));

		// when
		boolean result = addGreenSourcePlan.isPlanExecutable();

		// then
		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	void shouldConstructPlan() {
		// given
		var backUpPowerValue = 30;
		serverName = "Server2";
		when(managingAgent.getGreenCloudStructure()).thenReturn(greenCloudStructure);
		when(managingAgent.getContainerLocations("CNA1")).thenReturn(null);
		when(managingAgent.getContainerLocations("Main-Container")).thenReturn(mock(Location.class));
		when(timescaleDatabase.readMonitoringDataForDataTypes(of(SERVER_MONITORING), MONITOR_SYSTEM_DATA_TIME_PERIOD))
				.thenReturn(generateTestDataForTrafficValue(backUpPowerValue, backUpPowerValue + 20));
		addGreenSourcePlan.isPlanExecutable();

		// when
		var result = addGreenSourcePlan.constructAdaptationPlan();

		// then
		assertThat(result)
				.isNotNull()
				.matches(plan -> plan.getActionParameters() instanceof AddGreenSourceActionParameters);
		var params = (AddGreenSourceActionParameters) addGreenSourcePlan.getActionParameters();
		assertThat(params)
				.matches(p -> ((GreenEnergyAgentArgs) p.getAgentsArguments().get(1)).getOwnerSever().equals("Server2"));
	}

	private List<AgentData> generateTestDataForTrafficValue(Integer backUpPowerValue1, Integer backUpPowerValue2) {
		return of(
				new AgentData(now(), "Server1", SERVER_MONITORING, ImmutableServerMonitoringData.builder()
						.successRatio(1.0)
						.currentMaximumCapacity(100)
						.currentTraffic(CURRENT_TRAFFIC)
						.currentBackUpPowerUsage(backUpPowerValue1)
						.build()),
				new AgentData(now(), serverName, SERVER_MONITORING, ImmutableServerMonitoringData.builder()
						.successRatio(1.0)
						.currentMaximumCapacity(100)
						.currentTraffic(CURRENT_TRAFFIC)
						.currentBackUpPowerUsage(backUpPowerValue2)
						.build()));
	}
}
