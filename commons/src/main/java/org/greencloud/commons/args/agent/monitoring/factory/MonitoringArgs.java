package org.greencloud.commons.args.agent.monitoring.factory;

import static org.greencloud.commons.enums.weather.WeatherFetchingModeType.API;

import org.greencloud.commons.enums.weather.WeatherFetchingModeType;
import org.immutables.value.Value;
import org.jrba.agentmodel.domain.args.AgentArgs;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Arguments used to build Monitoring Agent
 */
@JsonSerialize(as = ImmutableMonitoringArgs.class)
@JsonDeserialize(as = ImmutableMonitoringArgs.class)
@Value.Immutable
public interface MonitoringArgs extends AgentArgs {

	/**
	 * @return optional bad stub probability
	 */
	@Value.Default
	default Double getBadStubProbability() {
		return 0.02;
	}

	/**
	 * @return optional mode of fetching weather data
	 */
	@Value.Default
	default WeatherFetchingModeType getWeatherFetchingMode() {
		return API;
	}

}
