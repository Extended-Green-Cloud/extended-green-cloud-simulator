package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.cloudnetwork.adaptation;

import static java.lang.Integer.parseInt;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_TYPE;
import static org.greencloud.commons.enums.rules.RuleType.REQUEST_STRATEGY_UPDATE_RULE;
import static org.greencloud.commons.utils.messaging.factory.StrategyAdaptationMessageFactory.prepareStrategyAdaptationRequest;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentRequestRule;
import org.slf4j.Logger;

import com.gui.agents.cloudnetwork.CloudNetworkNode;

import jade.lang.acl.ACLMessage;

public class UpdateStrategyForWeatherDropRule extends AgentRequestRule<CloudNetworkAgentProps, CloudNetworkNode> {

	private static final Logger logger = getLogger(UpdateStrategyForWeatherDropRule.class);

	public UpdateStrategyForWeatherDropRule(
			final RulesController<CloudNetworkAgentProps, CloudNetworkNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(REQUEST_STRATEGY_UPDATE_RULE,
				"sends strategy update request",
				"rule sends to all corresponding Servers, the strategy update request");
	}

	@Override
	protected ACLMessage createRequestMessage(final StrategyFacts facts) {
		final int nextIdx = controller.getLatestAdaptedStrategy().incrementAndGet();
		return prepareStrategyAdaptationRequest(facts.get(STRATEGY_IDX), nextIdx, facts.get(STRATEGY_TYPE),
				agentProps.getOwnedServers().keySet());
	}

	@Override
	protected void handleAllResults(final Collection<ACLMessage> informs, final Collection<ACLMessage> failures,
			final StrategyFacts facts) {
		final String strategyType = facts.get(STRATEGY_TYPE);
		final int indexOfNewStrategy = parseInt(informs.stream().findFirst().orElseThrow().getOntology());
		controller.addModifiedStrategy(facts.get(STRATEGY_TYPE), indexOfNewStrategy);
		logger.info("System components are changing strategy to {}!", strategyType);
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
