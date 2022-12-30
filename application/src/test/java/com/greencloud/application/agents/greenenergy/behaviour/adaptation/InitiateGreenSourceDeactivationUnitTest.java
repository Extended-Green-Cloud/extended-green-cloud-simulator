package com.greencloud.application.agents.greenenergy.behaviour.adaptation;

import static jade.lang.acl.ACLMessage.FAILURE;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REFUSE;
import static jade.lang.acl.ACLMessage.REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.quality.Strictness.LENIENT;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.greenenergy.domain.GreenSourceDisconnection;
import com.greencloud.application.agents.greenenergy.management.GreenEnergyAdaptationManagement;
import com.greencloud.commons.job.ExecutionJobStatusEnum;
import com.greencloud.commons.job.ImmutableServerJob;
import com.greencloud.commons.job.ServerJob;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class InitiateGreenSourceDeactivationUnitTest {

	@Mock
	private GreenEnergyAgent greenEnergyAgent;
	@Mock
	private GreenEnergyAdaptationManagement greenEnergyAdaptationManagement;
	@Mock
	private ACLMessage mockAdaptationRequest;

	private InitiateGreenSourceDeactivation initiateGreenSourceDeactivation;

	@BeforeEach
	void init() {
		mockAdaptationRequest = spy(new ACLMessage(REQUEST));
		greenEnergyAgent = spy(GreenEnergyAgent.class);
		greenEnergyAdaptationManagement = spy(new GreenEnergyAdaptationManagement(greenEnergyAgent));
		greenEnergyAgent.setAdaptationManagement(greenEnergyAdaptationManagement);

		var testDisconnection = new GreenSourceDisconnection(null, mockAdaptationRequest, true);
		greenEnergyAdaptationManagement.setGreenSourceDisconnection(testDisconnection);

		doReturn(prepareGreenEnergyJobs()).when(greenEnergyAgent).getServerJobs();
	}

	@Test
	@DisplayName("Test handle refuse message")
	void testRefuse() {
		initiateGreenSourceDeactivation = InitiateGreenSourceDeactivation.create(greenEnergyAgent, "test_server1");

		var refuse = new ACLMessage(REFUSE);
		refuse.setSender(new AID("test_server1", AID.ISGUID));

		clearInvocations(greenEnergyAgent);
		clearInvocations(greenEnergyAdaptationManagement);

		initiateGreenSourceDeactivation.handleRefuse(refuse);

		verify(greenEnergyAdaptationManagement).getGreenSourceDisconnectionState();
		verify(greenEnergyAgent).send(argThat(message -> message.getPerformative() == FAILURE));

		assertThat(greenEnergyAdaptationManagement.getGreenSourceDisconnectionState().isBeingDisconnected()).isFalse();
	}

	@Test
	@DisplayName("Test handle inform message for no jobs left")
	void testInformForNoJobs() {
		initiateGreenSourceDeactivation = InitiateGreenSourceDeactivation.create(greenEnergyAgent, "test_server1");

		var inform = new ACLMessage(INFORM);
		inform.setSender(new AID("test_server3", AID.ISGUID));

		clearInvocations(greenEnergyAgent);
		clearInvocations(greenEnergyAdaptationManagement);

		initiateGreenSourceDeactivation.handleInform(inform);

		verify(greenEnergyAgent).getServerJobs();
		verify(greenEnergyAdaptationManagement, times(1)).getGreenSourceDisconnectionState();
		verify(greenEnergyAgent).addBehaviour(
				argThat(behaviour -> behaviour instanceof InitiateGreenSourceDisconnection));

		assertThat(greenEnergyAdaptationManagement.getGreenSourceDisconnectionState().isBeingDisconnected()).isTrue();
		assertThat(greenEnergyAdaptationManagement.getGreenSourceDisconnectionState()
				.getServerToBeDisconnected()).isNull();
	}

	@Test
	@DisplayName("Test handle inform message for leftover left")
	void testInformForLeftoverJobs() {
		initiateGreenSourceDeactivation = InitiateGreenSourceDeactivation.create(greenEnergyAgent, "test_server1");

		var inform = new ACLMessage(INFORM);
		inform.setSender(new AID("test_server1", AID.ISGUID));

		clearInvocations(greenEnergyAgent);
		clearInvocations(greenEnergyAdaptationManagement);

		initiateGreenSourceDeactivation.handleInform(inform);

		verify(greenEnergyAgent).getServerJobs();
		verify(greenEnergyAdaptationManagement, times(1)).getGreenSourceDisconnectionState();
		verify(greenEnergyAgent, times(0)).addBehaviour(any());

		assertThat(greenEnergyAdaptationManagement.getGreenSourceDisconnectionState().isBeingDisconnected()).isTrue();
		assertThat(greenEnergyAdaptationManagement.getGreenSourceDisconnectionState()
				.getServerToBeDisconnected().getName()).isEqualTo("test_server1");
	}

	private Map<ServerJob, ExecutionJobStatusEnum> prepareGreenEnergyJobs() {
		final ServerJob mockJob1 = ImmutableServerJob.builder().jobId("1")
				.server(new AID("test_server1", AID.ISGUID))
				.startTime(Instant.parse("2022-01-01T08:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T10:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(10).build();
		final ServerJob mockJob2 = ImmutableServerJob.builder().jobId("2")
				.server(new AID("test_server1", AID.ISGUID))
				.startTime(Instant.parse("2022-01-01T07:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(20).build();
		final ServerJob mockJob3 = ImmutableServerJob.builder().jobId("3")
				.server(new AID("test_server2", AID.ISGUID))
				.startTime(Instant.parse("2022-01-01T06:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T15:00:00.000Z"))
				.deadline(Instant.parse("2022-01-01T20:00:00.000Z"))
				.power(50).build();

		final Map<ServerJob, ExecutionJobStatusEnum> mockJobMap = new HashMap<>();
		mockJobMap.put(mockJob1, ExecutionJobStatusEnum.ACCEPTED);
		mockJobMap.put(mockJob2, ExecutionJobStatusEnum.IN_PROGRESS);
		mockJobMap.put(mockJob3, ExecutionJobStatusEnum.ON_HOLD_PLANNED);
		return mockJobMap;
	}
}
