package agents.greenenergy.behaviour;

import static jade.lang.acl.ACLMessage.PROPOSE;
import static jade.lang.acl.MessageTemplate.*;
import static java.util.Objects.nonNull;
import static mapper.JsonMapper.getMapper;
import static messages.domain.ReplyMessageFactory.prepareRefuseReply;
import static messages.domain.ReplyMessageFactory.prepareReply;

import agents.greenenergy.GreenEnergyAgent;
import com.fasterxml.jackson.core.JsonProcessingException;
import domain.GreenSourceData;
import domain.ImmutableGreenSourceData;
import domain.MonitoringData;
import domain.job.Job;
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

    private final GreenEnergyAgent myGreenEnergyAgent;
    private final MessageTemplate template;
    private final String guid;
    private final ACLMessage cfp;
    private final PowerJob job;

    /**
     * Behaviour constructor.
     *
     * @param myGreenAgent agent which is executing the behaviour
     * @param cfp          call for proposal sent by the server to which the Green Source has to reply
     * @param job          job that is being processed
     */
    public ReceiveWeatherData(GreenEnergyAgent myGreenAgent, final ACLMessage cfp, final PowerJob job) {
        this.myGreenEnergyAgent = myGreenAgent;
        this.template = and(MatchSender(myGreenAgent.getMonitoringAgent()),
                            MatchConversationId(cfp.getConversationId()));
        this.guid = myGreenEnergyAgent.getName();
        this.cfp = cfp;
        this.job = job;
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
        final double power = myGreenEnergyAgent.getAvailablePower(job.getStartTime(), job.getEndTime(), data);

        if (job.getPower() > power) {
            logger.info("[{}] Refusing job with id {} - not enough available power. Needed {}, available {}", guid,
                job.getJobId(), job.getPower(), power);
            myAgent.send(ReplyMessageFactory.prepareRefuseReply(cfp.createReply()));
        }

        if (power > 0) {
            logger.info("[{}] Replying with propose message to server for job with id {}.", guid, job.getJobId());
            final GreenSourceData responseData = ImmutableGreenSourceData.builder()
                    .pricePerPowerUnit(myGreenEnergyAgent.getPricePerPowerUnit())
                    .availablePowerInTime(power)
                    .jobId(job.getJobId())
                    .build();
            myAgent.addBehaviour(new ProposePowerRequest(myAgent, prepareReply(cfp.createReply(), responseData, PROPOSE)));
        } else {
            logger.info("[{}] Too bad weather conditions, sending refuse message to server for job with id {}.", guid, job.getJobId());
            myAgent.send(ReplyMessageFactory.prepareRefuseReply(cfp.createReply()));
        }
    }

    private void handleRefuse(final ACLMessage cfp) {
        logger.info("[{}] Weather data not available, sending refuse message to server.", guid);
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
