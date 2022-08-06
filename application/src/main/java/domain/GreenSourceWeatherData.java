package domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import domain.location.Location;

import org.immutables.value.Value.Immutable;

/**
 * Object storing the data passed by the Green Source in the weather query message
 */
@JsonSerialize(as = ImmutableGreenSourceWeatherData.class)
@JsonDeserialize(as = ImmutableGreenSourceWeatherData.class)
@Immutable
public interface GreenSourceWeatherData {

	/**
	 * @return location for which the weather is to be retrieved
	 */
	Location getLocation();
}
