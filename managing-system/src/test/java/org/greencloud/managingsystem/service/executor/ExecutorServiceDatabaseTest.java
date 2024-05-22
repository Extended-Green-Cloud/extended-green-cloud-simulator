package org.greencloud.managingsystem.service.executor;

import static com.database.knowledge.types.DataType.CLIENT_MONITORING;
import static com.database.knowledge.types.DataType.REGIONAL_MANAGER_MONITORING;
import static com.database.knowledge.types.DataType.HEALTH_CHECK;
import static com.database.knowledge.types.GoalType.MAXIMIZE_JOB_SUCCESS_RATIO;
import static jade.core.AID.ISGUID;
import static java.util.Collections.emptyList;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.FINISHED;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.IN_PROGRESS;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.ON_BACK_UP;
import static org.greencloud.managingsystem.service.common.TestAdaptationPlanFactory.getTestAdaptationPlan;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.greencloud.commons.args.agent.EGCSAgentType;
import org.greencloud.commons.args.agent.server.factory.ImmutableServerArgs;
import org.greencloud.commons.args.scenario.ScenarioStructureArgs;
import org.jrba.utils.yellowpages.YellowPagesRegister;
import org.greencloud.gui.agents.managing.ManagingAgentNode;
import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.agent.behaviour.executor.InitiateAdaptationActionRequest;
import org.greencloud.managingsystem.service.common.TestPlanParameters;
import org.greencloud.managingsystem.service.mobility.MobilityService;
import org.greencloud.managingsystem.service.monitoring.MonitoringService;
import org.greencloud.managingsystem.service.planner.plans.AbstractPlan;
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
import com.database.knowledge.domain.agent.regionalmanager.ImmutableRegionalManagerMonitoringData;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.greencloud.connector.factory.EGCSControllerFactory;

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
	EGCSControllerFactory agentFactory;
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

		agentFactory = mock(EGCSControllerFactory.class);
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

	private void initializeData() {
		var serverAgentArgs = ImmutableServerArgs.builder()
				.jobProcessingLimit(200)
				.name("Server1")
				.ownerRegionalManager("RMA1")
				.price(5.0)
				.build();
		var greenCloudStructure = new ScenarioStructureArgs(null, null, emptyList(),
				List.of(serverAgentArgs), emptyList(), emptyList());
		var monitoringData = ImmutableClientMonitoringData.builder()
				.currentJobStatus(FINISHED)
				.jobStatusDurationMap(Map.of(ON_BACK_UP, 10L, IN_PROGRESS, 20L))
				.isFinished(true)
				.build();
		var rmaHealthData = new HealthCheck(true, EGCSAgentType.REGIONAL_MANAGER);
		var rmaTrafficData = ImmutableRegionalManagerMonitoringData.builder()
				.successRatio(0.8)
				.build();

		when(managingAgent.getGreenCloudStructure()).thenReturn(greenCloudStructure);

		database.writeMonitoringData("test", CLIENT_MONITORING, monitoringData);
		database.writeMonitoringData("testRMA", HEALTH_CHECK, rmaHealthData);
		database.writeMonitoringData("testRMA", REGIONAL_MANAGER_MONITORING, rmaTrafficData);
	}
}
