package com.greencloud.application.agents.greenenergy.management;

import static com.greencloud.application.agents.greenenergy.domain.GreenEnergySourceTypeEnum.SOLAR;
import static com.greencloud.application.agents.greenenergy.domain.GreenEnergySourceTypeEnum.WIND;
import static com.greencloud.application.constants.CacheTestConstants.MOCK_LOCATION;
import static com.greencloud.application.constants.CacheTestConstants.MOCK_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.quality.Strictness.LENIENT;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.assertj.core.data.Offset;
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
	public static final int MOCK_CAPACITY = 200;
	public static final WeatherData MOCK_WEATHER = ImmutableWeatherData.builder()
			.time(MOCK_TIME)
			.cloudCover(5.0)
			.windSpeed(10.0)
			.temperature(20.0)
			.build();
	public static final ZonedDateTime MOCK_ZONE_TIME = ZonedDateTime.from(MOCK_TIME.atZone(ZoneId.of("UTC")));

	@Mock
	private GreenEnergyAgent greenEnergyAgent;

	private GreenPowerManagement greenPowerManagement;

	@BeforeEach
	void init() {
		doReturn(MOCK_LOCATION).when(greenEnergyAgent).getLocation();
		greenPowerManagement = new GreenPowerManagement(MOCK_CAPACITY, greenEnergyAgent);
	}

	@Test
	@DisplayName("Test get available power for wind source")
	void testGetAvailablePowerWindSource() {
		doReturn(WIND).when(greenEnergyAgent).getEnergyType();
		final double result = greenPowerManagement.getAvailablePower(MOCK_WEATHER, MOCK_ZONE_TIME);

		assertThat(result).isEqualTo(200);
	}

	@Test
	@DisplayName("Test get available power for solar source")
	void testGetAvailablePowerSolarSource() {
		doReturn(SOLAR).when(greenEnergyAgent).getEnergyType();
		final double result = greenPowerManagement.getAvailablePower(MOCK_WEATHER, MOCK_ZONE_TIME);

		assertThat(result).isCloseTo(30, Offset.offset(0.01));
	}

	@Test
	@DisplayName("Test get available power for solar source during night-time")
	void testGetAvailablePowerSolarSourceNightTime() {
		doReturn(SOLAR).when(greenEnergyAgent).getEnergyType();
		final ZonedDateTime testZoneTime = ZonedDateTime.from(
				Instant.parse("2022-01-01T00:00:00.000Z").atZone(ZoneId.of("UTC")));
		final double result = greenPowerManagement.getAvailablePower(MOCK_WEATHER, testZoneTime);

		assertThat(result).isZero();
	}

	@Test
	@DisplayName("Test get available power from monitoring data for exact time")
	void testGetAvailablePowerMonitoringDataExactTime() {
		doReturn(WIND).when(greenEnergyAgent).getEnergyType();
		final MonitoringData mockMonitoringData = ImmutableMonitoringData.builder()
				.addWeatherData(MOCK_WEATHER)
				.build();

		final double result = greenPowerManagement.getAvailablePower(mockMonitoringData, MOCK_TIME);
		assertThat(result).isEqualTo(200);
	}

	@Test
	@DisplayName("Test get available power from monitoring data for nearest time")
	void testGetAvailablePowerMonitoringDataNearestTime() {
		doReturn(WIND).when(greenEnergyAgent).getEnergyType();
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
		assertThat(greenPowerManagement.getCurrentMaximumCapacity()).isEqualTo(MOCK_CAPACITY);
		greenPowerManagement.setCurrentMaximumCapacity(100);
		assertThat(greenPowerManagement.getCurrentMaximumCapacity()).isEqualTo(100);
	}
}
