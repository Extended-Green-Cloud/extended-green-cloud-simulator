package com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener;

import static com.database.knowledge.domain.agent.DataType.AVAILABLE_GREEN_ENERGY;
import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener.logs.WeatherCheckListenerLog.CHANGE_JOB_STATUS_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener.logs.WeatherCheckListenerLog.NO_POWER_DROP_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener.logs.WeatherCheckListenerLog.NO_POWER_LEAVE_ON_HOLD_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener.logs.WeatherCheckListenerLog.POWER_DROP_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener.logs.WeatherCheckListenerLog.RE_SUPPLY_FAILURE_JOB_NOT_FOUND_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener.logs.WeatherCheckListenerLog.RE_SUPPLY_FAILURE_NO_POWER_JOB_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener.logs.WeatherCheckListenerLog.RE_SUPPLY_JOB_WITH_GREEN_ENERGY_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener.logs.WeatherCheckListenerLog.WEATHER_UNAVAILABLE_JOB_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener.logs.WeatherCheckListenerLog.WEATHER_UNAVAILABLE_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener.logs.WeatherCheckListenerLog.WEATHER_UNAVAILABLE_RE_SUPPLY_JOB_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.mapper.JobMapper.mapToJobInstanceId;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.ON_HOLD_JOB_CHECK_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.PERIODIC_WEATHER_CHECK_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.POWER_SHORTAGE_FINISH_ALERT_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.MessageProtocolConstants.SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL;
import static com.greencloud.application.messages.domain.constants.PowerShortageMessageContentConstants.JOB_NOT_FOUND_CAUSE_MESSAGE;
import static com.greencloud.application.messages.domain.constants.PowerShortageMessageContentConstants.NOT_ENOUGH_GREEN_POWER_CAUSE_MESSAGE;
import static com.greencloud.application.messages.domain.constants.PowerShortageMessageContentConstants.RE_SUPPLY_SUCCESSFUL_MESSAGE;
import static com.greencloud.application.messages.domain.constants.PowerShortageMessageContentConstants.WEATHER_UNAVAILABLE_CAUSE_MESSAGE;
import static com.greencloud.application.messages.domain.factory.PowerShortageMessageFactory.prepareJobPowerShortageInformation;
import static com.greencloud.application.messages.domain.factory.ReplyMessageFactory.prepareReply;
import static com.greencloud.application.utils.JobUtils.isJobStarted;
import static com.greencloud.application.utils.TimeUtils.convertToRealTime;
import static com.greencloud.application.utils.TimeUtils.getCurrentTime;
import static com.greencloud.commons.args.event.powershortage.PowerShortageCause.WEATHER_CAUSE;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.ACCEPTED;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.IN_PROGRESS;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.ON_HOLD;
import static com.greencloud.commons.job.ExecutionJobStatusEnum.ON_HOLD_PLANNED;
import static jade.lang.acl.ACLMessage.FAILURE;
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

