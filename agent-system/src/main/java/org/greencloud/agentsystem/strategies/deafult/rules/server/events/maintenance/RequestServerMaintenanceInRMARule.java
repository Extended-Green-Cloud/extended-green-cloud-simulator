package org.greencloud.agentsystem.strategies.deafult.rules.server.events.maintenance;

import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.PREVIOUS_RESOURCES;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.INITIALIZE_SERVER_RESOURCE_KNOWLEDGE;
import static org.greencloud.commons.utils.messaging.factory.AgentDiscoveryMessageFactory.prepareRequestInformingRMAAboutResourceChange;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESOURCES;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentRequestRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

import jade.lang.acl.ACLMessage;

public class RequestServerMaintenanceInRMARule extends AgentRequestRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(RequestServerMaintenanceInRMARule.class);

	public RequestServerMaintenanceInRMARule(
			final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription("SERVER_MAINTENANCE_REQUEST_RULE",
				"rule sends information to RMA about the fact that resources of the given Server has been changed",
				"initiating resource change in RMA");
	}

	@Override
	protected ACLMessage createRequestMessage(final RuleSetFacts facts) {
		final Map<String, Resource> newResources = facts.get(RESOURCES);
		final ConcurrentHashMap<String, Resource> prevResources = new ConcurrentHashMap<>(agentProps.resources());
		agentProps.resources(new ConcurrentHashMap<>(newResources));

		final RuleSetFacts initializeResources = new RuleSetFacts(controller.getLatestLongTermRuleSetIdx().get());
		initializeResources.put(RULE_TYPE, INITIALIZE_SERVER_RESOURCE_KNOWLEDGE);
		controller.fire(initializeResources);

		facts.put(PREVIOUS_RESOURCES, prevResources);
		return prepareRequestInformingRMAAboutResourceChange(agentProps, agentProps.resources(),
				controller.getLatestLongTermRuleSetIdx().get());
	}

	@Override
	protected void handleInform(final ACLMessage inform, final RuleSetFacts facts) {
		logger.info("Received information that RMA adapted to new server resources!");
		agentNode.sendResultOfServerMaintenanceInRMA(true);
		agentNode.confirmSuccessfulMaintenance();
	}

	@Override
	protected void handleRefuse(final ACLMessage refuse, final RuleSetFacts facts) {
		final ConcurrentHashMap<String, Resource> prevResources = facts.get(PREVIOUS_RESOURCES);

		logger.info("Received information that RMA could not adapt to new server resources.");
		agentProps.resources(new ConcurrentHashMap<>(prevResources));
		agentNode.sendResultOfServerMaintenanceInRMA(false);
	}

	@Override
	public AgentRule copy() {
		return new RequestServerMaintenanceInRMARule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
