package com.greencloud.commons.managingsystem.planner;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jade.core.AID;

/**
 * Content of the message sent when the adaptation plan which connects additional Green Source to the Server is
 * executed
 */
@Value.Immutable
@JsonDeserialize(as = ImmutableConnectGreenSourceParameters.class)
@JsonSerialize(as = ImmutableConnectGreenSourceParameters.class)
public interface ConnectGreenSourceParameters extends AdaptationActionParameters {

	/**
	 * @return name of a Server to which the Green Source will be connected
	 */
	String getServerName();

	@Override
	default boolean dependsOnOtherAgents() {
		return true;
	}
}
