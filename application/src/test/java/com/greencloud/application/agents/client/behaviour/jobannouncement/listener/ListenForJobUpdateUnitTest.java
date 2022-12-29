package com.greencloud.application.agents.client.behaviour.jobannouncement.listener;

import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.templates.JobAnnouncementMessageTemplates.CLIENT_JOB_UPDATE_TEMPLATE;
import static com.greencloud.application.mapper.JsonMapper.getMapper;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.BACK_UP_POWER_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.DELAYED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.FAILED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.GREEN_POWER_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.ON_HOLD_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.POSTPONED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.PROCESSING_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.RE_SCHEDULED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.SCHEDULED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.STARTED_JOB_ID;
import static com.greencloud.application.utils.TimeUtils.postponeTime;
import static com.greencloud.commons.job.ClientJobStatusEnum.DELAYED;
import static com.greencloud.commons.job.ClientJobStatusEnum.FAILED;
import static com.greencloud.commons.job.ClientJobStatusEnum.IN_PROGRESS;
import static com.greencloud.commons.job.ClientJobStatusEnum.ON_BACK_UP;
import static com.greencloud.commons.job.ClientJobStatusEnum.ON_HOLD;
import static com.greencloud.commons.job.ClientJobStatusEnum.PROCESSED;
import static com.greencloud.commons.job.ClientJobStatusEnum.SCHEDULED;
import static java.time.Instant.parse;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greencloud.application.agents.client.ClientAgent;
import com.greencloud.application.agents.client.domain.JobPart;
import com.greencloud.application.agents.client.management.ClientStateManagement;
import com.greencloud.application.domain.job.ImmutableJobInstanceIdentifier;
import com.greencloud.application.domain.job.ImmutableJobTimeFrames;
import com.greencloud.application.domain.job.JobStatusUpdate;
import com.greencloud.commons.job.ClientJobStatusEnum;
import com.gui.agents.ClientAgentNode;
import com.gui.controller.GuiController;

import jade.lang.acl.ACLMessage;

@ExtendWith(MockitoExtension.class)
class ListenForJobUpdateUnitTest {

	private static final String JOB_ID = "1";
	private static final String JOB_PART_ID = "1#part1";
	private static final Instant START_TIME = parse("2022-01-01T13:30:00.000Z");
	private static final Instant END_TIME = parse("2022-01-01T14:00:00.000Z");

	@Mock
	ClientAgent clientAgent;
	@Mock
	ClientStateManagement clientStateManagement;
	@Mock
	ClientAgentNode clientAgentNode;
	@Mock
	JobPart jobPart;
	@Mock
	GuiController guiController;

	@InjectMocks
	ListenForJobUpdate listenForJobUpdate;

	Map<String, JobPart> jobParts;

	static private Stream<Arguments> jobStatusProvider() {
		return Stream.of(
				arguments(SCHEDULED_JOB_ID, SCHEDULED, SCHEDULED),
				arguments(PROCESSING_JOB_ID, PROCESSED, SCHEDULED),
				arguments(DELAYED_JOB_ID, DELAYED, PROCESSED),
				arguments(BACK_UP_POWER_JOB_ID, ON_BACK_UP, IN_PROGRESS),
				arguments(BACK_UP_POWER_JOB_ID, ON_BACK_UP, PROCESSED),
				arguments(GREEN_POWER_JOB_ID, IN_PROGRESS, PROCESSED),
				arguments(ON_HOLD_JOB_ID, ON_HOLD, IN_PROGRESS)
		);
	}

	private static ACLMessage messageBuilder(String conversationId, String jobId) throws JsonProcessingException {
		JobStatusUpdate messageContent = new JobStatusUpdate(ImmutableJobInstanceIdentifier.builder()
				.jobId(jobId)
				.startTime(parse("2022-01-01T13:30:00.000Z"))
				.build(), parse("2022-01-01T13:30:00.000Z"));

		ACLMessage message = new ACLMessage();
		message.setConversationId(conversationId);
		message.setContent(getMapper().writeValueAsString(messageContent));
		return message;
	}

	@BeforeEach
	void init() {
		jobParts = new HashMap<>();
		jobParts.put(JOB_PART_ID, jobPart);
		when(clientAgent.getAgentNode()).thenReturn(clientAgentNode);
		lenient().when(clientAgent.manage()).thenReturn(clientStateManagement);
	}

