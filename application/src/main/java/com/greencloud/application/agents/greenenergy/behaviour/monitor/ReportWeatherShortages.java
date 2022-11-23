package com.greencloud.application.agents.greenenergy.behaviour.monitor;

import static com.database.knowledge.domain.agent.DataType.WEATHER_SHORTAGES;

import com.database.knowledge.domain.agent.greensource.WeatherShortages;
import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

/**
 * Ticker behaviour responsible for periodical reporting of weather shortages
 */
public class ReportWeatherShortages extends TickerBehaviour {

	/**
	 * Defines how often weather shortage happens on the given agent in given window of time [ms].
	 */
	private static final long REPORT_SHORTAGE_PERIOD = 500;

	private final GreenEnergyAgent myGreenEnergyAgent;

	public ReportWeatherShortages(Agent a) {
		super(a, REPORT_SHORTAGE_PERIOD);
		myGreenEnergyAgent = (GreenEnergyAgent) a;
	}

	@Override
	protected void onTick() {
		Integer shortages = myGreenEnergyAgent.manage().getWeatherShortagesCounter().getAndSet(0);
		WeatherShortages weatherShortages = new WeatherShortages(shortages, REPORT_SHORTAGE_PERIOD);
		myGreenEnergyAgent.writeMonitoringData(WEATHER_SHORTAGES, weatherShortages);
	}
}
