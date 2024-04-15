package org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.df.listening;

import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SERVER_STATUS_CHANGE_HANDLER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.DISABLE_SERVER_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.ENABLE_SERVER_PROTOCOL;
import static org.jrba.rulesengine.enums.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.df.listening.processing.ProcessServerStatusChangeNotFoundRule;
import org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.df.listening.processing.ProcessServerStatusChangeRule;
import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.gui.agents.regionalmanager.RegionalManagerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

import jade.lang.acl.ACLMessage;

public class ProcessServerStatusChangeCombinedRule
		extends AgentCombinedRule<RegionalManagerAgentProps, RegionalManagerNode> {

	public ProcessServerStatusChangeCombinedRule(
			final RulesController<RegionalManagerAgentProps, RegionalManagerNode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(SERVER_STATUS_CHANGE_HANDLER_RULE,
				"handles change in status of connected servers",
				"rule run when one of the Servers connected to the RMA changes its status");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessServerStatusChangeNotFoundRule(controller),
				new ProcessServerStatusChangeRule(controller));
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ACLMessage request = facts.get(MESSAGE);
		final String action = request.getProtocol().equals(DISABLE_SERVER_PROTOCOL) ? "disabling" : "enabling";
		final boolean newStatus = request.getProtocol().equals(ENABLE_SERVER_PROTOCOL);

		facts.put(MESSAGE_CONTENT, newStatus);
		facts.put(MESSAGE_TYPE, action);
	}

}
