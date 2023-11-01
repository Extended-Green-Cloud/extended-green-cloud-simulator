package org.greencloud.managingsystem.service.executor;

import static com.database.knowledge.domain.action.AdaptationActionEnum.ADD_SERVER;
import static com.database.knowledge.domain.agent.DataType.CLIENT_MONITORING;
import static com.database.knowledge.domain.agent.DataType.CLOUD_NETWORK_MONITORING;
import static com.database.knowledge.domain.agent.DataType.HEALTH_CHECK;
import static com.database.knowledge.domain.goal.GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO;
import static jade.core.AID.ISGUID;
import static java.util.Collections.emptyList;
import static org.greencloud.commons.constants.DFServiceConstants.CNA_SERVICE_TYPE;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.FINISHED;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.IN_PROGRESS;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.ON_BACK_UP;
import static org.greencloud.commons.utils.yellowpages.YellowPagesRegister.search;
import static org.greencloud.managingsystem.service.common.TestAdaptationPlanFactory.getTestAdaptationPlan;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.greencloud.commons.args.agent.AgentType;
import org.greencloud.commons.args.agent.server.factory.ImmutableServerArgs;
import org.greencloud.commons.args.scenario.ScenarioStructureArgs;
import org.greencloud.commons.utils.yellowpages.YellowPagesRegister;
import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.agent.behaviour.executor.InitiateAdaptationActionRequest;
import org.greencloud.managingsystem.agent.behaviour.executor.VerifyAdaptationActionResult;
import org.greencloud.managingsystem.service.common.TestPlanParameters;
import org.greencloud.managingsystem.service.mobility.MobilityService;
import org.greencloud.managingsystem.service.monitoring.MonitoringService;
import org.greencloud.managingsystem.service.planner.plans.AbstractPlan;
import org.greencloud.managingsystem.service.planner.plans.AddServerPlan;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.database.knowledge.domain.agent.HealthCheck;
import com.database.knowledge.domain.agent.client.ImmutableClientMonitoringData;
import com.database.knowledge.domain.agent.cloudnetwork.ImmutableCloudNetworkMonitoringData;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.greencloud.connector.factory.AgentControllerFactory;
import com.greencloud.connector.gui.agents.managing.ManagingAgentNode;

import jade.core.AID;
import jade.core.Location;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Disabled
class ExecutorServiceDatabaseTest {

	private static final Integer TEST_VALUE = 1;
	private static final AID TEST_AID = new AID("test", ISGUID);

	@Mock
	ManagingAgent managingAgent;
	@Mock
	ManagingAgentNode abstractAgentNode;
	@Mock
	Location location;

	MobilityService mobilityService;
	AgentControllerFactory agentFactory;
	AbstractPlan adaptationPlan;
	TimescaleDatabase database;
	MonitoringService monitoringService;
	ExecutorService executorService;
	MockedStatic<YellowPagesRegister> yellowPagesService;

	@BeforeEach
	void init() {
		mobilityService = spy(new MobilityService(managingAgent));
		database = spy(TimescaleDatabase.setUpForTests());
		database.initDatabase();

		agentFactory = mock(AgentControllerFactory.class);
		monitoringService = spy(new MonitoringService(managingAgent));
		executorService = spy(new ExecutorService(managingAgent, agentFactory));
		yellowPagesService = mockStatic(YellowPagesRegister.class);

		when(managingAgent.monitor()).thenReturn(monitoringService);
		when(managingAgent.getAgentNode()).thenReturn(abstractAgentNode);
		when(abstractAgentNode.getDatabaseClient()).thenReturn(database);
		when(managingAgent.move()).thenReturn(mobilityService);

		adaptationPlan = getTestAdaptationPlan(managingAgent, TEST_AID, new TestPlanParameters(TEST_VALUE));
	}

	@AfterEach
	void cleanUp() {
		yellowPagesService.close();
		database.close();
	}

	@Test
	void shouldCorrectlyExecuteAdaptationAction() {
		// given
		initializeData();

		// when
		executorService.executeAdaptationAction(adaptationPlan);

		// then
		verify(managingAgent).addBehaviour(any(InitiateAdaptationActionRequest.class));
		verify(database).setAdaptationActionAvailability(1, false);
		verify(monitoringService).getGoalService(MAXIMIZE_JOB_SUCCESS_RATIO);
	}

	@Test
	@Disabled
	void shouldCorrectlyExecuteSystemAdaptationPlan() {
		// given
		final AID testAID = new AID("test@address", ISGUID);
		testAID.addAddresses("test_address");
		initializeData();
		when(mobilityService.getContainerLocations("CNA1")).thenReturn(
				new AbstractMap.SimpleEntry<>(location, testAID));
		when(location.getName()).thenReturn("Main-Container");
		doNothing().when(mobilityService).moveContainers(any(), any());
		yellowPagesService.when(() -> search(any(), any(), eq(CNA_SERVICE_TYPE)))
				.thenReturn(Set.of(new AID("CNA1", true)));
		adaptationPlan = new AddServerPlan(managingAgent, MAXIMIZE_JOB_SUCCESS_RATIO);
		adaptationPlan.isPlanExecutable();
		adaptationPlan.constructAdaptationPlan();

		// when
		executorService.executeAdaptationAction(adaptationPlan);

		// then
		verify(agentFactory, times(3)).createAgentController(any(), any(ScenarioStructureArgs.class));
		verify(abstractAgentNode).logNewAdaptation(eq(ADD_SERVER), any(Instant.class), eq(Optional.empty()));
		verify(managingAgent).addBehaviour(any(VerifyAdaptationActionResult.class));
	}

	private void initializeData() {
		var serverAgentArgs = ImmutableServerArgs.builder()
				.jobProcessingLimit(200)
				.name("Server1")
				.ownerCloudNetwork("CNA1")
				.price(5.0)
				.build();
		var greenCloudStructure = new ScenarioStructureArgs(null, null, emptyList(),
				List.of(serverAgentArgs), emptyList(), emptyList());
		var monitoringData = ImmutableClientMonitoringData.builder()
				.currentJobStatus(FINISHED)
				.jobStatusDurationMap(Map.of(ON_BACK_UP, 10L, IN_PROGRESS, 20L))
				.isFinished(true)
				.build();
		var cnaHealthData = new HealthCheck(true, AgentType.CLOUD_NETWORK);
		var cnaTrafficData = ImmutableCloudNetworkMonitoringData.builder()
				.successRatio(0.8)
				.build();

		when(managingAgent.getGreenCloudStructure()).thenReturn(greenCloudStructure);

		database.writeMonitoringData("test", CLIENT_MONITORING, monitoringData);
		database.writeMonitoringData("testCNA", HEALTH_CHECK, cnaHealthData);
		database.writeMonitoringData("testCNA", CLOUD_NETWORK_MONITORING, cnaTrafficData);
	}
}
