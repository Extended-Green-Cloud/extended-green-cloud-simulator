package com.greencloud.application.behaviours;

import static com.greencloud.application.gui.GuiConnectionProvider.connectAgentObject;

import java.util.List;

import com.greencloud.application.agents.AbstractAgent;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;

/**
 * Behaviour responsible for retrieving the GUI controller for agent
 */
public class ReceiveGUIController extends CyclicBehaviour {

	private final AbstractAgent abstractAgent;
	private final List<Behaviour> initialBehaviours;
	private int objectCounter;

	/**
	 * Behaviour constructor.
	 *
	 * @param agent             agent executing the behaviour
	 * @param initialBehaviours initial behaviour for given agent
	 */
	public ReceiveGUIController(final AbstractAgent agent, final List<Behaviour> initialBehaviours) {
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
			connectAgentObject(abstractAgent, objectCounter, object);

			if (objectCounter == 1) {
				ParallelBehaviour behaviour = new ParallelBehaviour();
				initialBehaviours.forEach(behaviour::addSubBehaviour);
				behaviour.addSubBehaviour(new ReportHealthCheck(abstractAgent));
				behaviour.addSubBehaviour(new ListenForAdaptationAction(abstractAgent));
				abstractAgent.addBehaviour(behaviour);
			}
			objectCounter++;
		} else {
			block();
		}
	}
}
