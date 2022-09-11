package agents.greenenergy.behaviour.weathercheck.listener;

import static agents.greenenergy.behaviour.weathercheck.listener.logs.WeatherCheckListenerLog.INCORRECT_WEATHER_DATA_FORMAT_LOG;
import static agents.greenenergy.behaviour.weathercheck.listener.logs.WeatherCheckListenerLog.NOT_ENOUGH_POWER_LOG;
import static agents.greenenergy.behaviour.weathercheck.listener.logs.WeatherCheckListenerLog.POWER_SUPPLY_PROPOSAL_LOG;
import static agents.greenenergy.behaviour.weathercheck.listener.logs.WeatherCheckListenerLog.TOO_BAD_WEATHER_LOG;
import static agents.greenenergy.behaviour.weathercheck.listener.logs.WeatherCheckListenerLog.WEATHER_UNAVAILABLE_FOR_JOB_LOG;
import static jade.lang.acl.MessageTemplate.MatchConversationId;
import static jade.lang.acl.MessageTemplate.MatchSender;
import static jade.lang.acl.MessageTemplate.and;
import static java.util.Objects.nonNull;
import static messages.MessagingUtils.readMessageContent;
import static messages.domain.factory.OfferMessageFactory.makeGreenEnergyPowerSupplyOffer;
import static messages.domain.factory.ReplyMessageFactory.prepareRefuseReply;
import static utils.GUIUtils.displayMessageArrow;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agents.greenenergy.GreenEnergyAgent;
import agents.greenenergy.behaviour.powersupply.initiator.InitiatePowerSupplyOffer;
import domain.MonitoringData;
import domain.job.PowerJob;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Behaviour listens for the Monitoring Agent's response with weather data checked for new job execution.
 */
public class ListenForNewJobWeatherData extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ListenForNewJobWeatherData.class);

	private final GreenEnergyAgent myGreenEnergyAgent;
	private final MessageTemplate template;
	private final String guid;
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
		this.guid = myGreenEnergyAgent.getName();
		this.cfp = cfp;
		this.powerJob = powerJob;
		this.parentBehaviour = parentBehaviour;
	}

	/**
	 * Method listens for the Monitoring Agent reply.
	 * It processes the received weather information, calculates the available power and then either supplies the job with power
	 * and sends ACCEPT message, or sends the REFUSE message.
	 */
	@Override
	public void action() {
		final ACLMessage message = myAgent.receive(template);

		if (nonNull(message)) {
			final MonitoringData data = readMonitoringData(message);

			if (nonNull(data)) {
				switch (message.getPerformative()) {
					case ACLMessage.REFUSE -> {
						logger.info(WEATHER_UNAVAILABLE_FOR_JOB_LOG, guid);
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
		final Optional<Double> averageAvailablePower = myGreenEnergyAgent.manage()
				.getAvailablePowerForJob(powerJob, data, true);
		final String jobId = powerJob.getJobId();

		if (averageAvailablePower.isEmpty()) {
			logger.info(TOO_BAD_WEATHER_LOG, guid, jobId);
			handleRefusal();
		} else if (powerJob.getPower() > averageAvailablePower.get()) {
			logger.info(NOT_ENOUGH_POWER_LOG, guid, jobId, powerJob.getPower(), averageAvailablePower.get());
			handleRefusal();
		} else {
			logger.info(POWER_SUPPLY_PROPOSAL_LOG, guid, jobId);
			final ACLMessage offer = makeGreenEnergyPowerSupplyOffer(myGreenEnergyAgent, averageAvailablePower.get(),
					jobId, cfp.createReply());
			displayMessageArrow(myGreenEnergyAgent, cfp.getSender());
			myAgent.addBehaviour(new InitiatePowerSupplyOffer(myAgent, offer));
		}
	}

	private MonitoringData readMonitoringData(ACLMessage message) {
		try {
			return readMessageContent(message, MonitoringData.class);
		} catch (Exception e) {
			logger.info(INCORRECT_WEATHER_DATA_FORMAT_LOG, guid);
			handleRefusal();
		}
		return null;
	}

	private void handleRefusal() {
		myGreenEnergyAgent.getPowerJobs().remove(powerJob);
		displayMessageArrow(myGreenEnergyAgent, cfp.getSender());
		myGreenEnergyAgent.send(prepareRefuseReply(cfp.createReply()));
	}
}
