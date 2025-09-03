package org.greencloud.weatherapi.domain;

import java.time.Instant;
import java.util.Map;

import org.immutables.value.Value;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableLocalWeatherDataTimeSeries.class)
@JsonDeserialize(as = ImmutableLocalWeatherDataTimeSeries.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value.Immutable
public interface LocalWeatherDataTimeSeries {

	Map<Instant, LocalWeatherData> getWeatherTimeSeries();

	@Nullable
	Integer getWeatherRelevanceMinutes();
}
