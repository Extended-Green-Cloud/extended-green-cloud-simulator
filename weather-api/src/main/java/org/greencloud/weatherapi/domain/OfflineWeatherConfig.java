package org.greencloud.weatherapi.domain;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableOfflineWeatherConfig.class)
@JsonDeserialize(as = ImmutableOfflineWeatherConfig.class)
@Value.Immutable
public interface OfflineWeatherConfig {

	/**
	 * @return get set of configurations for local weather data
	 */
	List<LocalWeatherConfig> getLocalConfigs();
}
