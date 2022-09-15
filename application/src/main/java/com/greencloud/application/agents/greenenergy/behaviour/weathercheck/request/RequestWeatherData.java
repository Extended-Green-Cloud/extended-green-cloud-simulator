package com.greencloud.application.agents.greenenergy.behaviour.weathercheck.request;

import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.request.logs.WeatherCheckRequestLog.WEATHER_REQUEST_SENT_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.utils.GUIUtils.displayMessageArrow;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.domain.ImmutableGreenSourceForecastData;
import com.greencloud.application.domain.ImmutableGreenSourceWeatherData;
import com.greencloud.application.domain.job.PowerJob;
import com.greencloud.application.messages.domain.factory.PowerCheckMessageFactory;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour responsible for requesting weather data from monitoring agent
 */
public class RequestWeatherData extends OneShotBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(RequestWeatherData.class);

	private final GreenEnergyAgent myGreenEnergyAgent;
	private final String protocol;
	private final String conversationId;
	private final PowerJob powerJob;

	/**
	 * Behaviour constructor.
	 *
	 * @param greenEnergyAgent agent which is executing the behaviour
	 * @param protocol         protocol of the message
	 * @param conversationId   conversation id of the message
	 * @param powerJob         (optional) job for which the weather is to be checked
	 */
	public RequestWeatherData(GreenEnergyAgent greenEnergyAgent, String protocol, String conversationId,
			PowerJob powerJob) {
		this.myGreenEnergyAgent = greenEnergyAgent;
		this.protocol = protocol;
		this.conversationId = conversationId;
		this.powerJob = powerJob;
	}

	/**
	 * Method which sends the request to the Monitoring Agent asking for the weather at the given location.
	 */
	@Override
	public void action() {
		if (nonNull(powerJob)) {
			MDC.put(MDC_JOB_ID, powerJob.getJobId());
		}
		logger.info(WEATHER_REQUEST_SENT_LOG);
		final ACLMessage request = PowerCheckMessageFactory.preparePowerCheckRequest(myGreenEnergyAgent,
				createMessageContent(), conversationId, protocol);
		displayMessageArrow(myGreenEnergyAgent, myGreenEnergyAgent.getMonitoringAgent());
		myAgent.send(request);
	}

	private Object createMessageContent() {
		return isNull(powerJob) ?
				ImmutableGreenSourceWeatherData.builder()
						.location(myGreenEnergyAgent.getLocation())
						.build() :
				ImmutableGreenSourceForecastData.builder()
						.location(myGreenEnergyAgent.getLocation())
						.timetable(myGreenEnergyAgent.manage().getJobsTimetable(powerJob))
						.build();
	}
}
