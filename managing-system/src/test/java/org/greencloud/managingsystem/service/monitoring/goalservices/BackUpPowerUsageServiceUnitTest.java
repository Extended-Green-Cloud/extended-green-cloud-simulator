package org.greencloud.managingsystem.service.monitoring.goalservices;

import static com.database.knowledge.domain.agent.DataType.CLIENT_MONITORING;
import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.FINISHED;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.IN_PROGRESS;
import static org.greencloud.commons.enums.job.JobClientStatusEnum.ON_BACK_UP;
import static org.greencloud.managingsystem.domain.ManagingSystemConstants.MONITOR_SYSTEM_DATA_TIME_PERIOD;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.greencloud.gui.agents.managing.ManagingAgentNode;
import org.greencloud.managingsystem.agent.ManagingAgent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.client.ImmutableClientMonitoringData;
import com.database.knowledge.timescale.TimescaleDatabase;

@ExtendWith(MockitoExtension.class)
class BackUpPowerUsageServiceUnitTest {

	@Mock
	ManagingAgent managingAgent;
	@Mock
	ManagingAgentNode managingAgentNode;
	@Mock
	TimescaleDatabase database;
	BackUpPowerUsageService backUpPowerUsageService;

	private static Stream<Arguments> monitoringDataProvider() {
		return Stream.of(
				arguments(of(buildMonitoringData(100, 100)), 0.5),
				arguments(of(buildMonitoringData(0, 100)), 1),
				arguments(of(buildMonitoringData(100, 0)), 0.0),
				arguments(of(buildMonitoringData(100, 0), buildMonitoringData(0, 100)), 0.5),
				arguments(emptyList(), -1),
				arguments(of(buildMonitoringData(0, 0)), -1)
		);
	}

	private static AgentData buildMonitoringData(long inProgress, long backUp) {
		return new AgentData(now(), "test", CLIENT_MONITORING, ImmutableClientMonitoringData.builder()
				.isFinished(true)
				.currentJobStatus(FINISHED)
				.jobStatusDurationMap(Map.of(IN_PROGRESS, inProgress, ON_BACK_UP, backUp))
				.build());
	}

	@BeforeEach
	void init() {
		managingAgent = mock(ManagingAgent.class);
		managingAgentNode = mock(ManagingAgentNode.class);
		database = mock(TimescaleDatabase.class);
		backUpPowerUsageService = new BackUpPowerUsageService(managingAgent);

		doReturn(managingAgentNode).when(managingAgent).getAgentNode();
		doReturn(database).when(managingAgentNode).getDatabaseClient();
	}

	@ParameterizedTest
	@MethodSource("monitoringDataProvider")
	void shouldCorrectlyReadCurrentGoalQuality(List<AgentData> agentData, double expectedQuality) {
		// given
		when(database.readLastMonitoringDataForDataTypes(of(CLIENT_MONITORING), MONITOR_SYSTEM_DATA_TIME_PERIOD))
				.thenReturn(agentData);

		// when
		var result = backUpPowerUsageService.computeCurrentGoalQuality();

		// then
		assertThat(result)
				.isEqualTo(expectedQuality);
	}
}
