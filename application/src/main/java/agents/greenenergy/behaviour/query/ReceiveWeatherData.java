package agents.greenenergy.behaviour.query;

import static common.constant.MessageProtocolConstants.SERVER_JOB_START_CHECK_PROTOCOL;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.and;
import static java.util.Objects.nonNull;
import static mapper.JsonMapper.getMapper;

import agents.greenenergy.GreenEnergyAgent;
import com.fasterxml.jackson.core.JsonProcessingException;
import domain.WeatherData;
import domain.job.PowerJob;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import messages.domain.ReplyMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Behaviour which is responsible for listening for the Monitoring Agent's response with weather data.
 */
public class ReceiveWeatherData extends CyclicBehaviour {

    private static final Logger logger = LoggerFactory.getLogger(ReceiveWeatherData.class);
    private static final MessageTemplate messageTemplate = and(MatchPerformative(INFORM),
        MatchProtocol(SERVER_JOB_START_CHECK_PROTOCOL));

    private final GreenEnergyAgent myGreenEnergyAgent;
    //private final MessageTemplate template;
    private final String guid;
    private final ACLMessage originalMessage;
    private final PowerJob powerJob;

    /**
     * Behaviour constructor.
     *
     * @param myGreenAgent agent which is executing the behaviour
     * @param message      message sent by the server to which the Green Source has to reply
     * @param powerJob     job that is being processed
     */
    public ReceiveWeatherData(GreenEnergyAgent myGreenAgent, final ACLMessage message, final PowerJob powerJob) {
        this.myGreenEnergyAgent = myGreenAgent;
        this.guid = myGreenEnergyAgent.getName();
        this.originalMessage = message;
        this.powerJob = powerJob;
    }

    /**
     * Method responsible for listening for the Monitoring Agent reply. It waits for the reply, then
     * processes the received weather information, re-checks the available power and informs the server about the
     * outcome
     */
    @Override
    public void action() {
        final ACLMessage message = myAgent.receive(messageTemplate);
        if (nonNull(message)) {
            final WeatherData data = readWeatherData(message);
            if (nonNull(data)) {
                switch (message.getPerformative()) {
                    case ACLMessage.REFUSE -> handleRefuse(originalMessage);
                    case INFORM -> handleInform(data);
                    default -> block();
                }
            }
        } else {
            block();
        }
    }

    private void handleInform(final WeatherData data) {
        double availablePower = myGreenEnergyAgent.getAvailablePower(data);

        if (availablePower < 0) {
            logger.info("[{}] Weather has changed before executing job with id {} - not enough available power. Needed {}, available {}",
                guid, powerJob.getJobId(), powerJob.getPower(), availablePower);
            myGreenEnergyAgent.getPowerJobs().remove(powerJob);
            myAgent.send(ReplyMessageFactory.prepareReply(originalMessage.createReply(), "ABORT", INFORM));
        }
        else {
            logger.info("[{}] Everything okay - continuing job {} execution!", guid, powerJob.getJobId());
            myAgent.send(ReplyMessageFactory.prepareReply(originalMessage.createReply(), "OK", INFORM));
        }
    }

    private void handleRefuse(final ACLMessage message) {
        logger.info("[{}] Weather data not available, sending refuse message to server.", guid);
        myGreenEnergyAgent.getPowerJobs().remove(powerJob);
        myAgent.send(ReplyMessageFactory.prepareRefuseReply(message));
    }

    private WeatherData readWeatherData(ACLMessage message) {
        try {
            return getMapper().readValue(message.getContent(), WeatherData.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