import com.database.knowledge.domain.agent.greensource.AvailableGreenEnergy;
import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.greenenergy.behaviour.powershortage.announcer.AnnounceSourcePowerShortage;
import com.greencloud.application.domain.MonitoringData;
import com.greencloud.application.messages.MessagingUtils;
import com.greencloud.commons.job.ExecutionJobStatusEnum;
import com.greencloud.commons.job.ServerJob;

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
	private final ServerJob serverJob;
	private final SequentialBehaviour parentBehaviour;
	private final String protocol;
	private final ACLMessage reply;

	/**
	 * Behaviour constructor
	 *
	 * @param myGreenAgent    agent which is executing the behaviour
	 * @param serverJob       (optional) job of interest
	 * @param protocol        message protocol
	 * @param conversationId  message conversation id
	 * @param parentBehaviour behaviour which should be removed
	 * @param reply           (optional) reply message sent upon received weather
	 */
	public ListenForWeatherData(GreenEnergyAgent myGreenAgent, ServerJob serverJob, String protocol,
			String conversationId, SequentialBehaviour parentBehaviour, ACLMessage reply) {
		this.messageTemplate = and(and(MatchProtocol(protocol), MatchSender(myGreenAgent.getMonitoringAgent())),
				and(or(MatchPerformative(INFORM), MatchPerformative(REFUSE)), MatchConversationId(conversationId)));
		this.myGreenEnergyAgent = myGreenAgent;
		this.serverJob = serverJob;
		this.parentBehaviour = parentBehaviour;
		this.protocol = protocol;
		this.reply = reply;
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
				if (nonNull(serverJob)) {
					MDC.put(MDC_JOB_ID, serverJob.getJobId());
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
			case SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL -> handleWeatherDataForReSupply(data);
		}
	}

	private void handleRefuse() {
		switch (protocol) {
			case ON_HOLD_JOB_CHECK_PROTOCOL -> logger.info(WEATHER_UNAVAILABLE_JOB_LOG, serverJob.getJobId());
			case PERIODIC_WEATHER_CHECK_PROTOCOL -> logger.info(WEATHER_UNAVAILABLE_LOG, getCurrentTime());
			case SERVER_POWER_SHORTAGE_RE_SUPPLY_PROTOCOL -> {
				logger.info(WEATHER_UNAVAILABLE_RE_SUPPLY_JOB_LOG, serverJob.getJobId());
				myGreenEnergyAgent.send(prepareReply(reply, WEATHER_UNAVAILABLE_CAUSE_MESSAGE, FAILURE));
			}
		}
	}

	private void handleWeatherDataForJobOnHold(final MonitoringData data) {
		final Optional<Double> availablePower = myGreenEnergyAgent.manage()
				.getAvailablePowerForJob(serverJob, data, false);

		if (availablePower.isEmpty() || serverJob.getPower() > availablePower.get()) {
			logger.info(NO_POWER_LEAVE_ON_HOLD_LOG, serverJob.getJobId());
		} else {
			logger.info(CHANGE_JOB_STATUS_LOG, serverJob.getJobId());
			final ExecutionJobStatusEnum newStatus =
					isJobStarted(serverJob, myGreenEnergyAgent.getServerJobs()) ? IN_PROGRESS : ACCEPTED;

			myGreenEnergyAgent.getServerJobs().replace(serverJob, newStatus);
			myGreenEnergyAgent.manage().updateGreenSourceGUI();
			myGreenEnergyAgent.send(prepareJobPowerShortageInformation(mapToJobInstanceId(serverJob),
					serverJob.getServer(), POWER_SHORTAGE_FINISH_ALERT_PROTOCOL));
		}
	}

	private void handleWeatherDataForPeriodicCheck(final MonitoringData data) {
		final Instant time = getCurrentTime();
		final double availablePower = myGreenEnergyAgent.manage().getAvailablePower(convertToRealTime(time), data)
				.orElse(-1.0);

		if (availablePower < 0 && !myGreenEnergyAgent.getServerJobs().isEmpty()) {
			logger.info(POWER_DROP_LOG, time);
			myAgent.addBehaviour(new AnnounceSourcePowerShortage(myGreenEnergyAgent, null, time, availablePower,
					WEATHER_CAUSE));
			myGreenEnergyAgent.manage().getWeatherShortagesCounter().getAndIncrement();
		} else {
			logger.info(NO_POWER_DROP_LOG, time);
		}
		reportAvailableEnergyData(myGreenEnergyAgent.manageGreenPower().getAvailablePower(data, time));
	}

	private void reportAvailableEnergyData(final double availablePower) {
		final double currentMaximumCapacity = myGreenEnergyAgent.manageGreenPower().getCurrentMaximumCapacity();
		final double availableGreenEnergyPercentage =
				currentMaximumCapacity == 0 ? 0 : availablePower / currentMaximumCapacity;
		final AvailableGreenEnergy greenEnergy = new AvailableGreenEnergy(availableGreenEnergyPercentage);
		myGreenEnergyAgent.writeMonitoringData(AVAILABLE_GREEN_ENERGY, greenEnergy);
	}

	private void handleWeatherDataForReSupply(final MonitoringData data) {
		final Optional<Double> availablePower = myGreenEnergyAgent.manage()
				.getAvailablePowerForJob(serverJob, data, false);

		if (availablePower.isEmpty() || serverJob.getPower() > availablePower.get()) {
			var power = availablePower.orElse(0D);
			logger.info(RE_SUPPLY_FAILURE_NO_POWER_JOB_LOG, serverJob.getPower(), power, serverJob.getJobId());
			myGreenEnergyAgent.send(prepareReply(reply, NOT_ENOUGH_GREEN_POWER_CAUSE_MESSAGE, FAILURE));
		} else {
			if (myGreenEnergyAgent.getServerJobs().containsKey(serverJob)) {
				logger.info(RE_SUPPLY_JOB_WITH_GREEN_ENERGY_LOG, serverJob.getJobId());

				myGreenEnergyAgent.getServerJobs().replace(serverJob, ON_HOLD, IN_PROGRESS);
				myGreenEnergyAgent.getServerJobs().replace(serverJob, ON_HOLD_PLANNED, ACCEPTED);
				myGreenEnergyAgent.manage().updateGreenSourceGUI();

				myGreenEnergyAgent.send(prepareReply(reply, RE_SUPPLY_SUCCESSFUL_MESSAGE, INFORM));
			} else {
				logger.info(RE_SUPPLY_FAILURE_JOB_NOT_FOUND_LOG, serverJob.getJobId());
				myGreenEnergyAgent.send(prepareReply(reply, JOB_NOT_FOUND_CAUSE_MESSAGE, FAILURE));
			}
		}
	}
}
