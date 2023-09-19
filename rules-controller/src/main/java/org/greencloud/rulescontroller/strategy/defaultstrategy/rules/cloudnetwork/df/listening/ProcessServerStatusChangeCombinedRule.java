package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.cloudnetwork.df.listening;

import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.greencloud.commons.enums.rules.RuleType.SERVER_STATUS_CHANGE_HANDLER_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.DISABLE_SERVER_PROTOCOL;
import static org.greencloud.commons.utils.messaging.constants.MessageProtocolConstants.ENABLE_SERVER_PROTOCOL;
import static org.greencloud.rulescontroller.rule.combined.domain.AgentCombinedRuleType.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.strategy.defaultstrategy.rules.cloudnetwork.df.listening.processing.ProcessServerStatusChangeNotFoundRule;
import org.greencloud.rulescontroller.strategy.defaultstrategy.rules.cloudnetwork.df.listening.processing.ProcessServerStatusChangeRule;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.combined.AgentCombinedRule;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;

import com.gui.agents.cloudnetwork.CloudNetworkNode;

import jade.lang.acl.ACLMessage;

public class ProcessServerStatusChangeCombinedRule extends AgentCombinedRule<CloudNetworkAgentProps, CloudNetworkNode> {

	public ProcessServerStatusChangeCombinedRule(
			final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(SERVER_STATUS_CHANGE_HANDLER_RULE,
				"handles change in status of connected servers",
				"rule run when one of the Servers connected to the CNA changes its status");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessServerStatusChangeNotFoundRule(controller),
				new ProcessServerStatusChangeRule(controller));
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final ACLMessage request = facts.get(MESSAGE);
		final String action = request.getProtocol().equals(DISABLE_SERVER_PROTOCOL) ? "disabling" : "enabling";
		final boolean newStatus = request.getProtocol().equals(ENABLE_SERVER_PROTOCOL);

		facts.put(MESSAGE_CONTENT, newStatus);
		facts.put(MESSAGE_TYPE, action);
	}

}
