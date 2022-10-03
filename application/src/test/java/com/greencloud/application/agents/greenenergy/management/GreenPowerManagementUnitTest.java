package com.greencloud.application.agents.greenenergy.management;

import static com.greencloud.application.agents.greenenergy.domain.GreenEnergySourceTypeEnum.WIND;
import static com.greencloud.application.constants.CacheTestConstants.MOCK_LOCATION;
import static com.greencloud.application.constants.CacheTestConstants.MOCK_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.quality.Strictness.LENIENT;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.greencloud.application.domain.job.ImmutablePowerJob;
import com.greencloud.application.domain.job.JobStatusEnum;
import com.greencloud.application.domain.job.PowerJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.domain.ImmutableMonitoringData;
import com.greencloud.application.domain.ImmutableWeatherData;
import com.greencloud.application.domain.MonitoringData;
import com.greencloud.application.domain.WeatherData;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class GreenPowerManagementUnitTest {

	// MOCK OBJECTS

	public static Map<PowerJob, JobStatusEnum> MOCK_JOBS;
	public static final int MOCK_CAPACITY = 200;
	public static final WeatherData MOCK_WEATHER = ImmutableWeatherData.builder()
			.time(MOCK_TIME)
			.cloudCover(5.0)
			.windSpeed(10.0)
			.temperature(20.0)
			.build();

	@Mock
	private GreenEnergyAgent mockGreenEnergyAgent;
	@Mock
	private GreenEnergyPowerManagement greenPowerManagement;

	@BeforeEach
	void init() {
		MOCK_JOBS = setUpGreenEnergyJobs();
		doReturn(MOCK_LOCATION).when(mockGreenEnergyAgent).getLocation();
		greenPowerManagement = new GreenEnergyPowerManagement(MOCK_CAPACITY, mockGreenEnergyAgent);
		setUpGreenEnergyMock();
	}

	@Test
	@DisplayName("Test get available power from monitoring data for exact time")
	void testGetAvailablePowerMonitoringDataExactTime() {
		doReturn(WIND).when(mockGreenEnergyAgent).getEnergyType();
		final MonitoringData mockMonitoringData = ImmutableMonitoringData.builder()
				.addWeatherData(MOCK_WEATHER)
				.build();

		final double result = greenPowerManagement.getAvailablePower(mockMonitoringData, MOCK_TIME);
		assertThat(result).isEqualTo(200);
	}

	@Test
	@DisplayName("Test get available power from monitoring data for nearest time")
	void testGetAvailablePowerMonitoringDataNearestTime() {
		doReturn(WIND).when(mockGreenEnergyAgent).getEnergyType();
		final Instant time1 = Instant.parse("2022-01-01T10:00:00.000Z");
		final Instant time2 = Instant.parse("2022-01-01T13:00:00.000Z");
		final MonitoringData mockMonitoringData = ImmutableMonitoringData.builder()
				.addWeatherData(ImmutableWeatherData.copyOf(MOCK_WEATHER)
						.withWindSpeed(200.0)
						.withTime(time1))
				.addWeatherData(ImmutableWeatherData.copyOf(MOCK_WEATHER)
						.withWindSpeed(100.0)
						.withTime(time2))
				.build();

		final double result = greenPowerManagement.getAvailablePower(mockMonitoringData, MOCK_TIME);
		assertThat(result).isEqualTo(200);
	}

	@Test
	@DisplayName("Test set new maximum capacity")
	void testSetNewMaximumCapacity() {
		assertThat(greenPowerManagement.getMaximumCapacity()).isEqualTo(MOCK_CAPACITY);
		greenPowerManagement.setMaximumCapacity(100);
		assertThat(greenPowerManagement.getMaximumCapacity()).isEqualTo(100);
	}

	@Test
	@DisplayName("Test updating maximum capacity")
	void testUpdatingMaximumCapacity() {
		final int newCapacity = 1000;
		mockGreenEnergyAgent.managePower().updateMaximumCapacity(newCapacity);

		assertThat(mockGreenEnergyAgent.managePower().getMaximumCapacity()).isEqualTo(1000);
		assertThat(mockGreenEnergyAgent.managePower().getInitialMaximumCapacity()).isEqualTo(MOCK_CAPACITY);
	}

	// PREPARING TEST DATA

	/**
	 * Class creates mock green energy power jobs used in test scenarios.
	 * The following structure was used:
	 * PowerJob1 -> power: 10, time: 08:00 - 10:00, status: IN_PROGRESS,
	 * PowerJob2 -> power: 20, time: 07:00 - 11:00, status: IN_PROGRESS
	 * PowerJob3 -> power: 50,  time: 06:00 - 15:00, status: ON_HOLD
	 * PowerJob4 -> power: 10,  time: 09:00 - 12:00, status: ON_HOLD
	 * PowerJob5 -> power: 25, time: 11:00 - 12:00, status: ACCEPTED
	 */
	private Map<PowerJob, JobStatusEnum> setUpGreenEnergyJobs() {
		final PowerJob mockJob1 = ImmutablePowerJob.builder()
				.jobId("1")
				.startTime(Instant.parse("2022-01-01T08:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T10:00:00.000Z"))
				.power(10)
				.build();
		final PowerJob mockJob2 = ImmutablePowerJob.builder()
				.jobId("2")
				.startTime(Instant.parse("2022-01-01T07:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T11:00:00.000Z"))
				.power(20)
				.build();
		final PowerJob mockJob3 = ImmutablePowerJob.builder()
				.jobId("3")
				.startTime(Instant.parse("2022-01-01T06:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T15:00:00.000Z"))
				.power(50)
				.build();
		final PowerJob mockJob4 = ImmutablePowerJob.builder()
				.jobId("4")
				.startTime(Instant.parse("2022-01-01T09:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T12:00:00.000Z"))
				.power(10)
				.build();
		final PowerJob mockJob5 = ImmutablePowerJob.builder()
				.jobId("5")
				.startTime(Instant.parse("2022-01-01T11:00:00.000Z"))
				.endTime(Instant.parse("2022-01-01T12:00:00.000Z"))
				.power(25)
				.build();
		final Map<PowerJob, JobStatusEnum> mockJobMap = new HashMap<>();
		mockJobMap.put(mockJob1, JobStatusEnum.IN_PROGRESS);
		mockJobMap.put(mockJob2, JobStatusEnum.IN_PROGRESS);
		mockJobMap.put(mockJob3, JobStatusEnum.ON_HOLD);
		mockJobMap.put(mockJob4, JobStatusEnum.ON_HOLD);
		mockJobMap.put(mockJob5, JobStatusEnum.ACCEPTED);
		return mockJobMap;
	}

	private void setUpGreenEnergyMock() {
		mockGreenEnergyAgent = spy(GreenEnergyAgent.class);
		mockGreenEnergyAgent.getPowerJobs().putAll(MOCK_JOBS);
		greenPowerManagement = spy(new GreenEnergyPowerManagement(MOCK_CAPACITY, mockGreenEnergyAgent));
		mockGreenEnergyAgent.setGreenPowerManagement(greenPowerManagement);

		doReturn(greenPowerManagement).when(mockGreenEnergyAgent).managePower();
		doNothing().when(mockGreenEnergyAgent).addBehaviour(any());
		doNothing().when(mockGreenEnergyAgent).send(any());
	}
}
