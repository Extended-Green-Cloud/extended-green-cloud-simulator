package behaviours;

import agents.AbstractAgent;
import com.gui.controller.GUIController;
import com.gui.domain.nodes.AgentNode;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Behaviour responsible for retrieving the GUI controller for agent
 */
public class ReceiveGUIController extends CyclicBehaviour {

    private static final Logger logger = LoggerFactory.getLogger(ReceiveGUIController.class);

    private final AbstractAgent abstractAgent;
    private final List<Behaviour> initialBehaviours;
    private int objectCounter;

    /**
     * Behaviour constructor.
     *
     * @param agent             agent executing the behaviour
     * @param initialBehaviours initial behaviour for given agent
     */
    public ReceiveGUIController(final Agent agent, final List<Behaviour> initialBehaviours) {
        super(agent);
        this.abstractAgent = (AbstractAgent) agent;
        this.initialBehaviours = initialBehaviours;
        this.objectCounter = 0;
    }

    /**
     * Method retrieves the GUI Controller and stores it in agent class
     */
    @Override
    public void action() {
        final Object object = abstractAgent.getO2AObject();
        if (object != null) {
            if(object instanceof GUIController) {
                abstractAgent.setGuiController((GUIController) object);
            } else if (object instanceof AgentNode) {
                abstractAgent.setAgentNode((AgentNode) object);
            }
            if(objectCounter == 1) {
                logger.info("[{}] Agent connected with the controller", myAgent.getName());
                ParallelBehaviour behaviour = new ParallelBehaviour();
                initialBehaviours.forEach(behaviour::addSubBehaviour);
                abstractAgent.addBehaviour(behaviour);
            }
            objectCounter++;
        } else {
            block();
        }
    }
}
