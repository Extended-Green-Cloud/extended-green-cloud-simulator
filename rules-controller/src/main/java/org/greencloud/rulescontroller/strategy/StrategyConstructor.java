package org.greencloud.rulescontroller.strategy;

import static java.util.Objects.nonNull;
import static org.greencloud.rulescontroller.rest.StrategyRestApi.getAvailableStrategies;
import static org.greencloud.rulescontroller.rule.AgentRuleType.CFP;
import static org.greencloud.rulescontroller.rule.AgentRuleType.COMBINED;
import static org.greencloud.rulescontroller.rule.AgentRuleType.LISTENER;
import static org.greencloud.rulescontroller.rule.AgentRuleType.LISTENER_SINGLE;
import static org.greencloud.rulescontroller.rule.AgentRuleType.PERIODIC;
import static org.greencloud.rulescontroller.rule.AgentRuleType.PROPOSAL;
import static org.greencloud.rulescontroller.rule.AgentRuleType.REQUEST;
import static org.greencloud.rulescontroller.rule.AgentRuleType.SCHEDULED;
import static org.greencloud.rulescontroller.rule.AgentRuleType.SEARCH;
import static org.greencloud.rulescontroller.rule.AgentRuleType.SUBSCRIPTION;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.enums.rules.RuleStepType;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.AgentRuleType;
import org.greencloud.rulescontroller.rule.combined.AgentCombinedRule;
import org.slf4j.Logger;

import com.gui.agents.AgentNode;

/**
 * Class storing methods used to construct strategies
 */
@SuppressWarnings("unchecked")
public class StrategyConstructor {

	public static final List<AgentRuleType> stepBasedRules = List.of(CFP, LISTENER, LISTENER_SINGLE, PERIODIC, REQUEST,
			SCHEDULED, SEARCH, SUBSCRIPTION, PROPOSAL);
	private static final Logger logger = getLogger(StrategyConstructor.class);

	/**
	 * Method constructs strategy for given type
	 *
	 * @param type       type of strategy
	 * @param controller controller which runs given strategy
	 * @return Strategy
	 */
	public static <E extends AgentProps, T extends AgentNode<E>> Strategy constructStrategy(
			final String type, final RulesController<E, T> controller) {
		return getStrategyTemplate(type, controller);
	}

	/**
	 * Method constructs strategy for given type
	 *
	 * @param baseType   type of base strategy
	 * @param type       type of strategy
	 * @param controller controller which runs given strategy
	 * @return Strategy
	 */
	public static <E extends AgentProps, T extends AgentNode<E>> Strategy constructStrategyForType(
			final String baseType, final String type, final RulesController<E, T> controller) {
		return constructModifiedStrategyForType(baseType, type, controller);
	}

	/**
	 * Method constructs modified strategy (modifications are applied to default strategy)
	 *
	 * @param baseType     type of base strategy
	 * @param typeModifier type of strategy which modifications are to be applied
	 * @param controller   controller which runs given strategy
	 * @return Strategy
	 */
	public static <E extends AgentProps, T extends AgentNode<E>> Strategy constructModifiedStrategyForType(
			final String baseType, final String typeModifier, final RulesController<E, T> controller) {
		final Strategy baseStrategy = getStrategyTemplate(baseType, controller);
		final Strategy modifications = getStrategyTemplate(typeModifier, controller);

		if (nonNull(modifications) && nonNull(baseStrategy)) {
			final List<String> modificationsTypes = new ArrayList<>(modifications.getAgentRules().stream()
					.map(AgentRule::getRuleType)
					.toList());
			baseStrategy.setName(modifications.getName());

			if (!modificationsTypes.isEmpty()) {
				final List<AgentRule> modifiableRules = baseStrategy.getAgentRules().stream()
						.filter(agentRule -> modificationsTypes.contains(agentRule.getRuleType()))
						.toList();

				final List<AgentRule> usedModificationsCombined =
						performModificationOfCombinedRules(modifiableRules, modifications, modificationsTypes);
				final List<AgentRule> usedModificationsStepBased =
						performModificationOfStepBasedRules(modifiableRules, modifications, modificationsTypes);
				final List<AgentRule> remainingModifications = modifications.getAgentRules().stream()
						.filter(modification -> !usedModificationsCombined.contains(modification)
								&& !usedModificationsStepBased.contains(modification))
						.toList();

				baseStrategy.getAgentRules()
						.removeIf(agentRule -> modificationsTypes.contains(agentRule.getRuleType()));
				baseStrategy.getAgentRules().addAll(remainingModifications);
			}
		}
		return baseStrategy;
	}

