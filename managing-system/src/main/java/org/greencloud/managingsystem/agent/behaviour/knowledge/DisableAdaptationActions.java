package org.greencloud.managingsystem.agent.behaviour.knowledge;

import java.util.Collection;
import java.util.List;

import org.greencloud.managingsystem.agent.ManagingAgent;

import org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum;
import com.database.knowledge.domain.action.AdaptationActionsDefinitions;
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

		actionsToDisable.stream()
				.map(AdaptationActionTypeEnum::valueOf)
				.map(AdaptationActionsDefinitions::getAdaptationAction)
				.flatMap(Collection::stream)
				.forEach(action -> database.setAdaptationActionAvailability(action.getActionId(), false));
	}
}
