package com.greencloud.application.agents.greenenergy.behaviour.weathercheck.request;

import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.request.logs.WeatherCheckRequestLog.WEATHER_REQUEST_SENT_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.messages.domain.factory.PowerCheckMessageFactory.preparePowerCheckRequest;
import static com.greencloud.application.utils.TimeUtils.convertToRealTime;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.ACCEPTED_JOB_STATUSES;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.google.common.annotations.VisibleForTesting;
import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.domain.ImmutableGreenSourceForecastData;
import com.greencloud.application.domain.ImmutableGreenSourceWeatherData;
import com.greencloud.commons.job.ExecutionJobStatusEnum;
import com.greencloud.commons.job.ServerJob;

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
	private final ServerJob serverJob;

	/**
	 * Behaviour constructor.
	 *
	 * @param greenEnergyAgent agent which is executing the behaviour
	 * @param protocol         protocol of the message
	 * @param conversationId   conversation id of the message
	 * @param serverJob        (optional) job for which the weather is to be checked
	 */
	public RequestWeatherData(GreenEnergyAgent greenEnergyAgent, String protocol, String conversationId,
			ServerJob serverJob) {
		this.myGreenEnergyAgent = greenEnergyAgent;
		this.protocol = protocol;
		this.conversationId = conversationId;
		this.serverJob = serverJob;
	}

	@VisibleForTesting
	protected static List<Instant> getJobsTimetable(final ServerJob candidateJob,
			final Map<ServerJob, ExecutionJobStatusEnum> jobMap) {
		var validJobs = jobMap.entrySet().stream()
				.filter(entry -> ACCEPTED_JOB_STATUSES.contains(entry.getValue()))
				.map(Map.Entry::getKey)
				.toList();
		return Stream.concat(
						Stream.of(
								convertToRealTime(candidateJob.getStartTime()),
								convertToRealTime(candidateJob.getEndTime())),
						Stream.concat(
								validJobs.stream().map(job -> convertToRealTime(job.getStartTime())),
								validJobs.stream().map(job -> convertToRealTime(job.getEndTime()))))
				.distinct()
				.toList();
	}

	/**
	 * Method which sends the request to the Monitoring Agent asking for the weather at the given location.
	 */
	@Override
	public void action() {
		if (nonNull(serverJob)) {
			MDC.put(MDC_JOB_ID, serverJob.getJobId());
		}
		logger.info(WEATHER_REQUEST_SENT_LOG);
		final ACLMessage request = preparePowerCheckRequest(myGreenEnergyAgent,
				createMessageContent(), conversationId, protocol);
		myAgent.send(request);
	}

	private Object createMessageContent() {
		return isNull(serverJob) ?
				ImmutableGreenSourceWeatherData.builder()
						.location(myGreenEnergyAgent.getLocation())
						.predictionError(myGreenEnergyAgent.getWeatherPredictionError())
						.build() :
				ImmutableGreenSourceForecastData.builder()
						.location(myGreenEnergyAgent.getLocation())
						.timetable(getJobsTimetable(serverJob, myGreenEnergyAgent.getServerJobs()))
						.build();
	}
}