	@ParameterizedTest
	@MethodSource("jobStatusProvider")
	void shouldCorrectlyProcessesJobStatus(String conversationId, ClientJobStatusEnum status)
			throws JsonProcessingException {
		// given
		var message = messageBuilder(conversationId, JOB_ID);
		when(clientAgent.receive(CLIENT_JOB_UPDATE_TEMPLATE)).thenReturn(message);

		// when
		listenForJobUpdate.action();

		// then
		verify(clientAgentNode).updateJobStatus(status);
	}

	@ParameterizedTest
	@MethodSource("jobStatusProvider")
	void shouldCorrectlyProcessPartJobStatus(String conversationId, ClientJobStatusEnum status,
			ClientJobStatusEnum currentStatus) throws JsonProcessingException {
		// given
		var message = messageBuilder(conversationId, JOB_PART_ID);
		when(clientAgent.receive(CLIENT_JOB_UPDATE_TEMPLATE)).thenReturn(message);
		when(clientAgent.getJobParts()).thenReturn(jobParts);
		when(clientAgent.isSplit()).thenReturn(true);
		lenient().when(clientAgent.manage().getCurrentJobStatus()).thenReturn(currentStatus);

		// when
		listenForJobUpdate.action();

		// then
		verify(jobPart).updateJobStatusDuration(status, parse("2022-01-01T13:30:00.000Z"));
		verify(clientAgentNode).updateJobStatus(status, JOB_PART_ID);
	}

	@Test
	void shouldCorrectlyProcessStartedJob() throws JsonProcessingException {
		// given
		var message = messageBuilder(STARTED_JOB_ID, JOB_ID);
		when(clientAgent.receive(CLIENT_JOB_UPDATE_TEMPLATE)).thenReturn(message);
		when(clientAgent.getSimulatedJobStart()).thenReturn(START_TIME);

		// when
		listenForJobUpdate.action();

		// then
		verify(clientAgentNode).updateJobStatus(IN_PROGRESS);
		verify(clientStateManagement).updateJobStatusDuration(eq(IN_PROGRESS), any(Instant.class));
		verify(clientStateManagement).writeClientData(false);
	}

	@Test
	void shouldCorrectlyProcessStartedJobPart() throws JsonProcessingException {
		// given
		var message = messageBuilder(STARTED_JOB_ID, JOB_PART_ID);
		when(clientAgent.receive(CLIENT_JOB_UPDATE_TEMPLATE)).thenReturn(message);
		when(clientAgent.isSplit()).thenReturn(true);
		when(clientAgent.getJobParts()).thenReturn(jobParts);
		when(jobPart.getSimulatedJobStart()).thenReturn(START_TIME);

		// when
		listenForJobUpdate.action();

		// then
		verify(jobPart).updateJobStatusDuration(IN_PROGRESS, START_TIME);
		verify(clientAgentNode).updateJobStatus(IN_PROGRESS, JOB_PART_ID);
		verify(clientStateManagement).updateOriginalJobStatus(IN_PROGRESS);
		verify(clientStateManagement).writeClientData(false);
	}

	@Test
	void shouldCorrectlyProcessFailedJob() throws JsonProcessingException {
		// given
		var message = messageBuilder(FAILED_JOB_ID, JOB_ID);
		when(clientAgent.receive(CLIENT_JOB_UPDATE_TEMPLATE)).thenReturn(message);
		when(clientAgent.getGuiController()).thenReturn(guiController);

		// when
		listenForJobUpdate.action();

		// then
		verify(guiController).updateClientsCountByValue(-1);
		verify(guiController).updateFailedJobsCountByValue(1);
		verify(clientAgentNode).updateJobStatus(FAILED);
		verify(clientStateManagement).updateJobStatusDuration(eq(FAILED), any(Instant.class));
		verify(clientStateManagement).writeClientData(true);
		verify(clientAgent).doDelete();
	}

	@Test
	void shouldCorrectlyProcessPostponedJob() {
		// given
		var message = mock(ACLMessage.class);
		when(message.getConversationId()).thenReturn(POSTPONED_JOB_ID);
		when(message.getContent()).thenReturn(JOB_ID);
		when(clientAgent.receive(CLIENT_JOB_UPDATE_TEMPLATE)).thenReturn(message);
		when(clientAgent.getSimulatedJobStart()).thenReturn(START_TIME);
		when(clientAgent.getSimulatedJobEnd()).thenReturn(END_TIME);

		// when
		listenForJobUpdate.action();

		// then
		verify(clientAgent).setSimulatedJobStart(postponeTime(START_TIME, 10));
		verify(clientAgent).setSimulatedJobEnd(postponeTime(END_TIME, 10));
		verify(clientAgentNode).updateJobTimeFrame(any(Instant.class), any(Instant.class));
	}

