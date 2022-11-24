package com.greencloud.application.agents.client.management;

import static com.greencloud.commons.job.JobStatusEnum.CREATED;
import static com.greencloud.commons.job.JobStatusEnum.FINISHED;
import static com.greencloud.commons.job.JobStatusEnum.IN_PROGRESS;
import static com.greencloud.commons.job.JobStatusEnum.PROCESSED;
import static com.greencloud.commons.job.JobStatusEnum.SCHEDULED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.quality.Strictness.LENIENT;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import com.database.knowledge.domain.agent.AgentData;
import com.database.knowledge.domain.agent.client.ClientMonitoringData;
import com.database.knowledge.timescale.TimescaleDatabase;
import com.greencloud.application.agents.client.ClientAgent;
import com.greencloud.application.agents.client.domain.JobPart;
import com.gui.agents.ClientAgentNode;

import jade.core.AID;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class ClientStateManagementDatabaseTest {

	@Mock
	private static ClientAgent mockClient;
	@Mock
	private static AID mockAID;
	@Mock
	private static ClientAgentNode mockNode;
	@Mock
	private static ClientStateManagement mockClientManagement;
	private TimescaleDatabase database;

	@BeforeEach
	void init() {
		database = new TimescaleDatabase();
		database.initDatabase();

		mockClient = spy(ClientAgent.class);
		mockNode = spy(ClientAgentNode.class);
		mockClientManagement = spy(new ClientStateManagement(mockClient));
		mockClient.setAgentNode(mockNode);

		doReturn(mockAID).when(mockClient).getAID();
		doReturn("ClientMock").when(mockAID).getName();
		doReturn(database).when(mockNode).getDatabaseClient();
	}

	@AfterEach
	void cleanUp() {
		database.close();
	}

	@Test
	@DisplayName("Test writing client data to database - no job split")
	void testWriteClientDataNoSplit() {
		mockClientManagement.setCurrentJobStatus(IN_PROGRESS);
		mockClientManagement.jobStatusDurationMap = Map.of(
				CREATED, 100L,
				PROCESSED, 50L,
				SCHEDULED, 200L
		);
		doReturn(false).when(mockClient).isSplit();

		mockClientManagement.writeClientData(false);
		List<AgentData> result = database.readMonitoringData();

		assertThat(result).hasSize(1);
		assertThat(result.get(0))
				.matches(data -> data.aid().contains("ClientMock"))
				.matches(data -> data.monitoringData() instanceof ClientMonitoringData)
				.matches(data -> {
					final ClientMonitoringData clientData = (ClientMonitoringData) data.monitoringData();
					return !clientData.getIsFinished() &&
							clientData.getCurrentJobStatus().equals(IN_PROGRESS) &&
							clientData.getJobStatusDurationMap().get(CREATED).equals(100L);
				});
	}

	@Test
	@DisplayName("Test writing client data to database - with split")
	void testWriteClientDataWithSplit() {
		mockClientManagement.setCurrentJobStatus(FINISHED);
		doReturn(true).when(mockClient).isSplit();
		prepareJobParts();

		mockClientManagement.writeClientData(true);
		List<AgentData> result = database.readMonitoringData();

		assertThat(result).hasSize(1);
		assertThat(result.get(0))
				.matches(data -> data.aid().contains("ClientMock"))
				.matches(data -> data.monitoringData() instanceof ClientMonitoringData)
				.matches(data -> {
					final ClientMonitoringData clientData = (ClientMonitoringData) data.monitoringData();
					return clientData.getIsFinished() &&
							clientData.getCurrentJobStatus().equals(FINISHED) &&
							clientData.getJobStatusDurationMap().get(CREATED).equals(300L) &&
							clientData.getJobStatusDurationMap().get(PROCESSED).equals(200L);
				});
	}

	private void prepareJobParts() {
		final JobPart jobPart1 = spy(new JobPart(null,null,null,null,null));
		final JobPart jobPart2 = spy(new JobPart(null,null,null,null,null));

		doReturn(Map.of(CREATED, 100L, PROCESSED, 50L)).when(jobPart1).getJobStatusDurationMap();
		doReturn(Map.of(CREATED, 200L, PROCESSED, 150L)).when(jobPart2).getJobStatusDurationMap();

		doReturn(Map.of("1#1", jobPart1, "1#2", jobPart2)).when(mockClient).getJobParts();
	}

}
