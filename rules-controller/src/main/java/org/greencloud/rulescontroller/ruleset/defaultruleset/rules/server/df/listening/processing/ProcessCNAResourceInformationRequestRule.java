package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.df.listening.processing;

import static org.greencloud.commons.utils.messaging.factory.AgentDiscoveryMessageFactory.prepareResourceInformationMessage;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.gui.agents.server.ServerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

public class ProcessCNAResourceInformationRequestRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessCNAResourceInformationRequestRule.class);

	public ProcessCNAResourceInformationRequestRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription("CNA_RESOURCE_REQUEST_HANDLER_RULE",
				"handle CNA resource information request",
				"sends information about resources of the server to CNA");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		logger.info("Received request for resource information. Sending information to {}",
				agentProps.getOwnerCloudNetworkAgent());
		agent.send(prepareResourceInformationMessage(agentProps, agentProps.getOwnerCloudNetworkAgent(),
				controller.getLatestRuleSet().get()));
	}
}
