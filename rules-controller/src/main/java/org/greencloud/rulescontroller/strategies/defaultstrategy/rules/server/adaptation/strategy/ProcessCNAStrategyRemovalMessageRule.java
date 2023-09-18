package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.server.adaptation.strategy;

import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_STRATEGY_REMOVAL_HANDLER_RULE;
import static org.greencloud.commons.utils.messaging.factory.StrategyAdaptationMessageFactory.prepareStrategyRemovalRequest;
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.server.ServerNode;

import jade.lang.acl.ACLMessage;

public class ProcessCNAStrategyRemovalMessageRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessCNAStrategyRemovalMessageRule.class);

	public ProcessCNAStrategyRemovalMessageRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_STRATEGY_REMOVAL_HANDLER_RULE,
				"process strategy removal messages",
				"processing messages from CNA asking Server to remove given strategy");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		return true;
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final ACLMessage message = facts.get(MESSAGE);
		final Integer strategyIdx = parseInt(message.getContent());

		MDC.put(MDC_STRATEGY_ID, valueOf(strategyIdx));
		logger.info(
				"Received strategy removal request from CNA. Removing strategy with id {} and informing Green Sources",
				strategyIdx);

		controller.getStrategies().remove(strategyIdx);
		agent.send(prepareStrategyRemovalRequest(strategyIdx, agentProps.getOwnedGreenSources().keySet()));
	}
}
