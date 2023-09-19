package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.cloudnetwork.df.listening.processing;

import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.greencloud.commons.enums.rules.RuleType.SERVER_STATUS_CHANGE_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.SERVER_STATUS_CHANGE_HANDLE_NOT_FOUND_RULE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareRefuseReply;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.cloudnetwork.CloudNetworkNode;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class ProcessServerStatusChangeNotFoundRule extends AgentBasicRule<CloudNetworkAgentProps, CloudNetworkNode> {

	private static final Logger logger = getLogger(ProcessServerStatusChangeNotFoundRule.class);

	public ProcessServerStatusChangeNotFoundRule(
			final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller) {
		super(controller, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(SERVER_STATUS_CHANGE_HANDLER_RULE, SERVER_STATUS_CHANGE_HANDLE_NOT_FOUND_RULE,
				"handles server connection change - server not found",
				"rule run when one of the Servers connected to the CNA changes its status but was not found in CNA");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		final ACLMessage request = facts.get(MESSAGE);
		final AID server = request.getSender();
		return !agentProps.getOwnedServers().containsKey(server);
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final ACLMessage request = facts.get(MESSAGE);
		final String type = facts.get(MESSAGE_TYPE);
		final AID server = request.getSender();

		logger.info("CNA didn't find the Server {} for {}.", type, server.getLocalName());
		agent.send(prepareRefuseReply(request));
	}
}
