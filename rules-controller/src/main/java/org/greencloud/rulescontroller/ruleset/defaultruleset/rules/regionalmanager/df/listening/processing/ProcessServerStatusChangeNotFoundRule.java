package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.regionalmanager.df.listening.processing;

import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.greencloud.commons.enums.rules.RuleType.SERVER_STATUS_CHANGE_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.SERVER_STATUS_CHANGE_HANDLE_NOT_FOUND_RULE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareRefuseReply;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.gui.agents.regionalmanager.RegionalManagerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class ProcessServerStatusChangeNotFoundRule extends AgentBasicRule<RegionalManagerAgentProps, RegionalManagerNode> {

	private static final Logger logger = getLogger(ProcessServerStatusChangeNotFoundRule.class);

	public ProcessServerStatusChangeNotFoundRule(
			final RulesController<RegionalManagerAgentProps, RegionalManagerNode> controller) {
		super(controller, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(SERVER_STATUS_CHANGE_HANDLER_RULE, SERVER_STATUS_CHANGE_HANDLE_NOT_FOUND_RULE,
				"handles server connection change - server not found",
				"rule run when one of the Servers connected to the RMA changes its status but was not found in RMA");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ACLMessage request = facts.get(MESSAGE);
		final AID server = request.getSender();
		return !agentProps.getOwnedServers().containsKey(server);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ACLMessage request = facts.get(MESSAGE);
		final String type = facts.get(MESSAGE_TYPE);
		final AID server = request.getSender();

		logger.info("RMA didn't find the Server {} for {}.", type, server.getLocalName());
		agent.send(prepareRefuseReply(request));
	}
}
