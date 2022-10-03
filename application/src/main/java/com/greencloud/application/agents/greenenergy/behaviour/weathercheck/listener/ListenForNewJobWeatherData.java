package com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener;

import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener.logs.WeatherCheckListenerLog.INCORRECT_WEATHER_DATA_FORMAT_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener.logs.WeatherCheckListenerLog.NOT_ENOUGH_POWER_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener.logs.WeatherCheckListenerLog.POWER_SUPPLY_PROPOSAL_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener.logs.WeatherCheckListenerLog.TOO_BAD_WEATHER_LOG;
import static com.greencloud.application.agents.greenenergy.behaviour.weathercheck.listener.logs.WeatherCheckListenerLog.WEATHER_UNAVAILABLE_FOR_JOB_LOG;
import static com.greencloud.application.common.constant.LoggingConstant.MDC_JOB_ID;
import static com.greencloud.application.utils.GUIUtils.displayMessageArrow;
import static jade.lang.acl.MessageTemplate.MatchConversationId;
import static jade.lang.acl.MessageTemplate.MatchSender;
import static jade.lang.acl.MessageTemplate.and;
import static java.util.Objects.nonNull;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.greencloud.application.agents.greenenergy.GreenEnergyAgent;
import com.greencloud.application.agents.greenenergy.behaviour.powersupply.initiator.InitiatePowerSupplyOffer;
import com.greencloud.application.domain.MonitoringData;
import com.greencloud.application.domain.job.PowerJob;
import com.greencloud.application.messages.MessagingUtils;
import com.greencloud.application.messages.domain.factory.OfferMessageFactory;
import com.greencloud.application.messages.domain.factory.ReplyMessageFactory;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Behaviour listens for the Monitoring Agent's response with com.greencloud.application.weather data checked for new job execution.
 */
public class ListenForNewJobWeatherData extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForNewJobWeatherData.class);

	private final GreenEnergyAgent myGreenEnergyAgent;
	private final MessageTemplate template;
	private final ACLMessage cfp;
	private final PowerJob powerJob;
	private final SequentialBehaviour parentBehaviour;

	/**
	 * Behaviour constructor.
	 *
	 * @param myGreenAgent agent which is executing the behaviour
	 * @param cfp          call for proposal sent by the server to which the Green Source has to reply
	 * @param powerJob     job that is being processed
	 */
	public ListenForNewJobWeatherData(GreenEnergyAgent myGreenAgent, final ACLMessage cfp, final PowerJob powerJob,
			final SequentialBehaviour parentBehaviour) {
		this.template = and(MatchSender(myGreenAgent.getMonitoringAgent()), MatchConversationId(cfp.getConversationId()));
		this.myGreenEnergyAgent = myGreenAgent;
		this.cfp = cfp;
		this.powerJob = powerJob;
		this.parentBehaviour = parentBehaviour;
	}

	/**
	 * Method listens for the Monitoring Agent reply.
	 * It processes the received com.greencloud.application.weather information, calculates the available power and then either supplies the job with power
	 * and sends ACCEPT message, or sends the REFUSE message.
	 */
	@Override
	public void action() {
		final ACLMessage message = myAgent.receive(template);

		if (nonNull(message)) {
			MDC.put(MDC_JOB_ID, powerJob.getJobId());
			final MonitoringData data = readMonitoringData(message);

			if (nonNull(data)) {
				switch (message.getPerformative()) {
					case ACLMessage.REFUSE -> {
						logger.info(WEATHER_UNAVAILABLE_FOR_JOB_LOG);
						handleRefusal();
					}
					case ACLMessage.INFORM -> handleInform(data);
				}
				myAgent.removeBehaviour(parentBehaviour);
			}
		} else {
			block();
		}
	}

	private void handleInform(final MonitoringData data) {
		final Optional<Double> averageAvailablePower = myGreenEnergyAgent.managePower()
				.getAvailablePowerForJob(powerJob, data, true);
		final String jobId = powerJob.getJobId();

		if (averageAvailablePower.isEmpty()) {
			logger.info(TOO_BAD_WEATHER_LOG, jobId);
			handleRefusal();
		} else if (powerJob.getPower() > averageAvailablePower.get()) {
			logger.info(NOT_ENOUGH_POWER_LOG, jobId, powerJob.getPower(), averageAvailablePower.get());
			handleRefusal();
		} else {
			logger.info(POWER_SUPPLY_PROPOSAL_LOG, jobId);
			final ACLMessage offer = OfferMessageFactory.makeGreenEnergyPowerSupplyOffer(myGreenEnergyAgent, averageAvailablePower.get(),
					jobId, cfp.createReply());
			displayMessageArrow(myGreenEnergyAgent, cfp.getSender());
			myAgent.addBehaviour(new InitiatePowerSupplyOffer(myAgent, offer, data));
		}
	}

	private MonitoringData readMonitoringData(ACLMessage message) {
		try {
			return MessagingUtils.readMessageContent(message, MonitoringData.class);
		} catch (Exception e) {
			logger.info(INCORRECT_WEATHER_DATA_FORMAT_LOG);
			handleRefusal();
		}
		return null;
	}

	private void handleRefusal() {
		myGreenEnergyAgent.getPowerJobs().remove(powerJob);
		displayMessageArrow(myGreenEnergyAgent, cfp.getSender());
		myGreenEnergyAgent.send(ReplyMessageFactory.prepareRefuseReply(cfp.createReply()));
	}
}
