package org.greencloud.weatherapi.api;

import static java.lang.String.format;
import static java.lang.System.getenv;
import static java.time.Duration.between;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.greencloud.commons.utils.logger.LogBuilder.constructLogBuilder;
import static org.greencloud.commons.utils.mapper.MapperExpander.expandMapperWithInstantDeserializer;
import static org.greencloud.commons.utils.time.TimeComparator.isWithinTimeStamp;
import static org.greencloud.commons.utils.time.TimeConverter.convertToRealTimeElapsing;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.greencloud.weatherapi.constants.MockWeatherConstants.CURRENT_WEATHER_BAD_STUB;
import static org.greencloud.weatherapi.constants.MockWeatherConstants.CURRENT_WEATHER_STUB;
import static org.greencloud.weatherapi.constants.MockWeatherConstants.FUTURE_WEATHER_STUB;
import static org.greencloud.weatherapi.mapper.WeatherMapper.mapToCurrentWeather;
import static org.greencloud.weatherapi.mapper.WeatherMapper.mapToForecast;
import static org.greencloud.weatherapi.mapper.WeatherMapper.mapToFutureWeather;
import static org.greencloud.weatherapi.mapper.WeatherMapper.mapToLocalWeatherDataTimeSeries;
import static org.jrba.utils.file.FileReader.buildResourceFilePath;
import static org.jrba.utils.file.FileReader.readFile;
import static org.jrba.utils.mapper.JsonMapper.getMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.greencloud.commons.domain.location.Location;
import org.greencloud.commons.enums.weather.WeatherFetchingModeType;
import org.greencloud.commons.exception.InvalidConfigurationException;
import org.greencloud.commons.exception.InvalidScenarioException;
import org.greencloud.commons.utils.logger.LogBuilder;
import org.greencloud.weatherapi.domain.AbstractWeather;
import org.greencloud.weatherapi.domain.CurrentWeather;
import org.greencloud.weatherapi.domain.Forecast;
import org.greencloud.weatherapi.domain.FutureWeather;
import org.greencloud.weatherapi.domain.LocalWeatherConfig;
import org.greencloud.weatherapi.domain.LocalWeatherData;
import org.greencloud.weatherapi.domain.LocalWeatherDataTimeSeries;
import org.greencloud.weatherapi.domain.OfflineWeatherConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Service used to communicate with external weather API
 */
public class OpenWeatherMapApi {

	private static final LogBuilder log = constructLogBuilder(OpenWeatherMapApi.class);

	private static final String LOCAL_WEATHER_DATA_DIR = "data";
	private static final String LOCAL_WEATHER_CONFIG_FILE = "local_weather_config.json";
	private static final String WEATHER_URL =
			"https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s&units=metric";
	private static final String FORECAST_URL =
			"https://api.openweathermap.org/data/2.5/forecast?lat=%s&lon=%s&appid=%s&units=metric";

	private final WeatherFetchingModeType mode;
	private String apiKey;
	private OkHttpClient client;
	private OfflineWeatherConfig offlineWeatherConfig;
	private Map<Location, LocalWeatherDataTimeSeries> offlineWeatherData;

	/**
	 * Default constructor that establishes the communication with API
	 */
	public OpenWeatherMapApi(final WeatherFetchingModeType modeType) {
		final Properties properties = new Properties();
		this.mode = modeType;

		try (final InputStream res = getClass().getClassLoader().getResourceAsStream("api.properties")) {
			properties.load(res);
		} catch (FileNotFoundException fileNotFoundException) {
			log.messageError("Could not find the properties file", fileNotFoundException);
		} catch (Exception exception) {
			log.messageError("Could not load properties file {}", exception.toString());
		}

		if (modeType == WeatherFetchingModeType.API) {
			this.apiKey = ofNullable(getenv("GC_WEATHER_API_KEY")).orElse(properties.getProperty("weather.api.key"));
			this.client = new OkHttpClient();
		} else {
			this.offlineWeatherConfig = parseLocalWeatherData();
			this.offlineWeatherData = new HashMap<>();
		}
	}

	/**
	 * Provides weather for the current location for the current moment.
	 *
	 * @param location location at which weather is going to be retrieved
	 * @return {@link CurrentWeather} for the provided location
	 */
	public CurrentWeather getWeather(final Location location) {
		return switch (mode) {
			case API -> fetchWeatherUsingAPI(location);
			case LOCAL -> fetchWeatherFromLocalData(location);
			case MOCK -> CURRENT_WEATHER_STUB;
		};
	}

	/**
	 * Get 5 day 3-hour forecast for the location.
	 *
	 * @param location location at which weather is going to be retrieved
	 * @return {@link Forecast} with a list of {@link CurrentWeather}
	 */
	public Forecast getForecast(final Location location) {
		return switch (mode) {
			case API -> fetchForecastUsingAPI(location);
			case LOCAL -> fetchForecastFromLocalData(location);
			case MOCK -> mapToForecast(List.of(FUTURE_WEATHER_STUB));
		};
	}

