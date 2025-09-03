package org.greencloud.commons.utils.energy;

import static java.lang.Math.min;
import static java.util.Objects.requireNonNull;
import static org.greencloud.commons.args.agent.greenenergy.agent.domain.GreenEnergyAgentPropsConstants.NOMINAL_OPERATING_CELL_TEMPERATURE;
import static org.greencloud.commons.args.agent.greenenergy.agent.domain.GreenEnergyAgentPropsConstants.POWER_TEMP_COEFFICIENT;
import static org.greencloud.commons.args.agent.greenenergy.agent.domain.GreenEnergyAgentPropsConstants.SOLAR_IRRADIANCE_RATED_AT_STC;
import static org.greencloud.commons.args.agent.greenenergy.agent.domain.GreenEnergyAgentPropsConstants.SOLAR_MODULE_COUNT_TOTAL;
import static org.greencloud.commons.args.agent.greenenergy.agent.domain.GreenEnergyAgentPropsConstants.SOLAR_MODULE_RATED_POWER_AT_STC;
import static org.greencloud.commons.args.agent.greenenergy.agent.domain.GreenEnergyAgentPropsConstants.SOLAR_TEMP_AT_STC;
import static org.greencloud.commons.args.agent.greenenergy.agent.domain.GreenEnergyAgentPropsConstants.TEST_MULTIPLIER;

import org.greencloud.commons.domain.weather.WeatherData;

/**
 * Class containing methods used to estimate available energy from renewable sources.
 */
public class EnergyEstimator {

	/**
	 * Method uses formula introduced in: <a href="https://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=5713847">
	 * Optimal Power Flow Management for Grid Connected PV Systems With Batteries</a>
	 * to estimate the amount of solar energy that can be produced by photovoltaic panels.
	 *
	 * @param weatherData current weather data
	 * @return estimated amount of energy
	 */
	public static double estimateSolarEnergyFromRadiation(final WeatherData weatherData) {
		final double relativeIrradiance = weatherData.getRadiation() / SOLAR_IRRADIANCE_RATED_AT_STC;
		final double cellTemperature = weatherData.getTemperature()
				+ ((NOMINAL_OPERATING_CELL_TEMPERATURE - 20) / 800) * weatherData.getRadiation();

		return SOLAR_MODULE_RATED_POWER_AT_STC *
				relativeIrradiance *
				(1 + POWER_TEMP_COEFFICIENT * (cellTemperature - SOLAR_TEMP_AT_STC)) *
				SOLAR_MODULE_COUNT_TOTAL;
	}

	/**
	 * Method estimates the amount of solar energy that can be produced based on the cloud cover.
	 *
	 * @param cloudCover               current cloud cover in percentage (0-100)
	 * @param maximumGeneratorCapacity maximum capacity of the solar generator
	 * @return estimated amount of energy
	 */
	public static double estimateSolarEnergyFromCloudCover(final double cloudCover,
			final double maximumGeneratorCapacity) {
		return maximumGeneratorCapacity * min(cloudCover / 100 + 0.1, 1) * TEST_MULTIPLIER;
	}
}
