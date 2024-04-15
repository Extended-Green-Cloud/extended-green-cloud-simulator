package org.greencloud.agentsystem.strategies.deault.rules.scheduler.df;

import static org.greencloud.commons.constants.DFServiceConstants.RMA_SERVICE_TYPE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SUBSCRIBE_OWNED_AGENTS_SERVICE_RULE;
import static org.jrba.utils.yellowpages.YellowPagesRegister.prepareSubscription;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.greencloud.gui.agents.scheduler.SchedulerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentSubscriptionRule;
import org.slf4j.Logger;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class SubscribeRegionalManagerAgentsRule extends AgentSubscriptionRule<SchedulerAgentProps, SchedulerNode> {

	private static final Logger logger = getLogger(SubscribeRegionalManagerAgentsRule.class);

	public SubscribeRegionalManagerAgentsRule(final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
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
}
