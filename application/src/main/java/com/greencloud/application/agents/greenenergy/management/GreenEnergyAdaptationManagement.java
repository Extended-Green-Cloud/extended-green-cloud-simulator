package com.greencloud.application.agents.greenenergy.management;

import static com.greencloud.application.agents.greenenergy.management.logs.GreenEnergyManagementLog.ADAPTATION_INCREASE_ERROR_LOG;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.commons.managingsystem.planner.IncrementGreenSourceErrorParameters;

/**
 * Set of methods used to adapt the current configuration of Green Energy agent
 */
public class GreenEnergyAdaptationManagement {

	private static final Logger logger = LoggerFactory.getLogger(GreenEnergyStateManagement.class);

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
		final double currentError = greenEnergyAgent.getWeatherPredictionError();
		final double newError = currentError + params.getPercentageChange();

		logger.info(ADAPTATION_INCREASE_ERROR_LOG, currentError, newError);

		greenEnergyAgent.setWeatherPredictionError(newError);
		greenEnergyAgent.manage().updateGreenSourceGUI();
		return true;
	}
}
