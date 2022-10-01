package com.greencloud.application.weather.domain;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableCoord.class)
@JsonDeserialize(as = ImmutableCoord.class)
@Immutable
public interface Coord {

	Double getLon();

	Double getLat();
}
