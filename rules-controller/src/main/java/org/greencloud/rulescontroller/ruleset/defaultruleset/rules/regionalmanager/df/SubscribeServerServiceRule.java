package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.df;

import static org.greencloud.commons.constants.DFServiceConstants.SA_SERVICE_TYPE;
import static org.greencloud.commons.enums.rules.RuleType.SUBSCRIBE_OWNED_AGENTS_SERVICE_RULE;
import static org.greencloud.commons.utils.messaging.factory.AgentDiscoveryMessageFactory.prepareRequestForResourceInformationMessage;
import static org.greencloud.commons.utils.yellowpages.YellowPagesRegister.prepareSubscription;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;
import java.util.Set;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.gui.agents.regionalmanager.RegionalManagerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentSubscriptionRule;
import org.slf4j.Logger;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class SubscribeServerServiceRule extends AgentSubscriptionRule<RegionalManagerAgentProps, RegionalManagerNode> {

	private static final Logger logger = getLogger(SubscribeServerServiceRule.class);

	public SubscribeServerServiceRule(final RulesController<RegionalManagerAgentProps, RegionalManagerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(SUBSCRIBE_OWNED_AGENTS_SERVICE_RULE,
				"subscribe Server Agent service",
				"handle changes in Server Agent service");
	}

	@Override
	protected ACLMessage createSubscriptionMessage(final RuleSetFacts facts) {
		return prepareSubscription(agent, agent.getDefaultDF(), SA_SERVICE_TYPE, agent.getName());
	}

	@Override
	protected void handleRemovedAgents(final Map<AID, Boolean> removedAgents) {
		logger.info("Found {} removed servers in the network!", removedAgents.size());
		agentProps.getOwnedServers().entrySet().removeIf(server -> removedAgents.containsKey(server.getKey()));
		removedAgents.forEach(agentProps.getWeightsForServersMap()::remove);
	}

	@Override
	protected void handleAddedAgents(final Map<AID, Boolean> addedAgents) {
		logger.info("Found {} new servers in the network!", addedAgents.size());
		initializeWeights(addedAgents.keySet());
		addedAgents.replaceAll((key, val) -> false);
		agentProps.getOwnedServers().putAll(addedAgents);
		addedAgents.forEach((server, state) -> agent.send(
				prepareRequestForResourceInformationMessage(server, controller.getLatestLongTermRuleSetIdx().get())));
	}

	private void initializeWeights(Set<AID> addedServers) {
		addedServers.forEach(server -> agentProps.getWeightsForServersMap().put(server, 1));
	}
}
