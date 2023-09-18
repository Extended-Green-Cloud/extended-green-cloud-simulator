package org.greencloud.weatherapi.domain;

import java.time.Instant;
import java.util.List;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableCurrentWeather.class)
@JsonDeserialize(as = ImmutableCurrentWeather.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@Immutable
public interface CurrentWeather extends AbstractWeather {

	Coord getCoord();

	List<Weather> getWeather();

	Double getVisibility();

	Long getTimezone();

	/**
	 * @return {@link Instant} timestamp for when the weather is valid
	 */
	@JsonProperty("dt")
	@JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "s")
	Instant getTimestamp();
}
