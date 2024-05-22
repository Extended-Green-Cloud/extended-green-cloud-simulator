package org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.resource.processing;

import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.utils.resources.ResourcesUtilization.addResources;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.agent.ServerResources;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class ProcessServerResourceInformationRule extends AgentBasicRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(ProcessServerResourceInformationRule.class);

	public ProcessServerResourceInformationRule(
			final RulesController<RegionalManagerAgentProps, RMANode> controller) {
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

		logger.info("RMA received information about resources of {}.", server.getLocalName());

		agentProps.addResourceCharacteristics(new HashMap<>(serverResources.getResources()));
		agentProps.setAggregatedResources(new ConcurrentHashMap<>(
				addResources(agentProps.getAggregatedResources(), serverResources.getResources())));

		agentNode.updateResourceMap(agentProps.getAggregatedResources());

		agentProps.getOwnedServers().replace(server, true);
		agentProps.getOwnedServerResources().put(server, serverResources);
		agentProps.getHighestExecutionTimeErrorForServer().put(server, 0L);
	}

	@Override
	public AgentRule copy() {
		return new ProcessServerResourceInformationRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
