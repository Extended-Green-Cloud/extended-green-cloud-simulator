package com.greencloud.commons.location;

import java.io.Serializable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Object storing the location details
 */
@JsonDeserialize(as = ImmutableLocation.class)
@JsonSerialize(as = ImmutableLocation.class)
@Value.Immutable
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