	@Test
	void shouldCorrectlyProcessPostponedJobPart() {
		// given
		var message = mock(ACLMessage.class);
		when(message.getConversationId()).thenReturn(POSTPONED_JOB_ID);
		when(message.getContent()).thenReturn(JOB_PART_ID);
		when(clientAgent.isSplit()).thenReturn(true);
		when(clientAgent.receive(CLIENT_JOB_UPDATE_TEMPLATE)).thenReturn(message);
		when(clientAgent.getJobParts()).thenReturn(jobParts);
		when(clientAgent.getSimulatedJobStart()).thenReturn(START_TIME);
		when(clientAgent.getSimulatedJobEnd()).thenReturn(END_TIME);

		// when
		when(jobPart.getSimulatedJobStart()).thenReturn(postponeTime(START_TIME, 10));
		when(jobPart.getSimulatedJobEnd()).thenReturn(postponeTime(END_TIME, 10));
		listenForJobUpdate.action();

		// then
		verify(jobPart).setSimulatedJobStart(postponeTime(START_TIME, 10));
		verify(jobPart).setSimulatedJobEnd(postponeTime(END_TIME, 10));
		verify(clientAgentNode).updateJobTimeFrame(any(Instant.class), any(Instant.class), eq(JOB_PART_ID));
	}

	@Test
	void shouldCorrectlyProcessRescheduledJob() throws JsonProcessingException {
		// given
		var message = mock(ACLMessage.class);
		var timeframes = ImmutableJobTimeFrames.builder()
				.jobId(JOB_ID)
				.newJobStart(START_TIME.plus(10, MINUTES))
				.newJobEnd(END_TIME.plus(10, MINUTES))
				.build();
		when(message.getConversationId()).thenReturn(RE_SCHEDULED_JOB_ID);
		when(message.getContent()).thenReturn(getMapper().writeValueAsString(timeframes));
		when(clientAgent.receive(CLIENT_JOB_UPDATE_TEMPLATE)).thenReturn(message);
		when(clientAgent.getSimulatedJobStart()).thenReturn(START_TIME);
		when(clientAgent.getSimulatedJobEnd()).thenReturn(END_TIME);

		// when
		listenForJobUpdate.action();

		// then
		verify(clientAgent).setSimulatedJobStart(timeframes.getNewJobStart());
		verify(clientAgent).setSimulatedJobEnd(timeframes.getNewJobEnd());
		verify(clientAgentNode).updateJobTimeFrame(any(Instant.class), any(Instant.class));
	}

	@Test
	void shouldCorrectlyProcessRescheduledJobPart() throws JsonProcessingException {
		// given
		var message = mock(ACLMessage.class);
		var timeframes = ImmutableJobTimeFrames.builder()
				.jobId(JOB_PART_ID)
				.newJobStart(START_TIME.plus(10, MINUTES))
				.newJobEnd(END_TIME.plus(10, MINUTES))
				.build();
		when(message.getConversationId()).thenReturn(RE_SCHEDULED_JOB_ID);
		when(message.getContent()).thenReturn(getMapper().writeValueAsString(timeframes));
		when(clientAgent.isSplit()).thenReturn(true);
		when(clientAgent.receive(CLIENT_JOB_UPDATE_TEMPLATE)).thenReturn(message);
		when(clientAgent.getJobParts()).thenReturn(jobParts);

		// when
		when(jobPart.getSimulatedJobStart()).thenReturn(timeframes.getNewJobStart());
		when(jobPart.getSimulatedJobEnd()).thenReturn(timeframes.getNewJobEnd());
		listenForJobUpdate.action();

		// then
		verify(jobPart).setSimulatedJobStart(timeframes.getNewJobStart());
		verify(jobPart).setSimulatedJobEnd(timeframes.getNewJobEnd());
		verify(clientAgentNode).updateJobTimeFrame(any(Instant.class), any(Instant.class), eq(JOB_PART_ID));
	}
}
