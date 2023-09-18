package org.greencloud.commons.domain.location;

import java.io.Serializable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.greencloud.commons.domain.ImmutableConfig;

/**
 * Object storing the location details
 */
@JsonSerialize(as = ImmutableLocation.class)
@JsonDeserialize(as = ImmutableLocation.class)
@Value.Immutable
@ImmutableConfig
public interface Location extends Serializable {

	/**
	 * @return location latitude
	 */
	double getLatitude();

	/**
	 * @return location longitude
	 */
	double getLongitude();
}
