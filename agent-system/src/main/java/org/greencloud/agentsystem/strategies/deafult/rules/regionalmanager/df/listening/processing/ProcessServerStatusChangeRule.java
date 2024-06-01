package org.greencloud.agentsystem.strategies.deafult.rules.regionalmanager.df.listening.processing;

import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SERVER_STATUS_CHANGE_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SERVER_STATUS_CHANGE_HANDLE_CHANGE_RULE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareInformReply;
import static org.greencloud.commons.utils.resources.ResourcesUtilization.addResources;
import static org.greencloud.commons.utils.resources.ResourcesUtilization.removeResources;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_TYPE;
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

public class ProcessServerStatusChangeRule extends AgentBasicRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(ProcessServerStatusChangeRule.class);

	public ProcessServerStatusChangeRule(
			final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller, 1);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(SERVER_STATUS_CHANGE_HANDLER_RULE, SERVER_STATUS_CHANGE_HANDLE_CHANGE_RULE,
				"handles server connection change",
				"rule run when one of the Servers connected to the RMA changes its status");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ACLMessage request = facts.get(MESSAGE);
		final AID server = request.getSender();
		return agentProps.getOwnedServers().containsKey(server);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ACLMessage request = facts.get(MESSAGE);
		final String type = facts.get(MESSAGE_TYPE);
		final boolean newStatus = facts.get(MESSAGE_CONTENT);
		final AID server = request.getSender();

		logger.info("RMA is {} Server {}.", type, server.getLocalName());
		final ServerResources serverResources = agentProps.getOwnedServerResources().get(server);

		agentProps.removeUnusedResources();
		agentProps.removeUnusedResourceCharacteristics();
		if (newStatus) {
			agentProps.addResourceCharacteristics(new HashMap<>(serverResources.getResources()));
			agentProps.setAggregatedResources(new ConcurrentHashMap<>(
					addResources(agentProps.getAggregatedResources(), serverResources.getResources())));
		} else {
			agentProps.setAggregatedResources(new ConcurrentHashMap<>(
					removeResources(agentProps.getAggregatedResources(), serverResources.getResources())));
		}
		agentNode.updateResourceMap(agentProps.getAggregatedResources());

		agentProps.getOwnedServers().replace(server, newStatus);
		agent.send(prepareInformReply(request));
	}

	@Override
	public AgentRule copy() {
		return new ProcessServerStatusChangeRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
