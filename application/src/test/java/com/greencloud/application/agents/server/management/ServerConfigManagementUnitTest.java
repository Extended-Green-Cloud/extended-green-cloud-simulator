package com.greencloud.application.agents.server.management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
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

import com.greencloud.application.agents.server.ServerAgent;
import com.greencloud.application.domain.GreenSourceData;
import com.greencloud.application.domain.ImmutableGreenSourceData;
import com.greencloud.commons.job.ClientJob;
import com.greencloud.commons.job.ImmutableClientJob;

import jade.core.AID;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class ServerConfigManagementUnitTest {

	private static final double MOCK_PRICE = 10;
	@Mock
	private ServerAgent mockServerAgent;
	@Mock
	private ServerStateManagement serverStateManagement;
	private ServerConfigManagement serverConfigManagement;

	@BeforeEach
	void init() {
		mockServerAgent = mock(ServerAgent.class);

		serverConfigManagement = new ServerConfigManagement(mockServerAgent);
		serverConfigManagement.setWeightsForGreenSourcesMap(initMap());
		serverConfigManagement.setPricePerHour(MOCK_PRICE);

		serverStateManagement = spy(new ServerStateManagement(mockServerAgent));
		doReturn(serverStateManagement).when(mockServerAgent).manage();
		doReturn(serverConfigManagement).when(mockServerAgent).manageConfig();
	}

	//TESTS
	@Test
	void testGetGreenSourcePercentages() {
		Map<AID, Double> greenSourcePercentages = mockServerAgent.manageConfig().getPercentages();
		assertThat(greenSourcePercentages)
				.containsEntry(new AID("1", AID.ISGUID), 1.0 * 100 / 7)
				.containsEntry(new AID("2", AID.ISGUID), 1.0 * 100 / 7)
				.containsEntry(new AID("3", AID.ISGUID), 3.0 * 100 / 7)
				.containsEntry(new AID("4", AID.ISGUID), 2.0 * 100 / 7);
	}

	@Test
	@DisplayName("Test calculating price for job")
	void testPriceCalculation() {
		doReturn(prepareTestJob()).when(serverStateManagement).getJobById("1");
		final GreenSourceData mockGreenSourceData = ImmutableGreenSourceData.builder()
				.jobId("1")
				.availablePowerInTime(100)
				.pricePerPowerUnit(5)
				.powerPredictionError(0.02)
				.build();

		final double resultPrice = serverConfigManagement.calculateServicePrice(mockGreenSourceData);

		assertThat(resultPrice).isEqualTo(60);
	}

	private Map<AID, Integer> initMap() {
		Map<AID, Integer> map = new HashMap<>();
		map.put(new AID("1", AID.ISGUID), 1);
		map.put(new AID("2", AID.ISGUID), 1);
		map.put(new AID("3", AID.ISGUID), 3);
		map.put(new AID("4", AID.ISGUID), 2);
		return map;
	}

	private ClientJob prepareTestJob() {
		return ImmutableClientJob.builder()
				.jobId("1")
				.clientIdentifier("Test Client")
				.startTime(Instant.parse("2022-01-01T07:30:00.000Z"))
				.endTime(Instant.parse("2022-01-01T08:30:00.000Z"))
				.deadline(Instant.parse("2022-01-01T09:30:00.000Z"))
				.power(10)
				.build();
	}
}
