package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.events.maintenance;

import static org.greencloud.commons.constants.FactTypeConstants.RESOURCES;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.utils.messaging.factory.AgentDiscoveryMessageFactory.prepareRequestInformingCNAAboutResourceChange;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentRequestRule;
import org.slf4j.Logger;

import com.gui.agents.server.ServerNode;

import jade.lang.acl.ACLMessage;

public class RequestServerMaintenanceInCNARule extends AgentRequestRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(RequestServerMaintenanceInCNARule.class);

	public RequestServerMaintenanceInCNARule(
			final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription("SERVER_MAINTENANCE_REQUEST_RULE",
				"rule sends information to CNA about the fact that resources of the given Server has been changed",
				"initiating resource change in CNA");
	}

	@Override
	protected ACLMessage createRequestMessage(final RuleSetFacts facts) {
		final Map<String, Resource> newResources = facts.get(RESOURCES);
		return prepareRequestInformingCNAAboutResourceChange(agentProps, newResources,
				controller.getLatestRuleSet().get());
	}

	@Override
	protected void handleInform(final ACLMessage inform, final RuleSetFacts facts) {
		final Map<String, Resource> newResources = facts.get(RESOURCES);

		logger.info("Received information that CNA adapted to new server resources!");
		agentNode.sendResultOfServerMaintenanceInCNA(true);
		agentProps.resources(new ConcurrentHashMap<>(newResources));

		final RuleSetFacts initializeResources = new RuleSetFacts(controller.getLatestRuleSet().get());
		facts.put(RULE_TYPE, "INITIALIZE_SERVER_RESOURCE_KNOWLEDGE");
		controller.fire(initializeResources);
		agentNode.confirmSuccessfulMaintenance();
	}

	@Override
	protected void handleRefuse(final ACLMessage refuse, final RuleSetFacts facts) {
		logger.info("Received information that CNA could not adapt to new server resources.");
		agentNode.sendResultOfServerMaintenanceInCNA(false);
	}
}
