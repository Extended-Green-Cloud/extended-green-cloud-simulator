package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.resource.processing;

import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.utils.resources.ResourcesUtilization.addResources;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.agent.ServerResources;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.gui.agents.cloudnetwork.CloudNetworkNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class ProcessServerResourceInformationRule extends AgentBasicRule<CloudNetworkAgentProps, CloudNetworkNode> {

	private static final Logger logger = getLogger(ProcessServerResourceInformationRule.class);

	public ProcessServerResourceInformationRule(
			final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription("SERVER_RESOURCE_INFORMATION_HANDLER_RULE",
				"handles server resource information",
				"rule run when resource information was received");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ServerResources serverResources = facts.get(MESSAGE_CONTENT);
		final ACLMessage request = facts.get(MESSAGE);
		final AID server = request.getSender();

		logger.info("CNA received information about resources of {}.", server.getLocalName());

		if (agentProps.getOwnedServerResources().isEmpty()) {
			agentProps.setAggregatedResources(new ConcurrentHashMap<>(serverResources.getResources()));
		} else {
			agentProps.addResourceCharacteristics(new HashMap<>(serverResources.getResources()));
			agentProps.setAggregatedResources(new ConcurrentHashMap<>(
					addResources(agentProps.getAggregatedResources(), serverResources.getResources())));
		}
		agentNode.updateResourceMap(agentProps.getAggregatedResources());

		agentProps.getOwnedServers().replace(server, true);
		agentProps.getOwnedServerResources().put(server, serverResources);
	}
}
