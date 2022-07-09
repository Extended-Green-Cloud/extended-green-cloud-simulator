package agents.greenenergy.behaviour.powercheck;

import static mapper.JsonMapper.getMapper;

import agents.greenenergy.GreenEnergyAgent;
import com.fasterxml.jackson.core.JsonProcessingException;
import domain.ImmutableGreenSourceWeatherData;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Behaviour responsible for requesting weather data from monitoring agent
 */
public class RequestWeatherData extends OneShotBehaviour {

    private final GreenEnergyAgent myGreenEnergyAgent;
    private final ACLMessage message;

    /**
     * Behaviour constructor.
     *
     * @param greenEnergyAgent agent which is executing the behaviour
     * @param message          request message that was sent to green energy agent
     */
    public RequestWeatherData(GreenEnergyAgent greenEnergyAgent, ACLMessage message) {
        myGreenEnergyAgent = greenEnergyAgent;
        this.message = message;
    }

    /**
     * Method which sends the request to the Monitoring Agent asking for the weather at the given location.
     */
    @Override
    public void action() {
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.addReceiver(myGreenEnergyAgent.getMonitoringAgent());
        request.setConversationId(message.getConversationId());
        request.setProtocol(message.getProtocol());
        var requestData = ImmutableGreenSourceWeatherData.builder()
            .location(myGreenEnergyAgent.getLocation())
            .build();
        try {
            request.setContent(getMapper().writeValueAsString(requestData));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        myAgent.send(request);
    }
}
