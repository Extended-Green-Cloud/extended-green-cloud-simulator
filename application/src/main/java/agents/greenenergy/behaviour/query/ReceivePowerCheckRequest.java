package agents.greenenergy.behaviour.query;

import static common.constant.MessageProtocolConstants.SERVER_JOB_START_CHECK_PROTOCOL;
import static jade.lang.acl.ACLMessage.REQUEST;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.and;
import static mapper.JsonMapper.getMapper;

import agents.greenenergy.GreenEnergyAgent;
import com.fasterxml.jackson.core.JsonProcessingException;
import domain.job.PowerJob;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceivePowerCheckRequest extends CyclicBehaviour {

    private static final Logger logger = LoggerFactory.getLogger(ReceivePowerCheckRequest.class);
    private static final MessageTemplate messageTemplate = and(MatchPerformative(REQUEST),
        MatchProtocol(SERVER_JOB_START_CHECK_PROTOCOL));

    private final GreenEnergyAgent myGreenEnergyAgent;
    private final String guid;

    /**
     * Behaviours constructor.
     *
     * @param myAgent agent which is executing the behaviour
     */
    public ReceivePowerCheckRequest(Agent myAgent) {
        this.myGreenEnergyAgent = (GreenEnergyAgent) myAgent;
        this.guid = myGreenEnergyAgent.getName();
    }

    @Override
    public void action() {
        final ACLMessage message = myAgent.receive(messageTemplate);
        if (Objects.nonNull(message)) {
            try {
                logger.info("[{}] Sending weather request to monitoring agent.", guid);
                requestMonitoringData(message, readJob(message));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            block();
        }
    }

    private PowerJob readJob(ACLMessage message) throws NotUnderstoodException {
        try {
            return getMapper().readValue(message.getContent(), PowerJob.class);
        } catch (JsonProcessingException e) {
            throw new NotUnderstoodException(e.getMessage());
        }
    }

    private void requestMonitoringData(final ACLMessage message, final PowerJob job) {
        var sequentialBehaviour = new SequentialBehaviour();
        sequentialBehaviour.addSubBehaviour(new RequestWeatherData(myGreenEnergyAgent, message));
        sequentialBehaviour.addSubBehaviour(new ReceiveWeatherData(myGreenEnergyAgent, message, job));
        myAgent.addBehaviour(sequentialBehaviour);
    }
}
