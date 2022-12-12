package org.greencloud.managingsystem.service.monitoring;

import static com.database.knowledge.domain.agent.DataType.CLIENT_MONITORING;
import static com.database.knowledge.domain.agent.DataType.CLOUD_NETWORK_MONITORING;
import static com.greencloud.commons.job.ClientJobStatusEnum.CREATED;
import static com.greencloud.commons.job.ClientJobStatusEnum.FAILED;
import static com.greencloud.commons.job.ClientJobStatusEnum.FINISHED;
import static com.greencloud.commons.job.ClientJobStatusEnum.IN_PROGRESS;
import static com.greencloud.commons.job.ClientJobStatusEnum.PROCESSED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.greencloud.managingsystem.agent.ManagingAgent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.client.ClientMonitoringData;
import com.database.knowledge.domain.agent.client.ImmutableClientMonitoringData;
import com.database.knowledge.domain.agent.cloudnetwork.CloudNetworkMonitoringData;
import com.database.knowledge.domain.agent.cloudnetwork.ImmutableCloudNetworkMonitoringData;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.greencloud.commons.args.agent.cloudnetwork.ImmutableCloudNetworkArgs;
import com.greencloud.commons.scenario.ScenarioStructureArgs;
import com.gui.agents.ManagingAgentNode;

public class TrafficDistributionServiceTest {

	@Mock
	private ManagingAgent mockManagingAgent;

	@Mock
	private ManagingAgentNode mockAgentNode;

	@Mock
	private TimescaleDatabase mockDatabase;

	@Mock
	private MonitoringService mockMonitoringService;

	private TrafficDistributionService trafficDistributionService;

	@BeforeEach
	void setUp() {
		mockManagingAgent = mock(ManagingAgent.class);
		mockAgentNode = mock(ManagingAgentNode.class);
		mockDatabase = mock(TimescaleDatabase.class);
		mockMonitoringService = mock(MonitoringService.class);

		trafficDistributionService = new TrafficDistributionService(mockManagingAgent);

		doReturn(mockAgentNode).when(mockManagingAgent).getAgentNode();
		doReturn(mockDatabase).when(mockAgentNode).getDatabaseClient();
		doReturn(mockMonitoringService).when(mockManagingAgent).monitor();
	}

	@Test
	@DisplayName("Test compute coefficient")
	public void testComputeCoefficient() {
		double coefficient = trafficDistributionService.computeCoefficient(List.of(1.0, 2.0, 3.0, 4.0, 5.0));

		assertThat(coefficient).isEqualTo(0.5270462766947299);
	}

	@Test
	@DisplayName("Test compute goal quality")
	public void testComputeGoalQualityForComponent() {
		double goalQuality = trafficDistributionService.computeGoalQualityForComponent(prepareCNAData());

		assertThat(goalQuality).isEqualTo(0.4107443490112104);
	}

	private List<AgentData> prepareCNAData() {
		final CloudNetworkMonitoringData data1 = ImmutableCloudNetworkMonitoringData.builder()
				.successRatio(0.5)
				.availablePower(50.0)
				.build();
		final CloudNetworkMonitoringData data2 = ImmutableCloudNetworkMonitoringData.builder()
				.successRatio(0.5)
				.availablePower(100.0)
				.build();
		final CloudNetworkMonitoringData data3 = ImmutableCloudNetworkMonitoringData.builder()
				.successRatio(0.5)
				.availablePower(150.0)
				.build();
		final CloudNetworkMonitoringData data4 = ImmutableCloudNetworkMonitoringData.builder()
				.successRatio(0.5)
				.availablePower(200.0)
				.build();
		return List.of(
				new AgentData(Instant.now(), "test_aid1", CLOUD_NETWORK_MONITORING, data1),
				new AgentData(Instant.now(), "test_aid1", CLOUD_NETWORK_MONITORING, data2),
				new AgentData(Instant.now(), "test_aid2", CLOUD_NETWORK_MONITORING, data3),
				new AgentData(Instant.now(), "test_aid2", CLOUD_NETWORK_MONITORING, data4)
		);
	}

	private List<String> prepareStructure() {
		return List.of("test_aid1", "test_aid2", "test_aid3");
	}
}
