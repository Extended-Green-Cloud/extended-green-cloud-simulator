package com.greencloud.application.agents.scheduler.behaviour.jobscheduling.listener;

import static com.greencloud.application.agents.scheduler.behaviour.job.scheduling.listener.templates.JobSchedulingMessageTemplates.JOB_UPDATE_TEMPLATE;
import static com.greencloud.application.mapper.JsonMapper.getMapper;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.FAILED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.FINISH_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.POSTPONED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.STARTED_JOB_ID;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.IN_PROGRESS;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.PROCESSING;
import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greencloud.application.agents.scheduler.SchedulerAgent;
import com.greencloud.application.agents.scheduler.behaviour.job.scheduling.listener.ListenForJobUpdate;
import com.greencloud.application.agents.scheduler.managment.SchedulerStateManagement;
import com.greencloud.application.domain.job.ImmutableJobInstanceIdentifier;
import com.greencloud.application.domain.job.JobStatusUpdate;
import com.greencloud.commons.job.ClientJob;

import jade.lang.acl.ACLMessage;

@ExtendWith(MockitoExtension.class)
class ListenForJobUpdateUnitTest {

	private static ObjectMapper objectMapper = getMapper();

	@Mock
	SchedulerAgent schedulerAgent;
	@Mock
	SchedulerStateManagement schedulerStateManagement;
	@Mock
	ACLMessage message;
	@Mock
	ClientJob clientJob;
	@InjectMocks
	ListenForJobUpdate listenForJobUpdate;
	@Captor
	ArgumentCaptor<ACLMessage> messageArgumentCaptor;

	@BeforeEach
	void init() throws JsonProcessingException {
		var jobInstance = ImmutableJobInstanceIdentifier.builder()
				.jobId("jobId")
				.startTime(now())
				.build();
		var jobStatusUpdate = new JobStatusUpdate(jobInstance, now());
		when(schedulerAgent.receive(JOB_UPDATE_TEMPLATE)).thenReturn(message);
		when(message.getContent()).thenReturn(objectMapper.writeValueAsString(jobStatusUpdate));
		when(clientJob.getJobId()).thenReturn("jobId");
		when(clientJob.getClientIdentifier()).thenReturn("clientIdentifier");
		when(schedulerAgent.getClientJobs()).thenReturn(
				spy(new ConcurrentHashMap<>(Map.of(clientJob, PROCESSING))));
	}

	@Test
	void shouldHandleCorrectlyStartedJobStatusChange() {
		// given
		when(message.getConversationId()).thenReturn(STARTED_JOB_ID);

		// when
		listenForJobUpdate.action();

		// then
		verify(schedulerAgent.getClientJobs()).replace(clientJob, PROCESSING, IN_PROGRESS);
		verify(schedulerAgent).send(messageArgumentCaptor.capture());
		verifyNoInteractions(schedulerStateManagement);
		var sentMessage = messageArgumentCaptor.getValue();
		assertThat(sentMessage.getConversationId()).isEqualTo(STARTED_JOB_ID);
	}

	@Test
	void shouldHandleCorrectlyFinishJobStatusChange() {
		// given
		when(message.getConversationId()).thenReturn(FINISH_JOB_ID);
		when(schedulerAgent.manage()).thenReturn(schedulerStateManagement);

		// when
		listenForJobUpdate.action();

		// then
		verify(schedulerStateManagement).handleJobCleanUp(clientJob);
		verify(schedulerAgent).send(messageArgumentCaptor.capture());
		var sentMessage = messageArgumentCaptor.getValue();
		assertThat(sentMessage.getConversationId()).isEqualTo(FINISH_JOB_ID);
	}

	@Test
	void shouldCorrectlyPostponeFailedJobStatusChange() {
		// given
		when(message.getConversationId()).thenReturn(FAILED_JOB_ID);
		when(schedulerAgent.manage()).thenReturn(schedulerStateManagement);

		// when
		when(schedulerStateManagement.postponeJobExecution(clientJob)).thenReturn(true);
		listenForJobUpdate.action();

		// then
		verify(schedulerStateManagement).postponeJobExecution(clientJob);
		verify(schedulerAgent).send(messageArgumentCaptor.capture());
		var sentMessage = messageArgumentCaptor.getValue();
		assertThat(sentMessage.getConversationId()).isEqualTo(POSTPONED_JOB_ID);
	}

	@Test
	void shouldCorrectlyCleanupFailedJobStatusChange() {
		// given
		when(message.getConversationId()).thenReturn(FAILED_JOB_ID);
		when(schedulerAgent.manage()).thenReturn(schedulerStateManagement);

		// when
		when(schedulerStateManagement.postponeJobExecution(clientJob)).thenReturn(false);
		listenForJobUpdate.action();

		// then
		verify(schedulerStateManagement).handleFailedJobCleanUp(eq(clientJob), any());
		verify(schedulerAgent).send(messageArgumentCaptor.capture());
		var sentMessage = messageArgumentCaptor.getValue();
		assertThat(sentMessage.getConversationId()).isEqualTo(FAILED_JOB_ID);
	}
}
