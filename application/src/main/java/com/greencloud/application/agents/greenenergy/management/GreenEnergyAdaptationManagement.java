package com.greencloud.application.agents.greenenergy.management;

import static com.greencloud.application.agents.greenenergy.domain.GreenEnergyAgentConstants.INITIAL_WEATHER_PREDICTION_ERROR;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.utils.AlgorithmUtils;
import com.greencloud.commons.managingsystem.planner.IncrementGreenSourceErrorParameters;

/**
 * Set of methods used to adapt the current configuration of Green Energy agent
 */
public class GreenEnergyAdaptationManagement {

	private final GreenEnergyAgent greenEnergyAgent;

	/**
	 * Default constructor
	 *
	 * @param greenEnergyAgent - agent representing given source
	 */
	public GreenEnergyAdaptationManagement(GreenEnergyAgent greenEnergyAgent) {
		this.greenEnergyAgent = greenEnergyAgent;
	}

	/**
	 * Method adapts the current weather prediction error of Green Energy Agent
	 *
	 * @param params adaptation parameters
	 */
	public boolean adaptAgentWeatherPredictionError(IncrementGreenSourceErrorParameters params) {
		final AtomicInteger newError = new AtomicInteger(
				(int) (greenEnergyAgent.getWeatherPredictionError() / INITIAL_WEATHER_PREDICTION_ERROR));

		IntStream.range(0, params.getPercentageChangeUnit())
				.forEach(i -> newError.getAndUpdate(AlgorithmUtils::nextFibonacci));

		greenEnergyAgent.setWeatherPredictionError((double) newError.get() * INITIAL_WEATHER_PREDICTION_ERROR);
		return true;
	}
}
