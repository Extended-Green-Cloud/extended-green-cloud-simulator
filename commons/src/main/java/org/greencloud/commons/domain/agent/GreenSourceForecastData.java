package org.greencloud.commons.domain.agent;

import java.time.Instant;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.domain.ImmutableConfig;
import org.greencloud.commons.domain.location.Location;

/**
 * Object storing the data passed by the Green Source in the forecast request message
 */
@JsonSerialize(as = ImmutableGreenSourceForecastData.class)
@JsonDeserialize(as = ImmutableGreenSourceForecastData.class)
@Value.Immutable
@ImmutableConfig
public interface GreenSourceForecastData {

	/**
	 * @return location for which the forecast is to be retrieved
	 */
	Location getLocation();

	/**
	 * @return timetable for which com.greencloud.application.weather is requested
	 */
	List<Instant> getTimetable();

	/**
	 * @return job identifier (used mostly for debugging purposes)
	 */
	String getJobId();
}
