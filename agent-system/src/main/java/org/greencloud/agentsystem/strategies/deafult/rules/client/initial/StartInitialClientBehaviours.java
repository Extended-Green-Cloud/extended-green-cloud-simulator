package org.greencloud.agentsystem.strategies.deafult.rules.client.initial;

import static org.greencloud.commons.args.agent.EGCSAgentType.CLIENT;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SEARCH_OWNED_AGENTS_RULE;

import java.util.Set;

import org.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import org.greencloud.gui.agents.client.ClientNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.listen.ListenForMessages;
import org.jrba.rulesengine.behaviour.search.SearchForAgents;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.simple.AgentBehaviourRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

import jade.core.behaviours.Behaviour;

public class StartInitialClientBehaviours extends AgentBehaviourRule<ClientAgentProps, ClientNode> {

	public StartInitialClientBehaviours(final RulesController<ClientAgentProps, ClientNode> controller) {
		super(controller);
	}

	/**
	 * Method initialize set of behaviours that are to be added
	 */
	@Override
	protected Set<Behaviour> initializeBehaviours() {
		return Set.of(
				SearchForAgents.create(agent, new RuleSetFacts(controller.getLatestLongTermRuleSetIdx().get()),
						SEARCH_OWNED_AGENTS_RULE, controller),
				ListenForMessages.create(agent, JOB_STATUS_RECEIVER_RULE, controller, true)
		);
	}

	@Override
	public AgentRule copy() {
		return new StartInitialClientBehaviours(controller);
	}

	@Override
	public String getAgentType() {
		return CLIENT.getName();
	}
}
