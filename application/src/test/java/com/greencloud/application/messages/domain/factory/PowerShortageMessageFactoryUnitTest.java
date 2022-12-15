package com.greencloud.application.messages.domain.factory;

import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.POWER_SHORTAGE_ALERT_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.POWER_SHORTAGE_FINISH_ALERT_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL;
import static com.greencloud.application.messages.domain.factory.PowerShortageMessageFactory.prepareGreenPowerSupplyRequest;
import static com.greencloud.application.messages.domain.factory.PowerShortageMessageFactory.prepareJobPowerShortageInformation;
import static com.greencloud.application.messages.domain.factory.PowerShortageMessageFactory.preparePowerShortageTransferRequest;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.greencloud.application.domain.job.ImmutableJobInstanceIdentifier;
import com.greencloud.application.domain.job.JobInstanceIdentifier;
import com.greencloud.application.domain.powershortage.ImmutablePowerShortageJob;
import com.greencloud.application.domain.powershortage.PowerShortageJob;
import com.greencloud.commons.job.ClientJob;
import com.greencloud.commons.job.ImmutableClientJob;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

class PowerShortageMessageFactoryUnitTest {

	@Test
	@DisplayName("Test prepare power shortage transfer request")
	void testPreparePowerShortageTransferRequest() {
		final AID mockReceiver = mock(AID.class);
		doReturn("test_receiver").when(mockReceiver).getName();

		final PowerShortageJob mockPowerShortageJob = ImmutablePowerShortageJob.builder()
				.powerShortageStart(Instant.parse("2022-01-01T13:30:00.000Z"))
				.jobInstanceId(ImmutableJobInstanceIdentifier.builder()
						.jobId("1")
						.startTime(Instant.parse("2022-01-01T14:30:00.000Z"))
						.build())
				.build();

		final String expectedContent =
				"{\"jobInstanceId\":{\"jobId\":\"1\",\"startTime\":1641047400.000000000},"
						+ "\"powerShortageStart\":1641043800.000000000}";

		final ACLMessage result = preparePowerShortageTransferRequest(mockPowerShortageJob, mockReceiver);
		final Iterable<AID> receiverIt = result::getAllReceiver;

		assertThat(result.getProtocol()).isEqualTo(POWER_SHORTAGE_ALERT_PROTOCOL);
		assertThat(result.getPerformative()).isEqualTo(REQUEST);
		assertThat(result.getContent()).isEqualTo(expectedContent);
		assertThat(receiverIt).allMatch(aid -> aid.equals(mockReceiver));
	}

	@Test
	@DisplayName("Test prepare green power supply request")
	void testPreparePowerGreenPowerSupplyRequest() {
		final AID mockReceiver = mock(AID.class);
		doReturn("test_receiver").when(mockReceiver).getName();

		final ClientJob mockJob = ImmutableClientJob.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T13:30:00.000Z"))
				.endTime(Instant.parse("2022-01-01T14:30:00.000Z"))
				.deadline(Instant.parse("2022-01-01T16:30:00.000Z"))
				.clientIdentifier("test_client")
				.power(10)
				.build();

		final String expectedContent = "{\"jobId\":\"1\",\"startTime\":1641043800.000000000}";

		final ACLMessage result = prepareGreenPowerSupplyRequest(mockJob, mockReceiver);
		final Iterable<AID> receiverIt = result::getAllReceiver;

		assertThat(result.getProtocol()).isEqualTo(SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL);
		assertThat(result.getPerformative()).isEqualTo(REQUEST);
		assertThat(result.getContent()).isEqualTo(expectedContent);
		assertThat(receiverIt).allMatch(aid -> aid.equals(mockReceiver));
	}

	@Test
	@DisplayName("Test prepare job power shortage information")
	void testPrepareJobPowerShortageInformation() {
		final AID mockReceiver = mock(AID.class);
		doReturn("test_receiver").when(mockReceiver).getName();

		final JobInstanceIdentifier mockJobInstance = ImmutableJobInstanceIdentifier.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T13:30:00.000Z"))
				.build();
		final String protocol = POWER_SHORTAGE_FINISH_ALERT_PROTOCOL;

		final String expectedContent = "{\"jobId\":\"1\",\"startTime\":1641043800.000000000}";

		final ACLMessage result = prepareJobPowerShortageInformation(mockJobInstance, mockReceiver, protocol);
		final Iterable<AID> receiverIt = result::getAllReceiver;

		assertThat(result.getProtocol()).isEqualTo(protocol);
		assertThat(result.getPerformative()).isEqualTo(INFORM);
		assertThat(result.getContent()).isEqualTo(expectedContent);
		assertThat(receiverIt).allMatch(aid -> aid.equals(mockReceiver));
	}
}
