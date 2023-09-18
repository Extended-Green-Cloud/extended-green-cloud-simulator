package org.greencloud.weatherapi.domain;

import org.immutables.value.Value.Immutable;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableWind.class)
@JsonDeserialize(as = ImmutableWind.class)
@JsonInclude(Include.NON_NULL)
@Immutable
public interface Wind {

	Double getSpeed();

	Double getDeg();

	@Nullable
	Double getGust();
}
