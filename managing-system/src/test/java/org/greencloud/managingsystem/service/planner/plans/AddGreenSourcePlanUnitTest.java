package org.greencloud.managingsystem.service.planner.plans;

import static com.database.knowledge.domain.agent.DataType.HEALTH_CHECK;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static com.database.knowledge.domain.goal.GoalEnum.MINIMIZE_USED_BACKUP_POWER;
import static com.google.common.collect.ImmutableList.of;
import static com.greencloud.connector.factory.constants.AgentTemplatesConstants.TEMPLATE_SERVER_RESOURCES;
import static jade.core.AID.ISGUID;
import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_HEALTH_PERIOD;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_TIME_PERIOD;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.AbstractMap;
import java.util.List;

import org.greencloud.commons.args.adaptation.system.AddGreenSourceActionParameters;
import org.greencloud.commons.args.agent.AgentType;
import org.greencloud.commons.args.agent.greenenergy.factory.GreenEnergyArgs;
import org.greencloud.commons.args.agent.server.factory.ImmutableServerArgs;
import org.greencloud.commons.args.agent.server.factory.ServerArgs;
import org.greencloud.commons.args.scenario.ScenarioStructureArgs;
import org.greencloud.gui.agents.managing.ManagingAgentNode;
import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.service.mobility.MobilityService;
import org.greencloud.managingsystem.service.monitoring.MonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.HealthCheck;
import com.database.knowledge.domain.agent.server.ImmutableServerMonitoringData;
import com.database.knowledge.timescale.TimescaleDatabase;

import jade.core.AID;
import jade.core.Location;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AddGreenSourcePlanUnitTest {

	private static final double CURRENT_TRAFFIC = 100;

	@Mock
	private ManagingAgent managingAgent;
	@Mock
	private ManagingAgentNode managingAgentNode;
	@Mock
	private TimescaleDatabase timescaleDatabase;

	private MobilityService mobilityService;
	private MonitoringService monitoringService;
	private AddGreenSourcePlan addGreenSourcePlan;
	private ScenarioStructureArgs greenCloudStructure;
	private String serverName;

	@BeforeEach
	void init() {
		mobilityService = spy(new MobilityService(managingAgent));
		monitoringService = spy(new MonitoringService(managingAgent));

		addGreenSourcePlan = new AddGreenSourcePlan(managingAgent, MINIMIZE_USED_BACKUP_POWER);
		ServerArgs server1AgentArgs = ImmutableServerArgs.builder()
				.jobProcessingLimit(200)
				.name("Server1")
				.ownerRegionalManager("CNA1")
				.price(5.0)
				.maxPower(100)
				.idlePower(10)
				.resources(TEMPLATE_SERVER_RESOURCES)
				.build();
		ServerArgs server2AgentArgs = ImmutableServerArgs.builder()
				.jobProcessingLimit(200)
				.name("Server2")
				.ownerRegionalManager("CNA1")
				.price(5.0)
				.maxPower(100)
				.idlePower(10)
				.resources(TEMPLATE_SERVER_RESOURCES)
				.build();
		greenCloudStructure = new ScenarioStructureArgs(null, null, emptyList(),
				List.of(server1AgentArgs, server2AgentArgs), emptyList(), emptyList());

		when(managingAgent.getAgentNode()).thenReturn(managingAgentNode);
		when((managingAgentNode.getDatabaseClient())).thenReturn(timescaleDatabase);
		when(managingAgent.move()).thenReturn(mobilityService);
		when(managingAgent.monitor()).thenReturn(monitoringService);
	}

	@ParameterizedTest
	@CsvSource({ "0,0,false", "10,10,false", "15,15,false", "10,30,false", "25,10,true", "50,0,true" })
	void shouldReturnThatPlanIsNotExecutable(int backUpPowerValue1, int backUpPowerValue2, boolean expectedResult) {
		// given
		serverName = "Server1";
		when(timescaleDatabase.readMonitoringDataForDataTypes(of(SERVER_MONITORING), MONITOR_SYSTEM_DATA_TIME_PERIOD))
				.thenReturn(generateTestDataForTrafficValue(backUpPowerValue1, backUpPowerValue2));
		when(timescaleDatabase.readLastMonitoringDataForDataTypes(singletonList(SERVER_MONITORING)))
				.thenReturn(generateTestDataForTrafficValue(backUpPowerValue1, backUpPowerValue2).subList(0, 1));
		when(timescaleDatabase.readLastMonitoringDataForDataTypes(singletonList(HEALTH_CHECK),
				MONITOR_SYSTEM_DATA_HEALTH_PERIOD)).thenReturn(generateHealthTestData());
		// when
		boolean result = addGreenSourcePlan.isPlanExecutable();

		// then
		assertThat(result).isEqualTo(expectedResult);
	}

	@Test
	@Disabled
	void shouldConstructPlan() {
		// given
		final AID testAID = new AID("test", ISGUID);
		testAID.addAddresses("test_address");
		var backUpPowerValue = 30;
		serverName = "Server2";
		when(managingAgent.getGreenCloudStructure()).thenReturn(greenCloudStructure);
		when(timescaleDatabase.readMonitoringDataForDataTypes(of(SERVER_MONITORING), MONITOR_SYSTEM_DATA_TIME_PERIOD))
				.thenReturn(generateTestDataForTrafficValue(backUpPowerValue, backUpPowerValue + 20));

		doReturn(null).when(mobilityService).getContainerLocations("CNA1");
		doReturn(new AbstractMap.SimpleImmutableEntry<>(mock(Location.class), testAID)).when(mobilityService)
				.getContainerLocations("Main-Container");

		addGreenSourcePlan.isPlanExecutable();

		// when
		var result = addGreenSourcePlan.constructAdaptationPlan();

		// then
		assertThat(result)
				.isNotNull()
				.matches(plan -> plan.getActionParameters() instanceof AddGreenSourceActionParameters);
		var params = (AddGreenSourceActionParameters) addGreenSourcePlan.getActionParameters();
		assertThat(params)
				.matches(p -> ((GreenEnergyArgs) p.getAgentsArguments().get(1)).getOwnerSever().equals("Server2"));
	}

	private List<AgentData> generateTestDataForTrafficValue(Integer backUpPowerValue1, Integer backUpPowerValue2) {
		return of(
				new AgentData(now(), "Server1", SERVER_MONITORING, ImmutableServerMonitoringData.builder()
						.successRatio(1.0)
						.currentTraffic(CURRENT_TRAFFIC)
						.isDisabled(false)
						.serverJobs(10)
						.idlePowerConsumption(10)
						.currentBackUpPowerTraffic(backUpPowerValue1)
						.currentPowerConsumption(0.7)
						.build()),
				new AgentData(now(), serverName, SERVER_MONITORING, ImmutableServerMonitoringData.builder()
						.successRatio(1.0)
						.currentTraffic(CURRENT_TRAFFIC)
						.serverJobs(10)
						.isDisabled(false)
						.idlePowerConsumption(10)
						.currentBackUpPowerTraffic(backUpPowerValue1)
						.currentPowerConsumption(0.7)
						.build()));
	}

	private List<AgentData> generateHealthTestData() {
		return of(
				new AgentData(now(), "Server1", HEALTH_CHECK, new HealthCheck(true, AgentType.SERVER)),
				new AgentData(now(), serverName, HEALTH_CHECK, new HealthCheck(true, AgentType.SERVER)));
	}
}
