package com.greencloud.application.agents.client.behaviour.jobannouncement.listener;

import static com.greencloud.application.agents.client.behaviour.jobannouncement.listener.templates.JobAnnouncementMessageTemplates.CLIENT_JOB_UPDATE_TEMPLATE;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.BACK_UP_POWER_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.DELAYED_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.GREEN_POWER_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.ON_HOLD_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.PROCESSING_JOB_ID;
import static com.greencloud.application.messages.domain.constants.MessageConversationConstants.SCHEDULED_JOB_ID;
import static com.greencloud.commons.job.ClientJobStatusEnum.DELAYED;
import static com.greencloud.commons.job.ClientJobStatusEnum.IN_PROGRESS;
import static com.greencloud.commons.job.ClientJobStatusEnum.ON_BACK_UP;
import static com.greencloud.commons.job.ClientJobStatusEnum.ON_HOLD;
import static com.greencloud.commons.job.ClientJobStatusEnum.PROCESSED;
import static com.greencloud.commons.job.ClientJobStatusEnum.SCHEDULED;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.greencloud.application.agents.client.ClientAgent;
import com.greencloud.application.agents.client.domain.JobPart;
import com.greencloud.application.agents.client.management.ClientStateManagement;
import com.greencloud.commons.job.ClientJobStatusEnum;
import com.gui.agents.ClientAgentNode;

import jade.lang.acl.ACLMessage;

// TODO add tests for remaining statuses - next pull request
@ExtendWith(MockitoExtension.class)
class ListenForJobUpdateUnitTest {

	private static final String JOB_ID = "1";
	private static final String JOB_PART_ID = "1#part1";

	@Mock
	ClientAgent clientAgent;
	@Mock
	ClientStateManagement mockClientManagement;
	@Mock
	ClientAgentNode clientAgentNode;
	@Mock
	JobPart jobPart;

	@InjectMocks
	ListenForJobUpdate listenForJobUpdate;

	Map<String, JobPart> jobParts;

	@BeforeEach
	void init() {
		jobParts = new HashMap<>();
		jobParts.put(JOB_PART_ID, jobPart);
		when(clientAgent.getAgentNode()).thenReturn(clientAgentNode);
		when(clientAgent.manage()).thenReturn(mockClientManagement);
	}

	@ParameterizedTest
	@MethodSource("jobStatusProvider")
	void shouldCorrectlyProcessesJobStatus(String conversationId, ClientJobStatusEnum status) {
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
			ClientJobStatusEnum currentStatus) {
		// given
		var message = messageBuilder(conversationId, JOB_PART_ID);
		when(clientAgent.receive(CLIENT_JOB_UPDATE_TEMPLATE)).thenReturn(message);
		when(clientAgent.getJobParts()).thenReturn(jobParts);
		when(clientAgent.isSplit()).thenReturn(true);
		lenient().when(clientAgent.manage().getCurrentJobStatus()).thenReturn(currentStatus);

		// when
		listenForJobUpdate.action();

		// then
		verify(jobPart).updateJobStatusDuration(status);
		verify(clientAgentNode).updateJobStatus(status, JOB_PART_ID);
	}

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

	private static ACLMessage messageBuilder(String conversationId, String content) {
		ACLMessage message = new ACLMessage();
		message.setConversationId(conversationId);
		message.setContent(content);
		return message;
	}
}
