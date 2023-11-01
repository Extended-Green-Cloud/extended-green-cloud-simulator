package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.df;

import static org.greencloud.commons.constants.DFServiceConstants.CNA_SERVICE_TYPE;
import static org.greencloud.commons.enums.rules.RuleType.SUBSCRIBE_OWNED_AGENTS_SERVICE_RULE;
import static org.greencloud.commons.utils.yellowpages.YellowPagesRegister.prepareSubscription;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.gui.agents.scheduler.SchedulerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentSubscriptionRule;
import org.slf4j.Logger;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class SubscribeCloudNetworkAgentsRule extends AgentSubscriptionRule<SchedulerAgentProps, SchedulerNode> {

	private static final Logger logger = getLogger(SubscribeCloudNetworkAgentsRule.class);

	public SubscribeCloudNetworkAgentsRule(final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(SUBSCRIBE_OWNED_AGENTS_SERVICE_RULE,
				"subscribe Cloud Network Agent service",
				"handle changes in Cloud Network Agent service");
	}

	@Override
	protected ACLMessage createSubscriptionMessage(final RuleSetFacts facts) {
		return prepareSubscription(agent, agent.getDefaultDF(), CNA_SERVICE_TYPE);
	}

	@Override
	protected void handleRemovedAgents(final Map<AID, Boolean> removedAgents) {
		logger.info("Received message that {} Cloud Networks deregistered its service", removedAgents.size());
		agentProps.getAvailableCloudNetworks().removeAll(removedAgents.keySet());
	}

	@Override
	protected void handleAddedAgents(final Map<AID, Boolean> addedAgents) {
		logger.info("Received message that {} new Cloud Networks registered its service", addedAgents.size());
		agentProps.getAvailableCloudNetworks().addAll(addedAgents.keySet());
	}
}
