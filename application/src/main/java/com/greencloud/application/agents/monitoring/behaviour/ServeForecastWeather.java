package com.greencloud.application.agents.monitoring.behaviour;

import static com.greencloud.application.agents.monitoring.behaviour.logs.WeatherServingLog.SERVE_FORECAST_LOG;
import static com.greencloud.application.agents.monitoring.behaviour.templates.WeatherServingMessageTemplates.SERVE_FORECAST_TEMPLATE;
import static com.greencloud.application.agents.monitoring.domain.MonitoringAgentConstants.BAD_STUB_DATA;
import static com.greencloud.application.agents.monitoring.domain.MonitoringAgentConstants.BAD_STUB_PROBABILITY;
import static com.greencloud.application.agents.monitoring.domain.MonitoringAgentConstants.OFFLINE_MODE;
import static com.greencloud.application.agents.monitoring.domain.MonitoringAgentConstants.STUB_DATA;
import static com.greencloud.application.messages.MessagingUtils.readMessageContent;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.PERIODIC_WEATHER_CHECK_PROTOCOL;
import static com.greencloud.application.utils.GUIUtils.displayMessageArrow;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REFUSE;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.greencloud.application.agents.monitoring.MonitoringAgent;
import com.greencloud.application.domain.GreenSourceForecastData;
import com.greencloud.application.domain.MonitoringData;
import com.greencloud.application.mapper.JsonMapper;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour listens for the upcoming forecast requests
 */
public class ServeForecastWeather extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ServeForecastWeather.class);
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
		final ACLMessage message = monitoringAgent.receive(SERVE_FORECAST_TEMPLATE);

		if (Objects.nonNull(message)) {
			logger.info(SERVE_FORECAST_LOG);
			final GreenSourceForecastData requestData = readMessageContent(message, GreenSourceForecastData.class);
			final ACLMessage response = prepareWeatherResponse(message, requestData);

			displayMessageArrow(monitoringAgent, message.getSender());
			monitoringAgent.send(response);
		} else {
			block();
		}
	}

	private ACLMessage prepareWeatherResponse(final ACLMessage message, final GreenSourceForecastData requestData) {
		final boolean isPeriodicCheck = message.getConversationId().equals(PERIODIC_WEATHER_CHECK_PROTOCOL);
		final ACLMessage response = message.createReply();
		response.setPerformative(INFORM);
		try {
			if (isPeriodicCheck && (double) STUB_DATA_RANDOM.nextInt(100) / 100 < BAD_STUB_PROBABILITY) {
				response.setContent(JsonMapper.getMapper().writeValueAsString(BAD_STUB_DATA));
			} else if (OFFLINE_MODE) {
				response.setContent(JsonMapper.getMapper().writeValueAsString(STUB_DATA));
			} else {
				response.setContent(JsonMapper.getMapper().writeValueAsString(useApi(requestData)));
			}
		} catch (IOException e) {
			e.printStackTrace();
			response.setPerformative(REFUSE);
		}
		response.setConversationId(message.getConversationId());
		return response;
	}

	private MonitoringData useApi(GreenSourceForecastData requestData) {
		return monitoringAgent.manageWeather().getForecast(requestData);
	}
}
