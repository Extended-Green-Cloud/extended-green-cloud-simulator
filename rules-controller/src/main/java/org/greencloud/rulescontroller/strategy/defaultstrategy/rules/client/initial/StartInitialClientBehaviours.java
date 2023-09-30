package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.client.initial;

import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.SEARCH_OWNED_AGENTS_RULE;

import java.util.Set;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.listen.ListenForMessages;
import org.greencloud.rulescontroller.behaviour.search.SearchForAgents;
import org.greencloud.rulescontroller.rule.simple.AgentBehaviourRule;

import org.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.client.ClientNode;

import jade.core.behaviours.Behaviour;

public class StartInitialClientBehaviours extends AgentBehaviourRule<ClientAgentProps, ClientNode> {

	public StartInitialClientBehaviours(
			final RulesController<ClientAgentProps, ClientNode> controller) {
		super(controller);
	}

	/**
	 * Method initialize set of behaviours that are to be added
	 */
	@Override
	protected Set<Behaviour> initializeBehaviours() {
		return Set.of(
				SearchForAgents.create(agent, new StrategyFacts(controller.getLatestStrategy().get()),
						SEARCH_OWNED_AGENTS_RULE, controller),
				ListenForMessages.create(agent, JOB_STATUS_RECEIVER_RULE, controller, true)
		);
	}
}
