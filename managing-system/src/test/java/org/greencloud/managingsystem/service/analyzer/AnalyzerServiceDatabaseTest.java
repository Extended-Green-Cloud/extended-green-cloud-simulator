package org.greencloud.managingsystem.service.analyzer;

import static com.database.knowledge.domain.action.AdaptationActionEnum.ADD_GREEN_SOURCE;
import static com.database.knowledge.domain.action.AdaptationActionEnum.ADD_SERVER;
import static com.database.knowledge.domain.action.AdaptationActionEnum.CHANGE_GREEN_SOURCE_WEIGHT;
import static com.database.knowledge.domain.action.AdaptationActionEnum.CONNECT_GREEN_SOURCE;
import static com.database.knowledge.domain.action.AdaptationActionEnum.DECREASE_GREEN_SOURCE_ERROR;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_DEADLINE_PRIORITY;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_GREEN_SOURCE_ERROR;
import static com.database.knowledge.domain.action.AdaptationActionEnum.INCREASE_POWER_PRIORITY;
import static com.database.knowledge.domain.action.AdaptationActionTypeEnum.ADD_COMPONENT;
import static com.database.knowledge.domain.action.AdaptationActionTypeEnum.RECONFIGURE;
import static com.database.knowledge.domain.agent.DataType.CLIENT_MONITORING;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static com.database.knowledge.domain.goal.GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO;
import static com.database.knowledge.domain.goal.GoalEnum.MINIMIZE_USED_BACKUP_POWER;
import static com.greencloud.commons.job.ClientJobStatusEnum.CREATED;
import static com.greencloud.commons.job.ClientJobStatusEnum.FAILED;
import static com.greencloud.commons.job.ClientJobStatusEnum.FINISHED;
import static com.greencloud.commons.job.ClientJobStatusEnum.IN_PROGRESS;
import static com.greencloud.commons.job.ClientJobStatusEnum.PROCESSED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.service.monitoring.MonitoringService;
import org.greencloud.managingsystem.service.planner.PlannerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.domain.agent.client.ClientMonitoringData;
import com.database.knowledge.domain.agent.client.ImmutableClientMonitoringData;
import com.database.knowledge.domain.agent.server.ImmutableServerMonitoringData;
import com.database.knowledge.domain.agent.server.ServerMonitoringData;
import com.database.knowledge.domain.goal.GoalEnum;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.gui.agents.ManagingAgentNode;

class AnalyzerServiceDatabaseTest {

	@Mock
	private ManagingAgent mockManagingAgent;
	@Mock
	private ManagingAgentNode mockAgentNode;

	private TimescaleDatabase database;

	private AnalyzerService analyzerService;
	private PlannerService plannerService;

	@BeforeEach
	void init() {
		database = spy(new TimescaleDatabase());
		database.initDatabase();

		mockManagingAgent = spy(ManagingAgent.class);
		mockAgentNode = mock(ManagingAgentNode.class);
		analyzerService = new AnalyzerService(mockManagingAgent);
		var monitoringService = new MonitoringService(mockManagingAgent);
		plannerService = spy(new PlannerService(mockManagingAgent));

		doReturn(mockAgentNode).when(mockManagingAgent).getAgentNode();
		doReturn(database).when(mockAgentNode).getDatabaseClient();
		doReturn(monitoringService).when(mockManagingAgent).monitor();
		doReturn(plannerService).when(mockManagingAgent).plan();
		doNothing().when(mockAgentNode).registerManagingAgent(anyList());

		mockManagingAgent.monitor().readSystemAdaptationGoals();
		prepareSystemData();
		monitoringService.isSuccessRatioMaximized();
	}

	@AfterEach
	void cleanUp() {
		database.close();
	}