	private CurrentWeather fetchWeatherFromLocalData(final Location location) {
		offlineWeatherData.computeIfAbsent(location, this::loadLocationOfflineData);

		final Instant time = convertToRealTimeElapsing(getCurrentTime());
		final LocalWeatherDataTimeSeries weatherDataForLocation = offlineWeatherData.get(location);
		final Map<Instant, LocalWeatherData> weatherTimeSeries = weatherDataForLocation.getWeatherTimeSeries();
		final LocalWeatherData weatherData = weatherTimeSeries.getOrDefault(time, null);

		if (isNull(weatherData)) {
			log.messageInfo("No local weather data found for location {} at time {}. "
					+ "Looking for possibly closest weather.", location, time);
			final int minutesToLookBack = weatherDataForLocation.getWeatherRelevanceMinutes();

			return weatherTimeSeries.entrySet().stream()
					.filter(entry -> isWeatherStillRelevant(entry.getKey(), time, minutesToLookBack))
					.findFirst()
					.map(weatherEntry -> mapToCurrentWeather(weatherEntry.getValue(), location))
					.orElse((CurrentWeather) handleNoRelevantDataPresent(location, time));
		}
		return mapToCurrentWeather(weatherData, location);
	}

	private CurrentWeather fetchWeatherUsingAPI(final Location location) {
		final String url = format(WEATHER_URL, location.getLatitude(), location.getLongitude(), apiKey);
		final Request request = new Request.Builder().url(url).build();

		try (final Response response = client.newCall(request).execute()) {
			return getMapper().readValue(response.body().string(), CurrentWeather.class);
		} catch (final IOException | NullPointerException e) {
			log.messageError("Network error fetching weather", e);
		}
		return null;
	}

	private Forecast fetchForecastFromLocalData(final Location location) {
		offlineWeatherData.computeIfAbsent(location, this::loadLocationOfflineData);

		final Instant time = convertToRealTimeElapsing(getCurrentTime());
		final LocalWeatherDataTimeSeries weatherDataForLocation = offlineWeatherData.get(location);
		final List<FutureWeather> weatherForecast = weatherDataForLocation.getWeatherTimeSeries().entrySet().stream()
				.filter(entry -> isWithinTimeStamp(time, time.plus(5, DAYS), entry.getKey()))
				.map(entry -> mapToFutureWeather(entry.getValue(), entry.getKey()))
				.toList();

		if (weatherForecast.isEmpty()) {
			log.messageError("No local weather forecast data found for location %s", location);
			return null;
		}
		return mapToForecast(weatherForecast);
	}

	private Forecast fetchForecastUsingAPI(final Location location) {
		final String url = format(FORECAST_URL, location.getLatitude(), location.getLongitude(), apiKey);
		final Request request = new Request.Builder().url(url).build();

		try (final Response response = client.newCall(request).execute()) {
			return getMapper().readValue(response.body().string(), Forecast.class);
		} catch (IOException | NullPointerException e) {
			log.messageError("Network error fetching weather", e);
		}

		return null;
	}

	private LocalWeatherDataTimeSeries loadLocationOfflineData(final Location location) {
		final LocalWeatherConfig config = offlineWeatherConfig.getLocalConfigs().stream()
				.filter(c -> c.getLocation().equals(location))
				.findFirst()
				.orElseThrow(() -> new InvalidScenarioException(
						format("No local weather data found for location %s", location), null));

		try {
			final String filePath = buildResourceFilePath(LOCAL_WEATHER_DATA_DIR, config.getDataSource());
			final File weatherDataFile = readFile(filePath);
			final ObjectMapper mapper = getMapper();
			expandMapperWithInstantDeserializer(mapper);

			final LocalWeatherDataTimeSeries data = mapper.readValue(weatherDataFile, LocalWeatherDataTimeSeries.class);
			return mapToLocalWeatherDataTimeSeries(data, config);
		} catch (IOException e) {
			throw new InvalidConfigurationException(
					format("Failed to load local weather data for location %s from file %s", location,
							config.getDataSource()), e);
		}
	}

	private boolean isWeatherStillRelevant(final Instant weatherMeasurementTime, final Instant requestedForecastTime,
			final int weatherRelevanceMinutes) {
		return weatherMeasurementTime.isBefore(requestedForecastTime) &&
				between(weatherMeasurementTime, requestedForecastTime).toMinutes() <= weatherRelevanceMinutes;
	}

	private AbstractWeather handleNoRelevantDataPresent(final Location location, final Instant time) {
		log.messageError("No relevant data was found for location {} at time {}.", location, time);
		return CURRENT_WEATHER_BAD_STUB;
	}

	private OfflineWeatherConfig parseLocalWeatherData() {
		try {
			final File weatherConfig = readFile(LOCAL_WEATHER_CONFIG_FILE);
			return getMapper().readValue(weatherConfig, OfflineWeatherConfig.class);
		} catch (IOException e) {
			throw new InvalidConfigurationException(
					format("Failed to parse scenario weather configuration file \"%s\"", LOCAL_WEATHER_CONFIG_FILE), e);
		}
	}
}
