package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.server.adaptation.strategy;

import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.NEXT_STRATEGY_IDX;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_TYPE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_STRATEGY_UPDATE_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.REQUEST_STRATEGY_UPDATE_RULE;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateRequest;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.strategy.StrategyUpdate;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.server.ServerNode;

public class ProcessStrategyUpdateRequestRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessStrategyUpdateRequestRule.class);

	public ProcessStrategyUpdateRequestRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_STRATEGY_UPDATE_HANDLER_RULE,
				"handles strategy update messages",
				"handling messages from CNA asking Server to update its strategy");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		return true;
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final StrategyUpdate updateData = facts.get(MESSAGE_CONTENT);
		logger.info("CNA asked Server to update its strategy to {}! Passing information to underlying Green Sources.",
				updateData.getStrategyType());

		final StrategyFacts handlerFacts = new StrategyFacts(facts.get(STRATEGY_IDX));
		handlerFacts.put(MESSAGE, facts.get(MESSAGE));
		handlerFacts.put(STRATEGY_TYPE, updateData.getStrategyType());
		handlerFacts.put(NEXT_STRATEGY_IDX, updateData.getStrategyIdx());
		agent.addBehaviour(InitiateRequest.create(agent, handlerFacts, REQUEST_STRATEGY_UPDATE_RULE, controller));
	}
}