	@Test
	@DisplayName("Test analyzer triggering")
	void testTrigger() {
		database.readAdaptationActions()
				.forEach(action -> database.setAdaptationActionAvailability(action.getActionId(), false));
		analyzerService.trigger(GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO);

		verify(mockManagingAgent).getSystemQualityThreshold();
		verify(database, times(2)).readAdaptationActions();
		verify(mockManagingAgent).plan();
		verify(plannerService).trigger(argThat(
				adaptationActionDoubleMap -> adaptationActionDoubleMap.values().stream().allMatch(val -> val == 0)));
	}

	@Test
	@DisplayName("Test getting adaptation actions for success ratio goal")
	void testGetAdaptationActionsForSuccessRatioGoal() {
		var expectedResult = List.of(
				new AdaptationAction(1, ADD_SERVER, ADD_COMPONENT, MAXIMIZE_JOB_SUCCESS_RATIO),
				new AdaptationAction(2, INCREASE_DEADLINE_PRIORITY, RECONFIGURE, MAXIMIZE_JOB_SUCCESS_RATIO),
				new AdaptationAction(3, INCREASE_POWER_PRIORITY, RECONFIGURE, MAXIMIZE_JOB_SUCCESS_RATIO),
				new AdaptationAction(4, CHANGE_GREEN_SOURCE_WEIGHT, RECONFIGURE, MAXIMIZE_JOB_SUCCESS_RATIO),
				new AdaptationAction(5, INCREASE_GREEN_SOURCE_ERROR, RECONFIGURE, MAXIMIZE_JOB_SUCCESS_RATIO),
				new AdaptationAction(6, CONNECT_GREEN_SOURCE, ADD_COMPONENT, MAXIMIZE_JOB_SUCCESS_RATIO)
		);

		var result = analyzerService.getAdaptationActionsForGoal(MAXIMIZE_JOB_SUCCESS_RATIO);

		assertThat(result)
				.as("There should be 6 adaptation actions")
				.hasSize(6)
				.as("Data of the adaptation actions should equal to the expected result")
				.usingRecursiveFieldByFieldElementComparator()
				.containsExactlyInAnyOrderElementsOf(expectedResult);
	}

	@Test
	@DisplayName("Test getting adaptation actions for back up power goal")
	void testGetAdaptationActionsForBackUpPowerGoal() {
		var expectedResult = List.of(
				new AdaptationAction(7, DECREASE_GREEN_SOURCE_ERROR,
						RECONFIGURE, MINIMIZE_USED_BACKUP_POWER),
				new AdaptationAction(8, ADD_GREEN_SOURCE,
						ADD_COMPONENT, MINIMIZE_USED_BACKUP_POWER)
		);

		var result = analyzerService.getAdaptationActionsForGoal(MINIMIZE_USED_BACKUP_POWER);

		assertThat(result)
				.as("There should be 1 adaptation action")
				.hasSize(2)
				.as("Data of the adaptation action should equal to the expected result")
				.usingRecursiveFieldByFieldElementComparator()
				.containsExactlyInAnyOrderElementsOf(expectedResult);
	}

	private void prepareSystemData() {
		final ClientMonitoringData data1 = ImmutableClientMonitoringData.builder()
				.currentJobStatus(FAILED)
				.isFinished(true)
				.jobStatusDurationMap(Map.of(FAILED, 10L, CREATED, 40L))
				.build();
		final ClientMonitoringData data2 = ImmutableClientMonitoringData.builder()
				.currentJobStatus(FINISHED)
				.isFinished(true)
				.jobStatusDurationMap(Map.of(CREATED, 10L, PROCESSED, 10L, IN_PROGRESS, 25L))
				.build();
		final ServerMonitoringData data3 = ImmutableServerMonitoringData.builder()
				.currentMaximumCapacity(100)
				.currentTraffic(0.7)
				.successRatio(0.9)
				.currentBackUpPowerUsage(0.7)
				.build();

		database.writeMonitoringData("test_aid1", CLIENT_MONITORING, data1);
		database.writeMonitoringData("test_aid2", CLIENT_MONITORING, data2);
		database.writeMonitoringData("test_aid3", SERVER_MONITORING, data3);
	}
}
