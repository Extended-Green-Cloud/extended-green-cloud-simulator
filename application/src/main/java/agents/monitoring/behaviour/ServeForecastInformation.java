package agents.monitoring.behaviour;

import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.REQUEST;
import static mapper.JsonMapper.getMapper;

import agents.monitoring.MonitoringAgent;
import domain.GreenSourceRequestData;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * Behaviour responsible for listening for the upcoming weather requests
 */
public class ServeForecastInformation extends CyclicBehaviour {

    private static final Logger logger = LoggerFactory.getLogger(ServeForecastInformation.class);
    private static final MessageTemplate template = MessageTemplate.MatchPerformative(REQUEST);

    private final MonitoringAgent monitoringAgent;

    /**
     * Behaviour constructor.
     *
     * @param monitoringAgent agent which is executing the behaviour
     */
    public ServeForecastInformation(MonitoringAgent monitoringAgent) {
        this.monitoringAgent = monitoringAgent;
    }

    /**
     * Method which listens for the request for weather data coming from the Green Source Agents.
     * It retrieves the weather information for the given location and forwards it as a reply to the sender.
     */
    @Override
    public void action() {
        final ACLMessage message = monitoringAgent.receive(template);

        if (Objects.nonNull(message)) {
            final ACLMessage response = message.createReply();
            response.setPerformative(INFORM);
            try {
                var requestData = getMapper().readValue(message.getContent(), GreenSourceRequestData.class);
                var data = monitoringAgent.getForecast(requestData);
                response.setContent(getMapper().writeValueAsString(data));
            } catch (IOException e) {
                e.printStackTrace();
            }
            response.setConversationId(message.getConversationId());
            logger.info("Sending message with the forecast data");
            monitoringAgent.send(response);
        } else {
            block();
        }
    }
}
