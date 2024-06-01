package org.greencloud.agentsystem.strategies.deafult.rules.centralmanager.df;

import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.constants.DFServiceConstants.RMA_SERVICE_TYPE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SUBSCRIBE_OWNED_AGENTS_SERVICE_RULE;
import static org.jrba.utils.yellowpages.YellowPagesRegister.prepareSubscription;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentSubscriptionRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class SubscribeRegionalManagerAgentsRule extends AgentSubscriptionRule<CentralManagerAgentProps, CMANode> {

	private static final Logger logger = getLogger(SubscribeRegionalManagerAgentsRule.class);

	public SubscribeRegionalManagerAgentsRule(final RulesController<CentralManagerAgentProps, CMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(SUBSCRIBE_OWNED_AGENTS_SERVICE_RULE,
				"subscribe Regional Manager Agent service",
				"handle changes in Regional Manager Agent service");
	}

	@Override
	protected ACLMessage createSubscriptionMessage(final RuleSetFacts facts) {
		return prepareSubscription(agent, agent.getDefaultDF(), RMA_SERVICE_TYPE);
	}

	@Override
	protected void handleRemovedAgents(final Map<AID, Boolean> removedAgents) {
		logger.info("Received message that {} Regional Managers deregistered its service", removedAgents.size());
		agentProps.getAvailableRegionalManagers().removeAll(removedAgents.keySet());
	}

	@Override
	protected void handleAddedAgents(final Map<AID, Boolean> addedAgents) {
		logger.info("Received message that {} new Regional Managers registered its service", addedAgents.size());
		agentProps.getAvailableRegionalManagers().addAll(addedAgents.keySet());
	}

	@Override
	public AgentRule copy() {
		return new SubscribeRegionalManagerAgentsRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
