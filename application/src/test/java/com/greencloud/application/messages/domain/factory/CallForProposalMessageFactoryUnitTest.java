package com.greencloud.application.messages.domain.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.greencloud.application.domain.job.ImmutableJobInstanceIdentifier;
import com.greencloud.commons.job.ImmutablePowerJob;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

class CallForProposalMessageFactoryUnitTest {

	private static Stream<Arguments> parametersMessageParams() {
		final AID aid1 = mock(AID.class);
		final AID aid2 = mock(AID.class);
		final AID aid3 = mock(AID.class);

		doReturn("Sender1").when(aid1).getName();
		doReturn("Sender2").when(aid2).getName();
		doReturn("Sender3").when(aid3).getName();

		return Stream.of(
				arguments(
						ImmutableJobInstanceIdentifier.builder().jobId("1")
								.startTime(Instant.parse("2022-01-01T13:30:00.000Z"))
								.build(), List.of(aid1), "TEST_PROTOCOL1",
						"{\"jobId\":\"1\",\"startTime\":1641043800.000000000}"),
				arguments(
						ImmutablePowerJob.builder().jobId("1").deadline(Instant.parse("2022-01-01T15:30:00.000Z"))
								.endTime(Instant.parse("2022-01-01T14:30:00.000Z"))
								.startTime(Instant.parse("2022-01-01T13:30:00.000Z")).power(20).build(),
						List.of(aid2, aid3),
						"TEST_PROTOCOL2",
						"{\"jobId\":\"1\",\"startTime\":1641043800.000000000,\"endTime\":1641047400.000000000,\"deadline\":1641051000.000000000,\"power\":20}"));
	}

	@ParameterizedTest
	@MethodSource("parametersMessageParams")
	@DisplayName("Test creating call for proposal message")
	void testCreateCallForProposal(Object content, List<AID> receivers, String protocol, String expectedContent) {
		final ACLMessage result = CallForProposalMessageFactory.createCallForProposal(content, receivers, protocol);
		final Iterable<AID> receiversIt = result::getAllReceiver;

		assertThat(result.getContent()).isEqualTo(expectedContent);
		assertThat(receiversIt).anyMatch((val) -> receivers.stream().map(AID::getName)
				.anyMatch((aid) -> aid.equals(val.getName())));
		assertThat(result.getProtocol()).isEqualTo(protocol);
	}
}
