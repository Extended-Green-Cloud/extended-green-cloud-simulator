package org.greencloud.agentsystem.agents.monitoring.behaviour;

import static java.lang.Math.max;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.ListUtils.partition;
import static org.greencloud.agentsystem.agents.monitoring.behaviour.logs.WeatherServingLog.SERVE_FORECAST_FOR_JOB_LOG;
import static org.greencloud.agentsystem.agents.monitoring.behaviour.logs.WeatherServingLog.SERVE_FORECAST_LOG;
import static org.greencloud.agentsystem.agents.monitoring.behaviour.templates.WeatherServingMessageTemplates.SERVE_FORECAST_TEMPLATE;
import static org.greencloud.agentsystem.agents.monitoring.domain.MonitoringAgentConstants.BAD_STUB_DATA;
import static org.greencloud.agentsystem.agents.monitoring.domain.MonitoringAgentConstants.MAX_NUMBER_OF_REQUESTS;
import static org.greencloud.agentsystem.agents.monitoring.domain.MonitoringAgentConstants.STUB_DATA;
import static org.greencloud.agentsystem.agents.monitoring.domain.MonitoringAgentConstants.WEATHER_REQUESTS_IN_BATCH;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.PERIODIC_WEATHER_CHECK_PROTOCOL;
import static org.greencloud.commons.utils.messaging.factory.WeatherCheckMessageFactory.prepareWeatherDataResponse;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_AGENT_NAME;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.utils.messages.MessageReader.readMessageContent;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

import org.greencloud.agentsystem.agents.monitoring.MonitoringAgent;
import org.greencloud.commons.domain.agent.GreenSourceForecastData;
import org.greencloud.commons.domain.agent.GreenSourceWeatherData;
import org.greencloud.commons.domain.weather.MonitoringData;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour listens for the upcoming forecast requests.
 */
public class ServeForecastWeather extends CyclicBehaviour implements Serializable {

	private static final Logger logger = getLogger(ServeForecastWeather.class);
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

		MDC.put(MDC_AGENT_NAME, myAgent.getLocalName());
		MDC.put(MDC_JOB_ID, requestData.getJobId());
		logger.info(SERVE_FORECAST_FOR_JOB_LOG, requestData.getJobId());

		return monitoringAgent.getProperties().isOfflineMode() ?
				STUB_DATA :
				monitoringAgent.weather().getForecast(requestData);
	}

	private MonitoringData getWeatherDataForPeriodicCheck(final ACLMessage message) {
		final GreenSourceWeatherData requestData = readMessageContent(message, GreenSourceWeatherData.class);
		MDC.clear();
		MDC.put(MDC_AGENT_NAME, myAgent.getLocalName());
		logger.info(SERVE_FORECAST_LOG);

		return of(monitoringAgent.getProperties().getBadStubProbability())
				.map(probability -> max(probability - requestData.getPredictionError(), 0))
				.filter(this::isBadStubbingProbability)
				.map(probability -> BAD_STUB_DATA)
				.orElse(monitoringAgent.getProperties().isOfflineMode() ?
						STUB_DATA :
						monitoringAgent.weather().getWeather(requestData));
	}

	private boolean isBadStubbingProbability(final double probability) {
		return probability > (double) STUB_DATA_RANDOM.nextInt(100) / 100;
	}

}
