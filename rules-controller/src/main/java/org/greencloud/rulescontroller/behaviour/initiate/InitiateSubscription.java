package org.greencloud.rulescontroller.behaviour.initiate;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_STEP;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.constants.FactTypeConstants.SUBSCRIPTION_ADDED_AGENTS;
import static org.greencloud.commons.constants.FactTypeConstants.SUBSCRIPTION_CREATE_MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.SUBSCRIPTION_REMOVED_AGENTS;
import static org.greencloud.commons.enums.rules.RuleStepType.SUBSCRIPTION_CREATE_STEP;
import static org.greencloud.commons.enums.rules.RuleStepType.SUBSCRIPTION_HANDLE_AGENTS_RESPONSE_STEP;
import static org.greencloud.commons.utils.yellowpages.YellowPagesRegister.decodeSubscription;

import java.util.Map;
import java.util.function.ToIntFunction;

import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.commons.enums.rules.RuleType;
import org.greencloud.commons.mapper.FactsMapper;
import org.greencloud.rulescontroller.RulesController;
import org.jeasy.rules.api.Facts;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;

/**
 * Abstract behaviour providing template initiating Subscription protocol handled with rules
 */
public class InitiateSubscription extends SubscriptionInitiator {

	protected final ToIntFunction<Facts> selectStrategy;
	final StrategyFacts facts;
	protected RulesController<?, ?> controller;

	protected InitiateSubscription(final Agent agent, final StrategyFacts facts,
			final RulesController<?, ?> controller, final ToIntFunction<Facts> selectStrategy) {
		super(agent, facts.get(SUBSCRIPTION_CREATE_MESSAGE));
		this.facts = facts;
		this.controller = controller;
		this.selectStrategy = isNull(selectStrategy) ? o -> controller.getLatestStrategy().get() : selectStrategy;
	}

	/**
	 * Method creates behaviour
	 *
	 * @param agent      agent executing the behaviour
	 * @param facts      facts under which the Subscription message is to be created
	 * @param ruleType   type of the rule that handles Subscription execution
	 * @param controller rules controller
	 * @return InitiateSubscription
	 */
	public static InitiateSubscription create(final Agent agent, final StrategyFacts facts, final RuleType ruleType,
			final RulesController<?, ?> controller) {
		final StrategyFacts methodFacts = FactsMapper.mapToStrategyFacts(facts);
		methodFacts.put(RULE_TYPE, ruleType);
		methodFacts.put(RULE_STEP, SUBSCRIPTION_CREATE_STEP);
		controller.fire(methodFacts);

		return new InitiateSubscription(agent, methodFacts, controller, null);
	}

	/**
	 * Method creates behaviour
	 *
	 * @param agent          agent executing the behaviour
	 * @param facts          facts under which the Subscription message is to be created
	 * @param ruleType       type of the rule that handles Subscription execution
	 * @param controller     rules controller
	 * @param selectStrategy predicate specifying how the strategy of the given behaviour should be selected
	 * @return InitiateSubscription
	 */
	public static InitiateSubscription create(final Agent agent, final StrategyFacts facts, final RuleType ruleType,
			final RulesController<?, ?> controller, final ToIntFunction<Facts> selectStrategy) {
		final StrategyFacts methodFacts = FactsMapper.mapToStrategyFacts(facts);
		methodFacts.put(RULE_TYPE, ruleType);
		methodFacts.put(RULE_STEP, SUBSCRIPTION_CREATE_STEP);
		controller.fire(methodFacts);

		return new InitiateSubscription(agent, methodFacts, controller, selectStrategy);
	}

	/**
	 * Method is triggered when agents register/deregister their services in DF.
	 * It groups the agents into 2 lists based on registration state
	 * (i.e. agents that registered their service and agents that deregistered their service).
	 * Then, it applies predefined handling methods.
	 *
	 * @param inform retrieved notification
	 */
	@Override
	protected void handleInform(final ACLMessage inform) {
		final Map<AID, Boolean> announcedAgents = decodeSubscription(inform);

		final Map<AID, Boolean> addedAgents = announcedAgents.entrySet().stream()
				.filter(Map.Entry::getValue)
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

		final Map<AID, Boolean> removedAgents = announcedAgents.entrySet().stream()
				.filter(entry -> !entry.getValue())
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

		if (!addedAgents.isEmpty() || !removedAgents.isEmpty()) {
			facts.put(RULE_STEP, SUBSCRIPTION_HANDLE_AGENTS_RESPONSE_STEP);
			facts.put(SUBSCRIPTION_ADDED_AGENTS, addedAgents);
			facts.put(SUBSCRIPTION_REMOVED_AGENTS, removedAgents);

			final int strategyIdx = selectStrategy.applyAsInt(facts);
			facts.put(STRATEGY_IDX, strategyIdx);

			controller.fire(facts);
			postProcessSubscriptionResponse(facts);
		}
	}

	/**
	 * Method can be optionally overridden in order to perform facts-based actions after handling subscription response
	 * message
	 */
	protected void postProcessSubscriptionResponse(final StrategyFacts facts) {
		// to be overridden if necessary
	}
}