	private static <E extends AgentProps, T extends AgentNode<E>> Strategy getStrategyTemplate(
			final String typeModifier, final RulesController<E, T> controller) {
		if (getAvailableStrategies().containsKey(typeModifier)) {
			final Strategy strategyTemplate = getAvailableStrategies().get(typeModifier);
			return new Strategy(strategyTemplate, controller);
		} else {
			logger.info("Strategy {} not found!", typeModifier);
			return null;
		}
	}

	private static List<AgentRule> performModificationOfCombinedRules(final List<AgentRule> originalRules,
			final Strategy modifications, final List<String> modificationsTypes) {
		return originalRules.stream()
				.filter(agentRule -> agentRule.getAgentRuleType().equals(COMBINED))
				.map(AgentCombinedRule.class::cast)
				.map(agentRule -> modifyCombinedRule(agentRule, modifications, modificationsTypes))
				.flatMap(Collection::stream)
				.toList();
	}

	private static List<AgentRule> performModificationOfStepBasedRules(final List<AgentRule> originalRules,
			final Strategy modifications, final List<String> modificationsTypes) {
		return originalRules.stream()
				.filter(agentRule -> stepBasedRules.contains(agentRule.getAgentRuleType()))
				.map(AgentBasicRule.class::cast)
				.map(agentRule -> modifyStepBasedRule(agentRule, modifications, modificationsTypes))
				.flatMap(Collection::stream)
				.toList();
	}

	private static <E extends AgentProps, T extends AgentNode<E>> List<AgentRule> modifyStepBasedRule(
			final AgentBasicRule<E, T> stepBasedRule, final Strategy modifications,
			final List<String> modificationsTypes) {

		final List<RuleStepType> stepRules = stepBasedRule.getRules().stream().map(AgentRule::getStepType).toList();
		final List<AgentRule> applicableModifications = modifications.getAgentRules().stream()
				.filter(rule -> stepRules.contains(rule.getStepType()))
				.toList();
		final List<RuleStepType> consideredTypes = applicableModifications.stream().map(AgentRule::getStepType)
				.toList();
		consideredTypes.forEach(type -> modificationsTypes.remove(stepBasedRule.getRuleType()));

		if (!applicableModifications.isEmpty()) {
			stepBasedRule.getRules().removeIf(stepRule -> consideredTypes.contains(stepRule.getStepType()));
			stepBasedRule.getRules().addAll(applicableModifications);

			return applicableModifications;
		}
		return new ArrayList<>();
	}

	private static <E extends AgentProps, T extends AgentNode<E>> List<AgentRule> modifyCombinedRule(
			final AgentCombinedRule<E, T> combinedRule, final Strategy modifications,
			final List<String> modificationsTypes) {

		final List<String> subRules = combinedRule.getNestedRules();
		final List<AgentRule> applicableModifications = modifications.getAgentRules().stream()
				.filter(rule -> subRules.contains(rule.getSubRuleType()))
				.toList();
		final List<String> consideredTypes = applicableModifications.stream().map(AgentRule::getSubRuleType)
				.toList();
		consideredTypes.forEach(type -> modificationsTypes.remove(combinedRule.getRuleType()));

		if (!applicableModifications.isEmpty()) {
			combinedRule.getRulesToCombine()
					.removeIf(subRule -> consideredTypes.contains(subRule.getSubRuleType()));
			combinedRule.getRulesToCombine().addAll(applicableModifications);

			return applicableModifications;
		}
		return new ArrayList<>();
	}
}
