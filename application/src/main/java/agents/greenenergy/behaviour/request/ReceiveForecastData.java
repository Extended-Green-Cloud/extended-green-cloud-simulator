package agents.greenenergy.behaviour.request;

import static common.GUIUtils.displayMessageArrow;
import static jade.lang.acl.ACLMessage.PROPOSE;
import static jade.lang.acl.MessageTemplate.MatchConversationId;
import static jade.lang.acl.MessageTemplate.MatchSender;
import static jade.lang.acl.MessageTemplate.and;
import static java.util.Objects.nonNull;
import static mapper.JsonMapper.getMapper;
import static messages.domain.ReplyMessageFactory.prepareReply;

import agents.greenenergy.GreenEnergyAgent;
import com.fasterxml.jackson.core.JsonProcessingException;
import domain.GreenSourceData;
import domain.ImmutableGreenSourceData;
import domain.MonitoringData;
import domain.job.PowerJob;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.Optional;
import messages.domain.ReplyMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Behaviour which is responsible for listening for the Monitoring Agent's response with weather data.
 */
public class ReceiveForecastData extends CyclicBehaviour {

    private static final Logger logger = LoggerFactory.getLogger(ReceiveForecastData.class);

    private final GreenEnergyAgent myGreenEnergyAgent;
    private final MessageTemplate template;
    private final String guid;
    private final ACLMessage cfp;
    private final PowerJob powerJob;

    /**
     * Behaviour constructor.
     *
     * @param myGreenAgent agent which is executing the behaviour
     * @param cfp          call for proposal sent by the server to which the Green Source has to reply
     * @param powerJob     job that is being processed
     */
    public ReceiveForecastData(GreenEnergyAgent myGreenAgent, final ACLMessage cfp, final PowerJob powerJob) {
        this.myGreenEnergyAgent = myGreenAgent;
        this.template = and(MatchSender(myGreenAgent.getMonitoringAgent()),
                            MatchConversationId(cfp.getConversationId()));
        this.guid = myGreenEnergyAgent.getName();
        this.cfp = cfp;
        this.powerJob = powerJob;
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
            final MonitoringData data = readMonitoringData(message);
            if (nonNull(data)) {
                switch (message.getPerformative()) {
                    case ACLMessage.REFUSE -> handleRefuse(cfp);
                    case ACLMessage.INFORM -> handleInform(data);
                    default -> block();
                }
            }
        } else {
            block();
        }
    }

    private void handleInform(final MonitoringData data) {
        Optional<Double> averageAvailablePower = myGreenEnergyAgent.getAverageAvailablePower(powerJob, data);

        if(averageAvailablePower.isEmpty()) {
            logger.info("[{}] Too bad weather conditions, sending refuse message to server for job with id {}.", guid, powerJob.getJobId());
            myGreenEnergyAgent.getPowerJobs().remove(powerJob);
            displayMessageArrow(myGreenEnergyAgent, cfp.getAllReceiver());
            myAgent.send(ReplyMessageFactory.prepareRefuseReply(cfp.createReply()));
        }
        else if (powerJob.getPower() > averageAvailablePower.get()) {
            logger.info("[{}] Refusing job with id {} - not enough available power. Needed {}, available {}", guid,
                powerJob.getJobId(), powerJob.getPower(),  averageAvailablePower.get());
            myGreenEnergyAgent.getPowerJobs().remove(powerJob);
            displayMessageArrow(myGreenEnergyAgent, cfp.getSender());
            myAgent.send(ReplyMessageFactory.prepareRefuseReply(cfp.createReply()));
        }
        else {
            logger.info("[{}] Replying with propose message to server for job with id {}.", guid, powerJob.getJobId());
            final GreenSourceData responseData = ImmutableGreenSourceData.builder()
                    .pricePerPowerUnit(myGreenEnergyAgent.getPricePerPowerUnit())
                    .availablePowerInTime(averageAvailablePower.get())
                    .jobId(powerJob.getJobId())
                    .build();
            displayMessageArrow(myGreenEnergyAgent, cfp.getAllReceiver());
            myAgent.addBehaviour(new ProposePowerRequest(myAgent, prepareReply(cfp.createReply(), responseData, PROPOSE)));
        }
    }

    private void handleRefuse(final ACLMessage cfp) {
        logger.info("[{}] Weather data not available, sending refuse message to server.", guid);
        myGreenEnergyAgent.getPowerJobs().remove(powerJob);
        displayMessageArrow(myGreenEnergyAgent, cfp.getAllReceiver());
        myAgent.send(ReplyMessageFactory.prepareRefuseReply(cfp));
    }

    private MonitoringData readMonitoringData(ACLMessage message) {
        try {
            return getMapper().readValue(message.getContent(), MonitoringData.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
