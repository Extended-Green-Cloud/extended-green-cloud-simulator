package agents.monitoring.behaviour;

import static common.constant.MessageProtocolConstants.SERVER_JOB_START_CHECK_PROTOCOL;
import static common.GUIUtils.displayMessageArrow;
import static jade.lang.acl.ACLMessage.INFORM;
import static jade.lang.acl.ACLMessage.QUERY_IF;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import static jade.lang.acl.MessageTemplate.MatchProtocol;
import static jade.lang.acl.MessageTemplate.and;
import static mapper.JsonMapper.getMapper;

import agents.monitoring.MonitoringAgent;
import domain.GreenSourceQueryData;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.IOException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServeWeatherInformation extends CyclicBehaviour {


    private static final Logger logger = LoggerFactory.getLogger(ServeWeatherInformation.class);
    private static final MessageTemplate template = and(MatchPerformative(QUERY_IF),
        MatchProtocol(SERVER_JOB_START_CHECK_PROTOCOL));

    private final MonitoringAgent monitoringAgent;

    /**
     * Behaviour constructor.
     *
     * @param monitoringAgent agent which is executing the behaviour
     */
    public ServeWeatherInformation(MonitoringAgent monitoringAgent) {
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
                var requestData = getMapper().readValue(message.getContent(), GreenSourceQueryData.class);
                var data = monitoringAgent.getWeather(requestData);
                response.setContent(getMapper().writeValueAsString(data));
            } catch (IOException e) {
                e.printStackTrace();
            }
            response.setConversationId(message.getConversationId());
            logger.info("[{}] Sending message with the weather data", myAgent.getName());
            displayMessageArrow(monitoringAgent, message.getSender());
            monitoringAgent.send(response);
        } else {
            block();
        }
    }
}
