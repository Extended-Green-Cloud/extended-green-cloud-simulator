package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.df.listening.processing;

import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.greencloud.commons.enums.rules.RuleType.SERVER_STATUS_CHANGE_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.SERVER_STATUS_CHANGE_HANDLE_CHANGE_RULE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareInformReply;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import com.gui.agents.cloudnetwork.CloudNetworkNode;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class ProcessServerStatusChangeRule extends AgentBasicRule<CloudNetworkAgentProps, CloudNetworkNode> {

	private static final Logger logger = getLogger(ProcessServerStatusChangeRule.class);

	public ProcessServerStatusChangeRule(
			final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller) {
		super(controller, 1);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(SERVER_STATUS_CHANGE_HANDLER_RULE, SERVER_STATUS_CHANGE_HANDLE_CHANGE_RULE,
				"handles server connection change",
				"rule run when one of the Servers connected to the CNA changes its status");
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

		logger.info("CNA is {} Server {}.", type, server.getLocalName());
		agentProps.getOwnedServers().replace(server, newStatus);
		agent.send(prepareInformReply(request));
	}
}
