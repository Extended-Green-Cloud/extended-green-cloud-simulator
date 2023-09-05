package com.greencloud.application.agents.greenenergy.behaviour.weathercheck.request;

import static com.database.knowledge.domain.agent.DataType.AVAILABLE_GREEN_ENERGY;
import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.request.RequestWeatherData.createWeatherRequest;
import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.request.logs.WeatherCheckRequestLog.NO_POWER_DROP_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.request.logs.WeatherCheckRequestLog.PERIODIC_CHECK_SENT_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.request.logs.WeatherCheckRequestLog.POWER_DROP_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.request.logs.WeatherCheckRequestLog.WEATHER_UNAVAILABLE_LOG;
import static com.greencloud.application.agents.greenenergy.constants.GreenEnergyAgentConstants.PERIODIC_WEATHER_CHECK_TIMEOUT;
import static com.greencloud.application.messages.constants.MessageProtocolConstants.PERIODIC_WEATHER_CHECK_PROTOCOL;
import static com.greencloud.application.utils.TimeUtils.convertToRealTime;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.commons.args.event.powershortage.PowerShortageCause.WEATHER_CAUSE;
import static java.util.Objects.nonNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.Serializable;
import java.time.Instant;
import java.util.function.BiConsumer;

import org.slf4j.Logger;

import com.database.knowledge.domain.agent.greensource.AvailableGreenEnergy;
import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.AnnounceSourcePowerShortage;
import com.greencloud.application.domain.weather.MonitoringData;
import com.gui.agents.GreenEnergyAgentNode;

import jade.core.behaviours.TickerBehaviour;

/**
 * Behaviour checks the current weather periodically and evaluates if the power drop has happened
 */
public class RequestWeatherPeriodically extends TickerBehaviour implements Serializable {

	private static final Logger logger = getLogger(RequestWeatherPeriodically.class);

	private final GreenEnergyAgent myGreenEnergyAgent;

	/**
	 * Behaviour constructor
	 *
	 * @param agent agent executing the behaviour
	 */
	public RequestWeatherPeriodically(GreenEnergyAgent agent) {
		super(agent, PERIODIC_WEATHER_CHECK_TIMEOUT);
		this.myGreenEnergyAgent = agent;
	}

	/**
	 * Method initiates weather check
	 */
	@Override
	protected void onTick() {
		logger.info(PERIODIC_CHECK_SENT_LOG);
		myAgent.addBehaviour(createWeatherRequest(myGreenEnergyAgent, PERIODIC_WEATHER_CHECK_PROTOCOL,
				getResponseHandler(), getResponseRefuseHandler()));
	}

	private BiConsumer<MonitoringData, Exception> getResponseHandler() {
		return (data, error) -> {
			if (nonNull(error)) {
				logger.info(WEATHER_UNAVAILABLE_LOG, getCurrentTime());
				return;
			}
			final Instant time = getCurrentTime();
			final Instant realTime = convertToRealTime(time);
			final double availablePower = myGreenEnergyAgent.power().getAvailableEnergy(realTime, data).orElse(-1.0);

			if (availablePower < 0 && !myGreenEnergyAgent.getServerJobs().isEmpty()) {
				logger.info(POWER_DROP_LOG, time);
				myAgent.addBehaviour(new AnnounceSourcePowerShortage(myGreenEnergyAgent, null,
						time, availablePower, WEATHER_CAUSE));
			} else {
				logger.info(NO_POWER_DROP_LOG, time);
			}
			reportAvailableEnergyData(myGreenEnergyAgent.power().getAvailableGreenEnergy(data, time));
		};
	}

	private Runnable getResponseRefuseHandler() {
		return (Runnable & Serializable) () -> logger.info(WEATHER_UNAVAILABLE_LOG, getCurrentTime());
	}

	private void reportAvailableEnergyData(final double availableEnergy) {
		final double energyPercentage = myGreenEnergyAgent.power().getEnergyPercentage(availableEnergy);
		myGreenEnergyAgent.writeMonitoringData(AVAILABLE_GREEN_ENERGY, new AvailableGreenEnergy(energyPercentage));

		if (nonNull(myGreenEnergyAgent.getAgentNode())) {
			((GreenEnergyAgentNode) myGreenEnergyAgent.getAgentNode()).updateGreenEnergyAmount(availableEnergy);
		}
	}
}
