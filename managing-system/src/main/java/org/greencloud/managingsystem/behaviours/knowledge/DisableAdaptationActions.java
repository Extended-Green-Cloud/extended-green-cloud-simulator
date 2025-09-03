package org.greencloud.managingsystem.behaviours.knowledge;

import java.util.Collection;
import java.util.List;

import org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum;
import org.greencloud.managingsystem.agents.ManagingAgent;

import com.database.knowledge.domain.action.AdaptationActionsDefinitions;
import com.database.knowledge.timescale.TimescaleDatabase;

import jade.core.behaviours.OneShotBehaviour;

/**
 * Behaviour is responsible for disabling (initially) specified by the user adaptation actions.
 */
public class DisableAdaptationActions extends OneShotBehaviour {

	private final ManagingAgent managingAgent;
	private final List<String> actionsToDisable;

	/**
	 * Behaviour constructor.
	 *
	 * @param managingAgent    managing agent executing the behaviour
	 * @param actionsToDisable actions that are to be disabled
	 */
	public DisableAdaptationActions(final ManagingAgent managingAgent, final List<String> actionsToDisable) {
		this.managingAgent = managingAgent;
		this.actionsToDisable = actionsToDisable;
	}

	/**
	 * Method disables specified actions
	 */
	@Override
	public void action() {
		final TimescaleDatabase database = managingAgent.getAgentNode().getDatabaseClient();

		if (actionsToDisable.contains("ALL")) {
			disableAllActions(database);
			return;
		}

		actionsToDisable.stream()
				.map(AdaptationActionTypeEnum::valueOf)
				.map(AdaptationActionsDefinitions::getAdaptationAction)
				.flatMap(Collection::stream)
				.forEach(action -> database.setAdaptationActionAvailability(action.getActionId(), false));
	}

	private void disableAllActions(final TimescaleDatabase database) {
		AdaptationActionsDefinitions.getAdaptationActions()
				.forEach(action -> database.setAdaptationActionAvailability(action.getActionId(), false));
	}
}
