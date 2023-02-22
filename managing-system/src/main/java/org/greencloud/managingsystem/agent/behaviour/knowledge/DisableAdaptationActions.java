package org.greencloud.managingsystem.agent.behaviour.knowledge;

import static com.database.knowledge.domain.action.AdaptationActionsDefinitions.getAdaptationAction;

import java.util.List;

import org.greencloud.managingsystem.agent.ManagingAgent;

import com.database.knowledge.domain.action.AdaptationActionEnum;
import com.database.knowledge.timescale.TimescaleDatabase;

import jade.core.behaviours.OneShotBehaviour;

/**
 * Behaviour is responsible for disabling (initially) specified by the user adaptation actions
 */
public class DisableAdaptationActions extends OneShotBehaviour {

	private final ManagingAgent managingAgent;
	private final List<String> actionsToDisable;

	/**
	 * Behaviour constructor
	 *
	 * @param managingAgent    managing agent executing the behaviour
	 * @param actionsToDisable actions that are to be disabled
	 */
	public DisableAdaptationActions(ManagingAgent managingAgent, final List<String> actionsToDisable) {
		this.managingAgent = managingAgent;
		this.actionsToDisable = actionsToDisable;
	}

	/**
	 * Method disables specified actions
	 */
	@Override
	public void action() {
		final TimescaleDatabase database = managingAgent.getAgentNode().getDatabaseClient();

		actionsToDisable.forEach(action -> database.setAdaptationActionAvailability(
				getAdaptationAction(AdaptationActionEnum.valueOf(action)).getActionId(), false));
	}
}
