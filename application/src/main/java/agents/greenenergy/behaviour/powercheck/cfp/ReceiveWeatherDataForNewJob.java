package agents.greenenergy.behaviour.powercheck.cfp;

import static utils.GUIUtils.displayMessageArrow;
import static jade.lang.acl.ACLMessage.PROPOSE;
import static jade.lang.acl.MessageTemplate.MatchConversationId;
import static jade.lang.acl.MessageTemplate.MatchSender;
import static jade.lang.acl.MessageTemplate.and;
import static java.util.Objects.nonNull;
import static messages.domain.factory.ReplyMessageFactory.prepareReply;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agents.greenenergy.GreenEnergyAgent;
import agents.greenenergy.behaviour.ProposePowerRequest;
import domain.GreenSourceData;
import domain.ImmutableGreenSourceData;
import domain.MonitoringData;
import domain.job.PowerJob;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import messages.domain.factory.ReplyMessageFactory;

/**
 * Behaviour which is responsible for listening for the Monitoring Agent's response with forecast data.
 */
public class ReceiveWeatherDataForNewJob extends CyclicBehaviour {

	private static final Logger logger = LoggerFactory.getLogger(ReceiveWeatherDataForNewJob.class);

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
	public ReceiveWeatherDataForNewJob(GreenEnergyAgent myGreenAgent, final ACLMessage cfp, final PowerJob powerJob,
			final
			SequentialBehaviour parentBehaviour) {
		this.myGreenEnergyAgent = myGreenAgent;
		this.template = and(MatchSender(myGreenAgent.getMonitoringAgent()),
				MatchConversationId(cfp.getConversationId()));
		this.guid = myGreenEnergyAgent.getName();
		this.cfp = cfp;
		this.powerJob = powerJob;
		this.parentBehaviour = parentBehaviour;
	}

	/**
	 * Method responsible for listening for the Monitoring Agent reply. It waits for the reply, then
	 * processes the received weather information, calculates the available power and then if there is enough
	 * power to execute the job, it sends the proposal response to the Server Agent. In other case it sends
	 * the refuse message.
	 */
	@Override
	public void action() {
		final ACLMessage message = myAgent.receive(template);
		if (nonNull(message)) {
			final MonitoringData data = myGreenEnergyAgent.manage().readMonitoringData(message, cfp);
			if (nonNull(data)) {
				switch (message.getPerformative()) {
					case ACLMessage.REFUSE -> handleRefuse(cfp, powerJob);
					case ACLMessage.INFORM -> handleInform(data);
				}
				myAgent.removeBehaviour(parentBehaviour);
			}
		} else {
			block();
		}
	}

	private void handleRefuse(final ACLMessage message, final PowerJob powerJob) {
		logger.info("[{}] Weather data not available, sending refuse message to server.", myGreenEnergyAgent.getName());
		myGreenEnergyAgent.getPowerJobs().remove(powerJob);
		displayMessageArrow(myGreenEnergyAgent, message.getAllReceiver());
		myGreenEnergyAgent.send(ReplyMessageFactory.prepareRefuseReply(message.createReply()));
	}

	private void handleInform(final MonitoringData data) {
		final Optional<Double> averageAvailablePower = myGreenEnergyAgent.manage()
				.getAverageAvailablePower(powerJob, data);
		final String jobId = powerJob.getJobId();

		if (averageAvailablePower.isEmpty()) {
			logger.info("[{}] Too bad weather conditions, sending refuse message to server for job with id {}.", guid,
					jobId);
			myGreenEnergyAgent.getPowerJobs().remove(powerJob);
			displayMessageArrow(myGreenEnergyAgent, cfp.getSender());
			myAgent.send(ReplyMessageFactory.prepareRefuseReply(cfp.createReply()));
		} else if (powerJob.getPower() > averageAvailablePower.get()) {
			logger.info("[{}] Refusing job with id {} - not enough available power. Needed {}, available {}", guid,
					jobId, powerJob.getPower(), averageAvailablePower.get());
			myGreenEnergyAgent.getPowerJobs().remove(powerJob);
			displayMessageArrow(myGreenEnergyAgent, cfp.getSender());
			myAgent.send(ReplyMessageFactory.prepareRefuseReply(cfp.createReply()));
		} else {
			logger.info("[{}] Replying with propose message to server for job with id {}.", guid, jobId);
			final GreenSourceData responseData = ImmutableGreenSourceData.builder()
					.pricePerPowerUnit(myGreenEnergyAgent.getPricePerPowerUnit())
					.availablePowerInTime(averageAvailablePower.get())
					.jobId(jobId)
					.build();
			displayMessageArrow(myGreenEnergyAgent, cfp.getSender());
			myAgent.addBehaviour(
					new ProposePowerRequest(myAgent, prepareReply(cfp.createReply(), responseData, PROPOSE)));
		}
	}
}
