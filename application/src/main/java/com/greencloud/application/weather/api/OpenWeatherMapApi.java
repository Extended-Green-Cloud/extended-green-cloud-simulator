package com.greencloud.application.weather.api;

import static java.lang.String.format;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greencloud.application.domain.location.Location;
import com.greencloud.application.mapper.JsonMapper;
import com.greencloud.application.weather.domain.CurrentWeather;
import com.greencloud.application.weather.domain.Forecast;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OpenWeatherMapApi {

	private static final Logger logger = LoggerFactory.getLogger(OpenWeatherMapApi.class);

	private static final String WEATHER_URL =
			"https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s&units=metric";
	private static final String FORECAST_URL =
			"https://api.openweathermap.org/data/2.5/forecast?lat=%s&lon=%s&appid=%s&units=metric";
	private static final ObjectMapper MAPPER = JsonMapper.getMapper();

	private final String apiKey;
	private final OkHttpClient client;

	public OpenWeatherMapApi() {
		Properties properties = new Properties();
		URL res = getClass().getClassLoader().getResource("config.properties");

		try (FileInputStream fileInputStream = new FileInputStream(Paths.get(res.toURI()).toFile())) {
			properties.load(fileInputStream);
		} catch (FileNotFoundException fileNotFoundException) {
			logger.error("Could not find the properties file", fileNotFoundException);
		} catch (Exception exception) {
			logger.error("Could not load properties file {}", exception.toString());
		}

		this.apiKey = properties.getProperty("weather_api_key");
		this.client = new OkHttpClient();
	}

	/**
	 * Provides com.greencloud.application.weather for the current location for the current moment.
	 *
	 * @param location to request com.greencloud.application.weather for
	 * @return {@link CurrentWeather} for the provided location
	 */
	public CurrentWeather getWeather(Location location) {
		var url = format(WEATHER_URL, location.getLatitude(), location.getLongitude(), apiKey);
		Request request = new Request.Builder().url(url).build();

		try (Response response = client.newCall(request).execute()) {
			return MAPPER.readValue(response.body().string(), CurrentWeather.class);
		} catch (IOException | NullPointerException e) {
			logger.error("Network error fetching com.greencloud.application.weather", e);
		}

		return null;
	}

	/**
	 * Get 5 day 3-hour forecast for the location.
	 *
	 * @param location to request com.greencloud.application.weather for
	 * @return {@link Forecast} with a list of {@link CurrentWeather}
	 */
	public Forecast getForecast(Location location) {
		var url = format(FORECAST_URL, location.getLatitude(), location.getLongitude(), apiKey);
		Request request = new Request.Builder().url(url).build();

		try (Response response = client.newCall(request).execute()) {
			return MAPPER.readValue(response.body().string(), Forecast.class);
		} catch (IOException | NullPointerException e) {
			logger.error("Network error fetching com.greencloud.application.weather", e);
		}

		return null;
	}
}
