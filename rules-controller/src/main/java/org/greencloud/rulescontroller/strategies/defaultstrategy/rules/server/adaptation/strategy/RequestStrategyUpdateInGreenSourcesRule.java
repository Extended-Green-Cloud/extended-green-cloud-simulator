package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.server.adaptation.strategy;

import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.NEXT_STRATEGY_IDX;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_TYPE;
import static org.greencloud.commons.enums.rules.RuleType.REQUEST_STRATEGY_UPDATE_RULE;
import static org.greencloud.commons.utils.messaging.factory.StrategyAdaptationMessageFactory.prepareStrategyAdaptationRequest;
import static org.greencloud.commons.utils.messaging.factory.StrategyAdaptationMessageFactory.prepareStrategyRequestReply;
import static java.lang.Integer.parseInt;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentRequestRule;
import org.slf4j.Logger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.enums.strategy.StrategyType;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.server.ServerNode;

import jade.lang.acl.ACLMessage;

public class RequestStrategyUpdateInGreenSourcesRule extends AgentRequestRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(RequestStrategyUpdateInGreenSourcesRule.class);

	public RequestStrategyUpdateInGreenSourcesRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(REQUEST_STRATEGY_UPDATE_RULE,
				"sends strategy update request",
				"rule sends to all corresponding Green Sources, the strategy update request");
	}

	@Override
	protected ACLMessage createRequestMessage(final StrategyFacts facts) {
		return prepareStrategyAdaptationRequest(facts.get(STRATEGY_IDX), facts.get(NEXT_STRATEGY_IDX),
				facts.get(STRATEGY_TYPE), agentProps.getOwnedGreenSources().keySet());
	}

	@Override
	protected void handleAllResults(final Collection<ACLMessage> informs, final Collection<ACLMessage> failures,
			final StrategyFacts facts) {
		final StrategyType strategyType = facts.get(STRATEGY_TYPE);
		final int indexOfNewStrategy = parseInt(informs.stream().findFirst().orElseThrow().getOntology());

		controller.addNewStrategy(strategyType, indexOfNewStrategy);
		logger.info("System components are changing strategy to {}!", strategyType);

		agent.send(prepareStrategyRequestReply(facts.get(MESSAGE), facts.get(NEXT_STRATEGY_IDX)));
	}

	@Override
	protected void handleInform(final ACLMessage inform, final StrategyFacts facts) {
		// case omitted
	}

	@Override
	protected void handleRefuse(final ACLMessage refuse, final StrategyFacts facts) {
		// case should not occur
	}

	@Override
	protected void handleFailure(final ACLMessage failure, final StrategyFacts facts) {
		// case should not occur
	}
}
