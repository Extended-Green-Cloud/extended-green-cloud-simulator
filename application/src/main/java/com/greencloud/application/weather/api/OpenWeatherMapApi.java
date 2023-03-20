package com.greencloud.application.weather.api;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greencloud.application.mapper.JsonMapper;
import com.greencloud.application.weather.domain.CurrentWeather;
import com.greencloud.application.weather.domain.Forecast;
import com.greencloud.commons.domain.location.Location;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Service used to communicate with external weather API
 */
public class OpenWeatherMapApi {

	private static final Logger logger = getLogger(OpenWeatherMapApi.class);

	private static final String WEATHER_URL =
			"https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s&units=metric";
	private static final String FORECAST_URL =
			"https://api.openweathermap.org/data/2.5/forecast?lat=%s&lon=%s&appid=%s&units=metric";
	private static final ObjectMapper MAPPER = JsonMapper.getMapper();

	private final String apiKey;
	private final OkHttpClient client;

	/**
	 * Default constructor that establishes the communication with API
	 */
	public OpenWeatherMapApi() {
		final Properties properties = new Properties();

		try (final InputStream res = getClass().getClassLoader().getResourceAsStream("config.properties")) {
			properties.load(res);
		} catch (FileNotFoundException fileNotFoundException) {
			logger.error("Could not find the properties file", fileNotFoundException);
		} catch (Exception exception) {
			logger.error("Could not load properties file {}", exception.toString());
		}

		this.apiKey = ofNullable(System.getenv("GC_WEATHER_API_KEY"))
				.orElse(properties.getProperty("weather_api_key"));
		this.client = new OkHttpClient();
	}

	/**
	 * Provides weather for the current location for the current moment.
	 *
	 * @param location location at which weather is going to be retrieved
	 * @return {@link CurrentWeather} for the provided location
	 */
	public CurrentWeather getWeather(final Location location) {
		final String url = format(WEATHER_URL, location.getLatitude(), location.getLongitude(), apiKey);
		final Request request = new Request.Builder().url(url).build();

		try (final Response response = client.newCall(request).execute()) {
			return MAPPER.readValue(response.body().string(), CurrentWeather.class);
		} catch (final IOException | NullPointerException e) {
			logger.error("Network error fetching weather", e);
		}
		return null;
	}

	/**
	 * Get 5 day 3-hour forecast for the location.
	 *
	 * @param location location at which weather is going to be retrieved
	 * @return {@link Forecast} with a list of {@link CurrentWeather}
	 */
	public Forecast getForecast(final Location location) {
		final String url = format(FORECAST_URL, location.getLatitude(), location.getLongitude(), apiKey);
		final Request request = new Request.Builder().url(url).build();

		try (final Response response = client.newCall(request).execute()) {
			return MAPPER.readValue(response.body().string(), Forecast.class);
		} catch (IOException | NullPointerException e) {
			logger.error("Network error fetching weather", e);
		}

		return null;
	}
}
