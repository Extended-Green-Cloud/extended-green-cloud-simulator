package org.greencloud.agentsystem.agents.monitoring.behaviour;

import static java.lang.Math.max;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.ListUtils.partition;
import static org.greencloud.agentsystem.agents.monitoring.behaviour.templates.WeatherServingMessageTemplates.SERVE_FORECAST_TEMPLATE;
import static org.greencloud.agentsystem.agents.monitoring.domain.MonitoringAgentConstants.MAX_NUMBER_OF_REQUESTS;
import static org.greencloud.agentsystem.agents.monitoring.domain.MonitoringAgentConstants.WEATHER_REQUESTS_IN_BATCH;
import static org.greencloud.commons.utils.logger.LogBuilder.constructLogBuilder;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.PERIODIC_WEATHER_CHECK_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.WeatherCheckMessageFactory.prepareWeatherDataResponse;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.greencloud.weatherapi.constants.MockWeatherConstants.CURRENT_WEATHER_BAD_STUB;
import static org.greencloud.weatherapi.mapper.WeatherMapper.mapToWeatherData;
import static org.jrba.utils.messages.MessageReader.readMessageContent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.greencloud.agentsystem.agents.monitoring.MonitoringAgent;
import org.greencloud.commons.domain.agent.GreenSourceForecastData;
import org.greencloud.commons.domain.agent.GreenSourceWeatherData;
import org.greencloud.commons.domain.weather.ImmutableMonitoringData;
import org.greencloud.commons.domain.weather.MonitoringData;
import org.greencloud.commons.utils.logger.LogBuilder;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour listens for the upcoming forecast requests.
 */
public class ServeForecastWeather extends CyclicBehaviour implements Serializable {

	private static final LogBuilder log = constructLogBuilder(ServeForecastWeather.class);
	private static final Random STUB_DATA_RANDOM = new Random();
	private final MonitoringAgent monitoringAgent;

	/**
	 * Behaviour constructor.
	 *
	 * @param monitoringAgent agent which is executing the behaviour
	 */
	public ServeForecastWeather(MonitoringAgent monitoringAgent) {
		this.monitoringAgent = monitoringAgent;
	}

	/**
	 * Method listens for the request for weather data coming from the Green Source Agents.
	 * It retrieves the forecast information for the given location and forwards it as a reply to the sender.
	 */
	@Override
	public void action() {
		final List<ACLMessage> messages = monitoringAgent.receive(SERVE_FORECAST_TEMPLATE, MAX_NUMBER_OF_REQUESTS);

		ofNullable(messages)
				.map(msgs -> partition(msgs, WEATHER_REQUESTS_IN_BATCH))
				.ifPresentOrElse(msgs -> msgs.stream().parallel().forEach(this::processMessages), this::block);
	}

	private void processMessages(final List<ACLMessage> messages) {
		messages.forEach(msg -> {
			final boolean isPeriodicCheck = msg.getConversationId().equals(PERIODIC_WEATHER_CHECK_PROTOCOL);
			final MonitoringData data = isPeriodicCheck ?
					getWeatherDataForPeriodicCheck(msg) :
					getWeatherForecast(msg);

			monitoringAgent.send(prepareWeatherDataResponse(data, msg));
		});
	}

	private MonitoringData getWeatherForecast(final ACLMessage message) {
		final GreenSourceForecastData requestData = readMessageContent(message, GreenSourceForecastData.class);

		log.withJobId(requestData.getJobId())
				.withAgentName(myAgent.getLocalName())
				.messageInfo("Sending message with the weather data for job {}", requestData.getJobId());

		return monitoringAgent.weather().getForecast(requestData);
	}

	private MonitoringData getWeatherDataForPeriodicCheck(final ACLMessage message) {
		final GreenSourceWeatherData requestData = readMessageContent(message, GreenSourceWeatherData.class);
		log.clearContext()
				.withAgentName(myAgent.getLocalName())
				.messageInfo("Sending message with the weather data");

		return of(monitoringAgent.getProperties().getConfig().getBadStubProbability())
				.map(probability -> max(probability - requestData.getPredictionError(), 0))
				.filter(this::isBadStubbingProbability)
				.map(_ -> mapToWeatherData(CURRENT_WEATHER_BAD_STUB, getCurrentTime()))
				.map(weatherData -> ImmutableMonitoringData.builder().addWeatherData(weatherData).build())
				.map(MonitoringData.class::cast)
				.orElse(monitoringAgent.weather().getWeather(requestData));
	}

	private boolean isBadStubbingProbability(final double probability) {
		return probability > (double) STUB_DATA_RANDOM.nextInt(100) / 100;
	}

}
