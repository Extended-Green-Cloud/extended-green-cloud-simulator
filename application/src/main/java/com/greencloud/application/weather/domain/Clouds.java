package com.greencloud.application.weather.domain;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableClouds.class)
@JsonDeserialize(as = ImmutableClouds.class)
@Immutable
public interface Clouds {

	Double getAll();
}
