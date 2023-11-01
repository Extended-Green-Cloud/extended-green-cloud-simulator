package org.greencloud.agentsystem.behaviours;

import static org.greencloud.agentsystem.utils.AgentConnector.connectAgentObject;

import java.util.List;

import org.greencloud.agentsystem.agents.AbstractAgent;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;

/**
 * Generic behaviour responsible for retrieving the GUI controller for a given agent
 */
public class ListenForControllerObjects extends CyclicBehaviour {

	private final AbstractAgent<?, ?> abstractAgent;
	private final List<Behaviour> initialBehaviours;
	private int objectCounter;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent             agent executing the behaviour
	 * @param initialBehaviours initial behaviour for given agent
	 */
	public ListenForControllerObjects(final AbstractAgent<?, ?> agent, final List<Behaviour> initialBehaviours) {
		super(agent);
		this.abstractAgent = agent;
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
			connectAgentObject(abstractAgent, object);

			if (objectCounter == 1) {
				final ParallelBehaviour behaviour = new ParallelBehaviour();
				initialBehaviours.forEach(behaviour::addSubBehaviour);
				behaviour.addSubBehaviour(new ReportHealthCheck(abstractAgent));
				behaviour.addSubBehaviour(new ListenForAdaptationAction(abstractAgent));
				abstractAgent.addBehaviour(behaviour);
				abstractAgent.setMainBehaviour(behaviour);
				abstractAgent.removeBehaviour(this);
			}
			objectCounter++;
		} else {
			block();
		}
	}
}
