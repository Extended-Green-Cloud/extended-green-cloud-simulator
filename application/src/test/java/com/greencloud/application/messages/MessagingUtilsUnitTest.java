package com.greencloud.application.messages;

import static com.greencloud.application.mapper.JsonMapper.getMapper;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.PROPOSE;
import static jade.lang.acl.ACLMessage.REFUSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.quality.Strictness.LENIENT;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.greencloud.application.agents.AbstractAgent;
import com.greencloud.application.agents.client.ClientAgent;
import com.greencloud.application.domain.job.ImmutableJobInstanceIdentifier;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.exception.IncorrectMessageContentException;
import com.greencloud.commons.job.ImmutablePowerJob;
import com.greencloud.commons.job.PowerJob;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class MessagingUtilsUnitTest {

	@Test
	@DisplayName("Test retrieve empty proposals")
	void testRetrieveProposalsEmpty() {
		assertThat(MessagingUtils.retrieveProposals(new Vector<>())).isEmpty();
	}

	@Test
	@DisplayName("Test retrieve non empty proposals")
	void testRetrieveProposals() {
		final Vector<ACLMessage> messages = new Vector<>(prepareMessages());

		assertThat(MessagingUtils.retrieveProposals(messages))
				.hasSize(2)
				.areExactly(2,
						new Condition<>(message -> List.of("Message 1", "Message 2").contains(message.getContent()),
								"secondMsg"))
				.areNot(new Condition<>(message -> message.getContent().equals("Message 3"), "secondMsg"));
	}

	@Test
	@DisplayName("Test retrieve empty set of messages for performative")
	void testRetrieveForPerformativeEmpty() {
		assertThat(MessagingUtils.retrieveForPerformative(new Vector<>(), INFORM)).isEmpty();
	}

	@Test
	@DisplayName("Test retrieve non empty set of messages for performative")
	void testRetrieveForPerformative() {
		final Vector<ACLMessage> messages = new Vector<>(prepareMessages());

		assertThat(MessagingUtils.retrieveForPerformative(messages, REFUSE))
				.hasSize(1)
				.areExactly(1,
						new Condition<>(message -> Objects.equals("Message 3", message.getContent()),
								"thirdMsg"));
	}

	@Test
	@DisplayName("Test reject job offers except chosen one which is null")
	void testRejectJobOffers() {
		final AbstractAgent mockAgent = mock(ClientAgent.class);
		final JobInstanceIdentifier jobInstance = ImmutableJobInstanceIdentifier.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T11:00:00.000Z"))
				.build();
		final List<ACLMessage> messages = prepareMessages();
		MessagingUtils.rejectJobOffers(mockAgent, jobInstance, null, messages);

		final ArgumentMatcher<ACLMessage> matcher = argument -> Objects.equals(
				"{\"jobId\":\"1\",\"startTime\":1641034800.000000000}", argument.getContent());
		verify(mockAgent, times(3)).send(argThat(matcher));
	}

	@Test
	@DisplayName("Test reject job offers except chosen one which is not null")
	void testRejectJobOffersWithChosen() {
		final AbstractAgent mockAgent = mock(ClientAgent.class);
		final JobInstanceIdentifier jobInstance = ImmutableJobInstanceIdentifier.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T11:00:00.000Z"))
				.build();
		final List<ACLMessage> messages = prepareMessages();
		MessagingUtils.rejectJobOffers(mockAgent, jobInstance, messages.get(0), messages);

		final ArgumentMatcher<ACLMessage> matcher = argument -> Objects.equals(
				"{\"jobId\":\"1\",\"startTime\":1641034800.000000000}", argument.getContent());
		verify(mockAgent, times(2)).send(argThat(matcher));
	}

	@Test
	@DisplayName("Test reject job offers for jobId except chosen one which is null")
	void testRejectJobOffersForJobId() {
		final AbstractAgent mockAgent = mock(ClientAgent.class);
		final List<ACLMessage> messages = prepareMessages();
		MessagingUtils.rejectJobOffers(mockAgent, "1", null, messages);

		final ArgumentMatcher<ACLMessage> matcher = argument -> Objects.equals("1", argument.getContent());
		verify(mockAgent, times(3)).send(argThat(matcher));
	}

	@Test
	@DisplayName("Test reject job offers for jobId except chosen one which is not null")
	void testRejectJobOffersForJobIdWithChosen() {
		final AbstractAgent mockAgent = mock(ClientAgent.class);
		final List<ACLMessage> messages = prepareMessages();
		MessagingUtils.rejectJobOffers(mockAgent, "1", messages.get(0), messages);

		final ArgumentMatcher<ACLMessage> matcher = argument -> Objects.equals("1", argument.getContent());
		verify(mockAgent, times(2)).send(argThat(matcher));
	}

	@Test
	@DisplayName("Test retrieve valid proposal (with valid one)")
	void testRetrieveValidProposals() throws JsonProcessingException {
		final JobInstanceIdentifier jobInstance = ImmutableJobInstanceIdentifier.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T11:00:00.000Z"))
				.build();
		final ACLMessage msg = new ACLMessage(PROPOSE);
		msg.setContent(getMapper().writeValueAsString(jobInstance));

		assertThat(MessagingUtils.retrieveValidMessages(List.of(msg), JobInstanceIdentifier.class))
				.hasSize(1)
				.contains(msg);
	}

	@Test
	@DisplayName("Test retrieve valid proposal (with invalid one)")
	void testRetrieveValidProposalsInvalid() throws JsonProcessingException {
		final JobInstanceIdentifier jobInstance = ImmutableJobInstanceIdentifier.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T11:00:00.000Z"))
				.build();
		final ACLMessage msg = new ACLMessage(PROPOSE);
		msg.setContent(getMapper().writeValueAsString(jobInstance));

		assertThat(MessagingUtils.retrieveValidMessages(List.of(msg), PowerJob.class)).isEmpty();
	}

	@Test
	@DisplayName("Test read message content (successful)")
	void testReadMessageContent() throws JsonProcessingException {
		final JobInstanceIdentifier jobInstance = ImmutableJobInstanceIdentifier.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T11:00:00.000Z"))
				.build();
		final ACLMessage msg = new ACLMessage(PROPOSE);
		msg.setContent(getMapper().writeValueAsString(jobInstance));

		assertThat(MessagingUtils.readMessageContent(msg, JobInstanceIdentifier.class))
				.isEqualTo(jobInstance);
	}

	@Test
	@DisplayName("Test read message content (unsuccessful)")
	void testReadMessageContentInvalid() throws JsonProcessingException {
		final JobInstanceIdentifier jobInstance = ImmutableJobInstanceIdentifier.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T11:00:00.000Z"))
				.build();
		final ACLMessage msg = new ACLMessage(PROPOSE);
		msg.setContent(getMapper().writeValueAsString(jobInstance));

		assertThatThrownBy(() -> MessagingUtils.readMessageContent(msg, PowerJob.class))
				.isInstanceOf(IncorrectMessageContentException.class);
	}

	@Test
	@DisplayName("Test read message list content (successful)")
	void testReadMessageListContent() throws JsonProcessingException {
		final JobInstanceIdentifier jobInstance1 = ImmutableJobInstanceIdentifier.builder()
				.jobId("2")
				.startTime(Instant.parse("2022-01-01T11:00:00.000Z"))
				.build();
		final JobInstanceIdentifier jobInstance2 = ImmutableJobInstanceIdentifier.builder()
				.jobId("2")
				.startTime(Instant.parse("2022-01-01T11:00:00.000Z"))
				.build();
		final ACLMessage msg = new ACLMessage(PROPOSE);
		msg.setContent(getMapper().writeValueAsString(List.of(jobInstance1, jobInstance2)));

		assertThat(MessagingUtils.readMessageListContent(msg, JobInstanceIdentifier.class))
				.hasSize(2)
				.containsExactly(jobInstance1, jobInstance2);
	}

	@Test
	@DisplayName("Test read message list content (unsuccessful)")
	void testReadMessageListContentInvalid() throws JsonProcessingException {
		final JobInstanceIdentifier jobInstance = ImmutableJobInstanceIdentifier.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T11:00:00.000Z"))
				.build();
		final PowerJob powerJob = ImmutablePowerJob.builder()
				.jobId("1")
				.power(50)
				.deadline(Instant.parse("2022-01-01T15:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T13:00:00.000Z"))
				.startTime(Instant.parse("2022-01-01T11:00:00.000Z"))
				.build();
		final ACLMessage msg = new ACLMessage(PROPOSE);
		msg.setContent(getMapper().writeValueAsString(List.of(jobInstance, powerJob)));

		assertThatThrownBy(() -> MessagingUtils.readMessageListContent(msg, PowerJob.class))
				.isInstanceOf(IncorrectMessageContentException.class);
	}

	private List<ACLMessage> prepareMessages() {
		final AID aid1 = mock(AID.class);
		final AID aid2 = mock(AID.class);
		final AID aid3 = mock(AID.class);

		doReturn("Sender1").when(aid1).getName();
		doReturn("Sender2").when(aid2).getName();
		doReturn("Sender3").when(aid3).getName();

		final ACLMessage aclMessage1 = new ACLMessage(PROPOSE);
		aclMessage1.setContent("Message 1");
		aclMessage1.setSender(aid1);
		final ACLMessage aclMessage2 = new ACLMessage(PROPOSE);
		aclMessage2.setContent("Message 2");
		aclMessage1.setSender(aid2);
		final ACLMessage aclMessage3 = new ACLMessage(REFUSE);
		aclMessage3.setContent("Message 3");
		aclMessage1.setSender(aid3);
		return List.of(aclMessage1, aclMessage2, aclMessage3);
	}
}
