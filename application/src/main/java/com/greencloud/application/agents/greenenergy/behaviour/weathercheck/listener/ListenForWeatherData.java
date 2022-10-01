package com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener;

import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener.logs.WeatherCheckListenerLog.CHANGE_JOB_STATUS_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener.logs.WeatherCheckListenerLog.NO_POWER_DROP_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener.logs.WeatherCheckListenerLog.NO_POWER_LEAVE_ON_HOLD_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener.logs.WeatherCheckListenerLog.POWER_DROP_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener.logs.WeatherCheckListenerLog.WEATHER_UNAVAILABLE_JOB_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener.logs.WeatherCheckListenerLog.WEATHER_UNAVAILABLE_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.domain.job.JobStatusEnum.ACCEPTED;
import static com.greencloud.application.domain.job.JobStatusEnum.ACTIVE_JOB_STATUSES;
import static com.greencloud.application.domain.job.JobStatusEnum.IN_PROGRESS;
import static com.greencloud.application.domain.powershortage.PowerShortageCause.WEATHER_CAUSE;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.ON_HOLD_JOB_CHECK_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.PERIODIC_WEATHER_CHECK_PROTOCOL;
import static com.greencloud.application.messages.domain.factory.PowerShortageMessageFactory.preparePowerShortageFinishInformation;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REFUSE;
import static jade.lang.acl.MessageTemplate.MatchConversationId;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.MatchSender;
import static jade.lang.acl.MessageTemplate.and;
import static jade.lang.acl.MessageTemplate.or;
import static java.util.Objects.nonNull;

import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.AnnounceSourcePowerShortage;
import com.greencloud.application.domain.MonitoringData;
import com.greencloud.application.domain.job.JobStatusEnum;
import com.greencloud.application.domain.job.PowerJob;
import com.greencloud.application.messages.MessagingUtils;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Behaviour listens for response from Monitoring Agent containing com.greencloud.application.weather data
 */
public class ListenForWeatherData extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForWeatherData.class);

	private final MessageTemplate messageTemplate;
	private final GreenEnergyAgent myGreenEnergyAgent;
	private final PowerJob powerJob;
	private final SequentialBehaviour parentBehaviour;
	private final String protocol;

	/**
	 * Behaviour constructor
	 *
	 * @param myGreenAgent    agent which is executing the behaviour
	 * @param powerJob        (optional) job of interest
	 * @param protocol        message protocol
	 * @param conversationId  message conversation id
	 * @param parentBehaviour behaviour which should be removed
	 */
	public ListenForWeatherData(GreenEnergyAgent myGreenAgent, PowerJob powerJob, String protocol,
			String conversationId, SequentialBehaviour parentBehaviour) {
		this.messageTemplate = and(and(MatchProtocol(protocol), MatchSender(myGreenAgent.getMonitoringAgent())),
				and(or(MatchPerformative(INFORM), MatchPerformative(REFUSE)), MatchConversationId(conversationId)));
		this.myGreenEnergyAgent = myGreenAgent;
		this.powerJob = powerJob;
		this.parentBehaviour = parentBehaviour;
		this.protocol = protocol;
	}

	/**
	 * Method listens for the Monitoring Agent reply.
	 * It processes the received com.greencloud.application.weather information and handles the response using method assign to given protocol
	 */
	@Override
	public void action() {
		final ACLMessage message = myAgent.receive(messageTemplate);

		if (nonNull(message)) {
			final MonitoringData data = MessagingUtils.readMessageContent(message, MonitoringData.class);

			if (nonNull(data)) {
				if (nonNull(powerJob)) {
					MDC.put(MDC_JOB_ID, powerJob.getJobId());
				}
				switch (message.getPerformative()) {
					case REFUSE -> handleRefuse();
					case INFORM -> handleInform(data);
				}
			}
			myAgent.removeBehaviour(parentBehaviour);
		} else {
			block();
		}
	}

	private void handleInform(final MonitoringData data) {
		switch (protocol) {
			case ON_HOLD_JOB_CHECK_PROTOCOL -> handleWeatherDataForJobOnHold(data);
			case PERIODIC_WEATHER_CHECK_PROTOCOL -> handleWeatherDataForPeriodicCheck(data);
		}
	}

	private void handleRefuse() {
		switch (protocol) {
			case ON_HOLD_JOB_CHECK_PROTOCOL -> logger.info(WEATHER_UNAVAILABLE_JOB_LOG, powerJob.getJobId());
			case PERIODIC_WEATHER_CHECK_PROTOCOL -> logger.info(WEATHER_UNAVAILABLE_LOG, getCurrentTime());
		}
	}

	private void handleWeatherDataForJobOnHold(final MonitoringData data) {
		final Optional<Double> availablePower = myGreenEnergyAgent.manage()
				.getAvailablePowerForJob(powerJob, data, false);

		if (availablePower.isEmpty()) {
			logger.info(NO_POWER_LEAVE_ON_HOLD_LOG, powerJob.getJobId());
		} else {
			logger.info(CHANGE_JOB_STATUS_LOG, powerJob.getJobId());
			final JobStatusEnum newStatus = powerJob.getStartTime().isAfter(getCurrentTime()) ? ACCEPTED : IN_PROGRESS;

			myGreenEnergyAgent.getPowerJobs().replace(powerJob, newStatus);
			myGreenEnergyAgent.manage().updateGreenSourceGUI();
			myGreenEnergyAgent.send(preparePowerShortageFinishInformation(mapToJobInstanceId(powerJob),
					myGreenEnergyAgent.getOwnerServer()));
		}
	}

	private void handleWeatherDataForPeriodicCheck(final MonitoringData data) {
		final Instant time = getCurrentTime();
		final double availablePower = myGreenEnergyAgent.manage().getAvailablePower(time, data).orElse(0.0);

		if (availablePower < myGreenEnergyAgent.manage().getCurrentPowerInUseForGreenSource()) {
			logger.info(POWER_DROP_LOG, time);
			myAgent.addBehaviour(new AnnounceSourcePowerShortage(myGreenEnergyAgent, null, time, availablePower,
					WEATHER_CAUSE));
		} else {
			logger.info(NO_POWER_DROP_LOG, time);
		}
	}
}
