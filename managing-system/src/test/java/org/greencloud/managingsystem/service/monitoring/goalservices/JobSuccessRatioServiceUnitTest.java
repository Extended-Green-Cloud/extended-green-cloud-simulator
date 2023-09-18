package org.greencloud.managingsystem.service.monitoring.goalservices;

import static com.database.knowledge.domain.agent.DataType.CLIENT_MONITORING;
import static com.database.knowledge.domain.agent.DataType.SERVER_MONITORING;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.CREATED;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.FAILED;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.FINISHED;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.IN_PROGRESS;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.PROCESSED;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_TIME_PERIOD;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.NETWORK_AGENT_DATA_TYPES;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.greencloud.managingsystem.service.monitoring.MonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.client.ClientMonitoringData;
import com.database.knowledge.domain.agent.client.ImmutableClientMonitoringData;
import com.database.knowledge.domain.agent.server.ImmutableServerMonitoringData;
import com.database.knowledge.domain.agent.server.ServerMonitoringData;
import com.database.knowledge.domain.goal.AdaptationGoal;
import com.database.knowledge.domain.goal.GoalEnum;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.gui.agents.managing.ManagingAgentNode;

class JobSuccessRatioServiceUnitTest {

	@Mock
	private ManagingAgent mockManagingAgent;
	@Mock
	private ManagingAgentNode mockAgentNode;
	@Mock
	private TimescaleDatabase mockDatabase;
	@Mock
	private MonitoringService mockMonitoringService;

	private JobSuccessRatioService jobSuccessRatioService;

	private static Stream<Arguments> parametersForSuccessRatioTest() {
		final AdaptationGoal successRatioCorrect =
				new AdaptationGoal(1, "Maximize job success ratio", 0.5, true, 0.7);
		final AdaptationGoal successRatioIncorrect =
				new AdaptationGoal(1, "Maximize job success ratio", 0.9, true, 0.7);

		return Stream.of(
				Arguments.of(successRatioCorrect, true),
				Arguments.of(successRatioIncorrect, false)
		);
	}

	@BeforeEach
	void setUp() {
		mockManagingAgent = mock(ManagingAgent.class);
		mockAgentNode = mock(ManagingAgentNode.class);
		mockDatabase = mock(TimescaleDatabase.class);
		mockMonitoringService = mock(MonitoringService.class);

		jobSuccessRatioService = new JobSuccessRatioService(mockManagingAgent);

		doReturn(mockAgentNode).when(mockManagingAgent).getAgentNode();
		doReturn(mockDatabase).when(mockAgentNode).getDatabaseClient();
		doReturn(mockMonitoringService).when(mockManagingAgent).monitor();
	}

	@Test
	@DisplayName("Test is job success ratio correct for clients when data is not available")
	void testIsClientJobSuccessRatioCorrectNotAvailable() {
		doReturn(Collections.emptyList()).when(mockDatabase).readLastMonitoringDataForDataTypes(singletonList(
				CLIENT_MONITORING), MONITOR_SYSTEM_DATA_TIME_PERIOD);

		assertThat(jobSuccessRatioService.evaluateAndUpdate()).isTrue();
	}

	@ParameterizedTest
	@MethodSource("parametersForSuccessRatioTest")
	@DisplayName("Test is job success ratio correct for clients")
	void testIsClientJobSuccessRatioCorrect(AdaptationGoal goal) {
		doReturn(goal).when(mockMonitoringService).getAdaptationGoal(GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO);
		doReturn(prepareClientData()).when(mockDatabase).readLastMonitoringDataForDataTypes(eq(singletonList(
				CLIENT_MONITORING)), anyDouble());

		assertThat(jobSuccessRatioService.evaluateAndUpdate()).isFalse();
	}

	@Test
	@DisplayName("Test is job success ratio correct for components when ratio is not available")
	void testIsClientJobSuccessRatioCorrectForNotAvailable() {
		doReturn(Collections.emptyList()).when(mockDatabase).readLastMonitoringDataForDataTypes(
				NETWORK_AGENT_DATA_TYPES, MONITOR_SYSTEM_DATA_TIME_PERIOD);

		assertThat(jobSuccessRatioService.evaluateComponentSuccessRatio()).isTrue();
	}

	@ParameterizedTest
	@MethodSource("parametersForSuccessRatioTest")
	@DisplayName("Test is job success ratio correct for components")
	void testIsComponentJobSuccessRatioCorrect(AdaptationGoal goal) {
		doReturn(goal).when(mockMonitoringService).getAdaptationGoal(GoalEnum.MAXIMIZE_JOB_SUCCESS_RATIO);
		doReturn(prepareComponentData()).when(mockDatabase).readLastMonitoringDataForDataTypes(
				eq(NETWORK_AGENT_DATA_TYPES), anyDouble());

		assertThat(jobSuccessRatioService.evaluateComponentSuccessRatio()).isFalse();
	}

	private List<AgentData> prepareClientData() {
		final ClientMonitoringData data1 = ImmutableClientMonitoringData.builder()
				.currentJobStatus(IN_PROGRESS)
				.isFinished(false)
				.jobStatusDurationMap(Map.of(CREATED, 10L, PROCESSED, 15L))
				.build();
		final ClientMonitoringData data2 = ImmutableClientMonitoringData.builder()
				.currentJobStatus(FAILED)
				.isFinished(true)
				.jobStatusDurationMap(Map.of(CREATED, 10L, PROCESSED, 15L))
				.build();
		final ClientMonitoringData data3 = ImmutableClientMonitoringData.builder()
				.currentJobStatus(FINISHED)
				.isFinished(true)
				.jobStatusDurationMap(Map.of(CREATED, 10L, PROCESSED, 15L))
				.build();
		return List.of(
				new AgentData(Instant.now(), "test_aid1", CLIENT_MONITORING, data1),
				new AgentData(Instant.now(), "test_aid2", CLIENT_MONITORING, data2),
				new AgentData(Instant.now(), "test_aid3", CLIENT_MONITORING, data3)
		);
	}

	private List<AgentData> prepareComponentData() {
		final ServerMonitoringData data1 = ImmutableServerMonitoringData.builder()
				.serverJobs(10)
				.currentTraffic(0.7)
				.successRatio(0.9)
				.isDisabled(false)
				.build();
		final ServerMonitoringData data2 = ImmutableServerMonitoringData.builder()
				.serverJobs(10)
				.currentTraffic(0.7)
				.successRatio(0.75)
				.isDisabled(false)
				.build();
		final ServerMonitoringData data3 = ImmutableServerMonitoringData.builder()
				.serverJobs(10)
				.currentTraffic(0.7)
				.successRatio(0D)
				.isDisabled(false)
				.build();
		return List.of(
				new AgentData(Instant.now(), "test_aid1", SERVER_MONITORING, data1),
				new AgentData(Instant.now(), "test_aid2", SERVER_MONITORING, data2),
				new AgentData(Instant.now(), "test_aid3", SERVER_MONITORING, data3)

		);
	}
}
