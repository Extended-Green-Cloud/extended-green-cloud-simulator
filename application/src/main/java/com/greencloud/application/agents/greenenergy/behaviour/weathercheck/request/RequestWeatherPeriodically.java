package com.greencloud.application.agents.greenenergy.behaviour.weathercheck.request;

import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.request.logs.WeatherCheckRequestLog.PERIODIC_CHECK_SENT_LOG;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.PERIODIC_WEATHER_CHECK_PROTOCOL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener.ListenForWeatherData;
import com.greencloud.application.agents.greenenergy.domain.GreenEnergyAgentConstants;

import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.TickerBehaviour;

/**
 * Behaviour checks the current weather periodically and evaluates if the power drop has happened
 */
public class RequestWeatherPeriodically extends TickerBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(RequestWeatherPeriodically.class);

	private final GreenEnergyAgent myGreenEnergyAgent;

	/**
	 * Behaviour constructor
	 *
	 * @param agent agent executing the behaviour
	 */
	public RequestWeatherPeriodically(GreenEnergyAgent agent) {
		super(agent, GreenEnergyAgentConstants.PERIODIC_WEATHER_CHECK_TIMEOUT);
		this.myGreenEnergyAgent = agent;
	}

	/**
	 * Method initiates weather check
	 */
	@Override
	protected void onTick() {
		if (!myGreenEnergyAgent.getServerJobs().isEmpty()) {
			logger.info(PERIODIC_CHECK_SENT_LOG);
			final SequentialBehaviour sequentialBehaviour = new SequentialBehaviour();
			sequentialBehaviour.addSubBehaviour(
					new RequestWeatherData(myGreenEnergyAgent, PERIODIC_WEATHER_CHECK_PROTOCOL,
							PERIODIC_WEATHER_CHECK_PROTOCOL, null));
			sequentialBehaviour.addSubBehaviour(
					new ListenForWeatherData(myGreenEnergyAgent, null, PERIODIC_WEATHER_CHECK_PROTOCOL,
							PERIODIC_WEATHER_CHECK_PROTOCOL, sequentialBehaviour, null));
			myAgent.addBehaviour(sequentialBehaviour);
		}
	}
}
