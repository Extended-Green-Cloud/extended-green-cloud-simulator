package com.greencloud.application.messages.domain.factory;

import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.CNA_JOB_CFP_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.SCHEDULER_JOB_CFP_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.SERVER_JOB_CFP_PROTOCOL;
import static com.greencloud.application.messages.domain.factory.OfferMessageFactory.makeGreenEnergyPowerSupplyOffer;
import static com.greencloud.application.messages.domain.factory.OfferMessageFactory.makeJobOfferForClient;
import static com.greencloud.application.messages.domain.factory.OfferMessageFactory.makeServerJobOffer;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.PROCESSING;
import static jade.lang.acl.ACLMessage.CFP;
import static jade.lang.acl.ACLMessage.PROPOSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.agents.server.management.ServerStateManagement;
import com.greencloud.application.domain.ImmutableServerData;
import com.greencloud.application.domain.ServerData;
import com.greencloud.commons.job.ClientJob;
import com.greencloud.commons.job.ImmutableClientJob;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

class OfferMessageFactoryUnitTest {

	@Test
	@DisplayName("Test making job offer for client")
	void testMakeJobOfferForClient() {
		final AID mockScheduler = mock(AID.class);
		doReturn("test_scheduler").when(mockScheduler).getName();

		final ServerData serverData = ImmutableServerData.builder()
				.jobId("1")
				.availablePower(100)
				.servicePrice(50.0)
				.build();
		final double powerInUse = 50;

		final ACLMessage replyMessage = new ACLMessage(CFP);
		replyMessage.addReceiver(mockScheduler);
		replyMessage.setProtocol(SCHEDULER_JOB_CFP_PROTOCOL);
		replyMessage.setInReplyTo("R1671062222360_1");
		replyMessage.setConversationId("C805691330_Scheduler_1671062222359_0");

		final String expectedContent =
				"{\"jobId\":\"1\","
						+ "\"priceForJob\":50.0,"
						+ "\"powerInUse\":50.0}";

		final ACLMessage result = makeJobOfferForClient(serverData, powerInUse, replyMessage);
		final Iterable<AID> receiverIt = result::getAllReceiver;

		assertThat(result.getProtocol()).isEqualTo(SCHEDULER_JOB_CFP_PROTOCOL);
		assertThat(result.getInReplyTo()).isEqualTo("R1671062222360_1");
		assertThat(result.getConversationId()).isEqualTo("C805691330_Scheduler_1671062222359_0");
		assertThat(result.getPerformative()).isEqualTo(PROPOSE);
		assertThat(result.getContent()).isEqualTo(expectedContent);
		assertThat(receiverIt).allMatch(aid -> aid.equals(mockScheduler));
	}

	@Test
	@DisplayName("Test making server job offer")
	void testMakeServerJobOffer() {
		final AID mockCNA = mock(AID.class);
		doReturn("test_cna").when(mockCNA).getName();

		final ServerAgent mockServer = mock(ServerAgent.class);
		final ServerStateManagement mockManagement = mock(ServerStateManagement.class);

		final ClientJob mockJob = ImmutableClientJob.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T13:30:00.000Z"))
				.endTime(Instant.parse("2022-01-01T14:30:00.000Z"))
				.deadline(Instant.parse("2022-01-01T16:30:00.000Z"))
				.clientIdentifier("test_client")
				.power(10)
				.build();
		final String jobId = "1";
		final double servicePrice = 15;
		final int mockPower = 100;

		final ACLMessage replyMessage = new ACLMessage(CFP);
		replyMessage.addReceiver(mockCNA);
		replyMessage.setProtocol(CNA_JOB_CFP_PROTOCOL);
		replyMessage.setInReplyTo("R1671062222360_1");
		replyMessage.setConversationId("C805691330_CNA_1671062222359_0");

		doReturn(Map.of(mockJob, PROCESSING)).when(mockServer).getServerJobs();
		doReturn(mockManagement).when(mockServer).manage();
		doReturn(mockPower).when(mockManagement).getAvailableCapacity(Instant.parse("2022-01-01T13:30:00.000Z"),
				Instant.parse("2022-01-01T14:30:00.000Z"), null, null);

		final String expectedContent =
				"{\"servicePrice\":15.0,"
						+ "\"availablePower\":100,"
						+ "\"jobId\":\"1\"}";

		final ACLMessage result = makeServerJobOffer(mockServer, servicePrice, jobId, replyMessage);
		final Iterable<AID> receiverIt = result::getAllReceiver;

		assertThat(result.getProtocol()).isEqualTo(CNA_JOB_CFP_PROTOCOL);
		assertThat(result.getInReplyTo()).isEqualTo("R1671062222360_1");
		assertThat(result.getConversationId()).isEqualTo("C805691330_CNA_1671062222359_0");
		assertThat(result.getPerformative()).isEqualTo(PROPOSE);
		assertThat(result.getContent()).isEqualTo(expectedContent);
		assertThat(receiverIt).allMatch(aid -> aid.equals(mockCNA));
	}

	@Test
	@DisplayName("Test making green energy power supply offer")
	void testMakeGreenEnergyPowerSupplyOffer() {
		final AID mockServer = mock(AID.class);
		doReturn("test_server").when(mockServer).getName();

		final GreenEnergyAgent mockGreenEnergy = mock(GreenEnergyAgent.class);
		doReturn(10.0).when(mockGreenEnergy).getPricePerPowerUnit();

		final double averageAvailablePower = 150;
		final double predictionError = 0.02;
		final String jobId = "1";

		final ACLMessage replyMessage = new ACLMessage(CFP);
		replyMessage.addReceiver(mockServer);
		replyMessage.setProtocol(SERVER_JOB_CFP_PROTOCOL);
		replyMessage.setInReplyTo("R1671062222360_1");
		replyMessage.setConversationId("C805691330_Server_1671062222359_0");

		final String expectedContent =
				"{\"availablePowerInTime\":150.0,"
						+ "\"pricePerPowerUnit\":10.0,"
						+ "\"powerPredictionError\":0.02,"
						+ "\"jobId\":\"1\"}";

		final ACLMessage result = makeGreenEnergyPowerSupplyOffer(mockGreenEnergy, averageAvailablePower,
				predictionError, jobId, replyMessage);
		final Iterable<AID> receiverIt = result::getAllReceiver;

		assertThat(result.getProtocol()).isEqualTo(SERVER_JOB_CFP_PROTOCOL);
		assertThat(result.getInReplyTo()).isEqualTo("R1671062222360_1");
		assertThat(result.getConversationId()).isEqualTo("C805691330_Server_1671062222359_0");
		assertThat(result.getPerformative()).isEqualTo(PROPOSE);
		assertThat(result.getContent()).isEqualTo(expectedContent);
		assertThat(receiverIt).allMatch(aid -> aid.equals(mockServer));
	}
}
