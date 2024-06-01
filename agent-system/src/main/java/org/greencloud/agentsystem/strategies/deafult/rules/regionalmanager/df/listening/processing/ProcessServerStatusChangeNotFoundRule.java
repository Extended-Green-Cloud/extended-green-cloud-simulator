package org.greencloud.agentsystem.strategies.deafult.rules.regionalmanager.df.listening.processing;

import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SERVER_STATUS_CHANGE_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SERVER_STATUS_CHANGE_HANDLE_NOT_FOUND_RULE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareRefuseReply;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class ProcessServerStatusChangeNotFoundRule extends AgentBasicRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(ProcessServerStatusChangeNotFoundRule.class);

	public ProcessServerStatusChangeNotFoundRule(
			final RulesController<RegionalManagerAgentProps, RMANode> controller) {
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

	@Override
	public AgentRule copy() {
		return new ProcessServerStatusChangeNotFoundRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
