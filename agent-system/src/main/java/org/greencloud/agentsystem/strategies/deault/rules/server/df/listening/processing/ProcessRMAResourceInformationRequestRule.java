package org.greencloud.agentsystem.strategies.deault.rules.server.df.listening.processing;

import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.utils.messaging.factory.AgentDiscoveryMessageFactory.prepareResourceInformationMessage;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

public class ProcessRMAResourceInformationRequestRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessRMAResourceInformationRequestRule.class);

	public ProcessRMAResourceInformationRequestRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription("RMA_RESOURCE_REQUEST_HANDLER_RULE",
				"handle RMA resource information request",
				"sends information about resources of the server to RMA");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		logger.info("Received request for resource information. Sending information to {}",
				agentProps.getOwnerRegionalManagerAgent());
		agent.send(prepareResourceInformationMessage(agentProps, agentProps.getOwnerRegionalManagerAgent(),
				controller.getLatestLongTermRuleSetIdx().get()));
	}

	@Override
	public AgentRule copy() {
		return new ProcessRMAResourceInformationRequestRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
