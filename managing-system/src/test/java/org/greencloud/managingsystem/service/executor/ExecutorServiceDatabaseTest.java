package org.greencloud.managingsystem.service.executor;

import static com.database.knowledge.domain.action.AdaptationActionEnum.ADD_SERVER;
import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationAction;
import static com.database.knowledge.domain.agent.DataType.CLIENT_MONITORING;
import static com.database.knowledge.domain.goal.GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO;
import static com.greencloud.application.yellowpages.domain.DFServiceConstants.CNA_SERVICE_TYPE;
import static com.greencloud.commons.job.ClientJobStatusEnum.CREATED;
import static com.greencloud.commons.job.ClientJobStatusEnum.FINISHED;
import static com.greencloud.commons.job.ClientJobStatusEnum.IN_PROGRESS;
import static com.greencloud.commons.job.ClientJobStatusEnum.PROCESSED;
import static jade.core.AID.ISGUID;
import static java.util.Collections.emptyList;
import static org.greencloud.managingsystem.service.common.TestAdaptationPlanFactory.getTestAdaptationPlan;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.agent.behaviour.executor.InitiateAdaptationActionRequest;
import org.greencloud.managingsystem.agent.behaviour.executor.VerifyAdaptationActionResult;
import org.greencloud.managingsystem.service.common.TestPlanParameters;
import org.greencloud.managingsystem.service.executor.jade.AgentRunner;
import org.greencloud.managingsystem.service.monitoring.MonitoringService;
import org.greencloud.managingsystem.service.planner.plans.AbstractPlan;
import org.greencloud.managingsystem.service.planner.plans.AddServerPlan;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.database.knowledge.domain.agent.client.ImmutableClientMonitoringData;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.greencloud.application.yellowpages.YellowPagesService;
import com.greencloud.commons.args.agent.server.ImmutableServerAgentArgs;
import com.greencloud.commons.args.agent.server.ServerAgentArgs;
import com.greencloud.commons.scenario.ScenarioStructureArgs;
import com.gui.agents.ManagingAgentNode;

import jade.core.AID;
import jade.core.Location;

@ExtendWith(MockitoExtension.class)
class ExecutorServiceDatabaseTest {

	private static final Integer TEST_VALUE = 1;
	private static final AID TEST_AID = new AID("test", ISGUID);

	@Mock
	ManagingAgent managingAgent;
	@Mock
	ManagingAgentNode abstractAgentNode;
	@Mock
	Location location;

	AgentRunner agentRunner;
	AbstractPlan adaptationPlan;
	TimescaleDatabase database;
	MonitoringService monitoringService;
	ExecutorService executorService;
	MockedStatic<YellowPagesService> yellowPagesService;

	@BeforeEach
	void init() {
		database = spy(new TimescaleDatabase());
		database.initDatabase();

		agentRunner = mock(AgentRunner.class);
		monitoringService = spy(new MonitoringService(managingAgent));
		executorService = spy(new ExecutorService(managingAgent, agentRunner));
		yellowPagesService = mockStatic(YellowPagesService.class);

		when(managingAgent.monitor()).thenReturn(monitoringService);
		when(managingAgent.getAgentNode()).thenReturn(abstractAgentNode);
		when(abstractAgentNode.getDatabaseClient()).thenReturn(database);

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
		var monitoringData = ImmutableClientMonitoringData.builder()
				.currentJobStatus(FINISHED)
				.isFinished(true)
				.jobStatusDurationMap(Map.of(CREATED, 10L, PROCESSED, 10L, IN_PROGRESS, 25L))
				.build();
		database.writeMonitoringData("test", CLIENT_MONITORING, monitoringData);

		// when
		executorService.executeAdaptationAction(adaptationPlan);

		// then
		verify(managingAgent).addBehaviour(any(InitiateAdaptationActionRequest.class));
		verify(database).setAdaptationActionAvailability(1, false);
		verify(monitoringService).getGoalService(MAXIMIZE_JOB_SUCCESS_RATIO);
	}

	@Test
	void shouldThrowExceptionWhereNoGoalQualityData() {
		// when & then
		assertThrows(IllegalStateException.class, () -> executorService.executeAdaptationAction(adaptationPlan));
	}

	@Test
	void shouldCorrectlyExecuteSystemAdaptationPlan() {
		// given
		ServerAgentArgs serverAgentArgs = ImmutableServerAgentArgs.builder()
				.jobProcessingLimit("200")
				.name("Server1")
				.latitude("latitude")
				.longitude("longitude")
				.maximumCapacity("200")
				.ownerCloudNetwork("CNA1")
				.price("5.0")
				.build();
		ScenarioStructureArgs greenCloudStructure = new ScenarioStructureArgs(null, null, emptyList(),
				List.of(serverAgentArgs), emptyList(), emptyList());
		when(managingAgent.getGreenCloudStructure()).thenReturn(greenCloudStructure);
		when(managingAgent.getContainerLocations("CNA1")).thenReturn(location);
		when(location.getName()).thenReturn("Main-Container");
		yellowPagesService.when(() -> YellowPagesService.search(any(), eq(CNA_SERVICE_TYPE)))
				.thenReturn(Set.of(new AID("CNA1", true)));
		var monitoringData = ImmutableClientMonitoringData.builder()
				.currentJobStatus(FINISHED)
				.isFinished(true)
				.build();
		database.writeMonitoringData("test", CLIENT_MONITORING, monitoringData);
		adaptationPlan = new AddServerPlan(managingAgent);
		adaptationPlan.isPlanExecutable();
		adaptationPlan.constructAdaptationPlan();

		// when
		executorService.executeAdaptationAction(adaptationPlan);

		// then
		verify(agentRunner).runAgents(anyList());
		verify(abstractAgentNode).logNewAdaptation(eq(getAdaptationAction(ADD_SERVER)), any(Instant.class),
				eq(Optional.empty()));
		verify(managingAgent).addBehaviour(any(VerifyAdaptationActionResult.class));
	}
}
