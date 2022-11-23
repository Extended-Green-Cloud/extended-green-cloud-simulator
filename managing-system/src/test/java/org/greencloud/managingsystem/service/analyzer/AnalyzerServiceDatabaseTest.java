package org.greencloud.managingsystem.service.analyzer;

import static com.database.knowledge.domain.action.AdaptationActionEnum.ADD_COMPONENT;
import static com.database.knowledge.domain.action.AdaptationActionEnum.RECONFIGURE;
import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationActions;
import static com.database.knowledge.domain.agent.DataType.CLIENT_MONITORING;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static com.database.knowledge.domain.goal.GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO;
import static com.greencloud.commons.job.JobStatusEnum.CREATED;
import static com.greencloud.commons.job.JobStatusEnum.FAILED;
import static com.greencloud.commons.job.JobStatusEnum.FINISHED;
import static com.greencloud.commons.job.JobStatusEnum.IN_PROGRESS;
import static com.greencloud.commons.job.JobStatusEnum.PROCESSED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.service.monitoring.MonitoringService;
import org.greencloud.managingsystem.service.planner.PlannerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.database.knowledge.domain.action.AdaptationAction;
import com.database.knowledge.domain.agent.ClientMonitoringData;
import com.database.knowledge.domain.agent.ImmutableClientMonitoringData;
import com.database.knowledge.domain.agent.ImmutableServerMonitoringData;
import com.database.knowledge.domain.agent.ServerMonitoringData;
import com.database.knowledge.domain.goal.GoalEnum;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.greencloud.commons.job.JobResultType;
import com.gui.agents.ManagingAgentNode;

import jade.core.AID;

class AnalyzerServiceDatabaseTest {

	@Mock
	private ManagingAgent mockManagingAgent;
	@Mock
	private ManagingAgentNode mockAgentNode;

	private TimescaleDatabase database;

	private AnalyzerService analyzerService;

	@BeforeEach
	void init() {
		database = new TimescaleDatabase();
		database.initDatabase();

		mockManagingAgent = spy(ManagingAgent.class);
		mockAgentNode = mock(ManagingAgentNode.class);
		analyzerService = new AnalyzerService(mockManagingAgent);
		var monitoringService = new MonitoringService(mockManagingAgent);
		var plannerService = new PlannerService(mockManagingAgent);

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
	@Disabled
	@DisplayName("Test analyzer triggering")
	void testTrigger() {
		var expectedQualityMap = getAdaptationActions().stream()
				.collect(Collectors.toMap(action -> action, (action) -> 0.0D));

		analyzerService.trigger(GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO);

		verify(mockManagingAgent).getSystemQualityThreshold();
		verify(database).readAdaptationActions();
		verify(mockManagingAgent).plan().trigger(expectedQualityMap);
	}

	@Test
	@DisplayName("Test getting adaptation actions for goal")
	void testGetAdaptationActionsForGoal() {
		var expectedResult = List.of(
				new AdaptationAction(1, "Add Server",
						ADD_COMPONENT, MAXIMIZE_JOB_SUCCESS_RATIO),
				new AdaptationAction(2, "Increase job deadline priority",
						RECONFIGURE, MAXIMIZE_JOB_SUCCESS_RATIO),
				new AdaptationAction(3, "Increase job power priority",
						RECONFIGURE, MAXIMIZE_JOB_SUCCESS_RATIO),
				new AdaptationAction(4, "Increase Green Source selection chance",
						RECONFIGURE, MAXIMIZE_JOB_SUCCESS_RATIO),
				new AdaptationAction(5, "Increase Green Source weather prediction error",
						RECONFIGURE, MAXIMIZE_JOB_SUCCESS_RATIO),
				new AdaptationAction(7, "Add Green Source",
						ADD_COMPONENT, MAXIMIZE_JOB_SUCCESS_RATIO));

		var result = analyzerService.getAdaptationActionsForGoal(MAXIMIZE_JOB_SUCCESS_RATIO);

		assertThat(result)
				.as("There should be 6 adaptation actions")
				.hasSize(6)
				.as("Data of the adaptation actions should equal to the expected result")
				.usingRecursiveFieldByFieldElementComparator()
				.containsExactlyInAnyOrderElementsOf(expectedResult);
	}

	private void prepareSystemData() {
		final AID mockAID1 = mock(AID.class);
		final AID mockAID2 = mock(AID.class);

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
				.currentlyExecutedJobs(10)
				.currentlyProcessedJobs(3)
				.currentMaximumCapacity(100)
				.currentTraffic(0.7)
				.jobProcessingLimit(5)
				.weightsForGreenSources(Map.of(mockAID1, 3, mockAID2, 2))
				.jobResultStatistics(Map.of(JobResultType.FAILED, 2L, JobResultType.ACCEPTED, 10L))
				.serverPricePerHour(20)
				.build();

		database.writeMonitoringData("test_aid1", CLIENT_MONITORING, data1);
		database.writeMonitoringData("test_aid2", CLIENT_MONITORING, data2);
		database.writeMonitoringData("test_aid3", SERVER_MONITORING, data3);
	}
}
