package org.greencloud.rulescontroller.ruleset;

import static java.util.Objects.nonNull;
import static org.greencloud.commons.enums.rules.RuleType.COMPARE_EXECUTION_PROPOSALS;
import static org.greencloud.rulescontroller.rest.RuleSetRestApi.getAvailableRuleSets;
import static org.greencloud.rulescontroller.rule.AgentRuleType.BASIC;
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
import org.greencloud.commons.args.agent.AgentType;
import org.greencloud.commons.enums.rules.RuleStepType;
import org.greencloud.gui.agents.AgentNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.rest.domain.RuleRest;
import org.greencloud.rulescontroller.rest.domain.RuleSetRest;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.AgentRuleType;
import org.greencloud.rulescontroller.rule.combined.AgentCombinedRule;
import org.slf4j.Logger;

/**
 * Class storing methods used to construct rule sets
 */
@SuppressWarnings("unchecked")
public class RuleSetConstructor {

	public static final List<AgentRuleType> stepBasedRules = List.of(CFP, LISTENER, LISTENER_SINGLE, PERIODIC, REQUEST,
			SCHEDULED, SEARCH, SUBSCRIPTION, PROPOSAL);
	private static final Logger logger = getLogger(RuleSetConstructor.class);

	/**
	 * Method constructs rule set for given type
	 *
	 * @param type       type of rule set
	 * @param controller controller which runs given rule set
	 * @return RuleSet
	 */
	public static <E extends AgentProps, T extends AgentNode<E>> RuleSet constructRuleSet(
			final String type, final RulesController<E, T> controller) {
		return getRuleSetTemplate(type, controller);
	}

	/**
	 * Method constructs rule set for given type
	 *
	 * @param baseType   type of base rule set
	 * @param type       type of rule set
	 * @param controller controller which runs given rule set
	 * @return RuleSet
	 */
	public static <E extends AgentProps, T extends AgentNode<E>> RuleSet constructRuleSetForType(
			final String baseType, final String type, final RulesController<E, T> controller) {
		final RuleSet baseRuleSet = getRuleSetTemplate(baseType, controller);
		final RuleSet modifications = getRuleSetTemplate(type, controller);
		return constructModifiedRuleSetForType(baseRuleSet, modifications);
	}

	/**
	 * Method creates rule set used when client specifies individual comparison preferences
	 *
	 * @param instructions instructions obtained by the client
	 * @param ruleSetName  name of the created rule set
	 * @param log          log message displayed when performing comparison
	 * @param jobId        job identifier
	 * @return rule set with client comparison
	 */
	public static RuleSetRest constructRuleSetForCustomClientComparison(final String instructions,
			final String ruleSetName, final String log,
			final String jobId, final AgentType agentType) {
		final RuleRest compareProposalsRule = createComparatorRule(instructions, log, jobId, agentType);

		final RuleSetRest ruleSetRest = new RuleSetRest();
		ruleSetRest.setName(ruleSetName);
		ruleSetRest.setRules(new ArrayList<>(List.of(compareProposalsRule)));

		return ruleSetRest;
	}

	private static RuleRest createComparatorRule(final String instructions, final String log, final String jobId,
			final AgentType agentType) {
		final RuleRest handleProposalsRule = new RuleRest();
		handleProposalsRule.setAgentRuleType(BASIC);
		handleProposalsRule.setAgentType(agentType.name());
		handleProposalsRule.setType(COMPARE_EXECUTION_PROPOSALS);
		handleProposalsRule.setName(
				"compare proposals from servers according to custom instructions of job" + jobId);
		handleProposalsRule.setDescription(
				"compare proposals from servers according to custom instructions of job" + jobId);
		handleProposalsRule.setImports(List.of(
				"import org.greencloud.commons.constants.FactTypeConstants;",
				"import org.greencloud.commons.domain.job.extended.JobWithPrice;"
		));
		handleProposalsRule.setExecute("""
				bestP = facts.get("BEST_PROPOSAL_CONTENT");
				newP = facts.get("NEW_PROPOSAL_CONTENT");
				def computeServerComparison(bestProposal, newProposal) { $instruction }
				MDC.put(LoggingConstants.MDC_JOB_ID, $jobId);
				MDC.put(LoggingConstants.MDC_RULE_SET_ID, LoggingConstants.getIdxFromFacts.apply(facts));
				logger.info("$log (job: $jobId).");
				finalResult = computeServerComparison(bestP, newP);
				intResult = 
				facts.put(FactTypeConstants.RESULT, finalResult);
				"""
				.replace("$instruction", instructions)
				.replace("$log", log)
				.replace("$jobId", jobId));
		return handleProposalsRule;
	}

	/**
	 * Method constructs modified rule set (modifications are applied to default rule set)
	 *
	 * @param baseRuleSet     base rule set
	 * @param modifications rule set which modifications are to be applied
	 *
	 * @return RuleSet
	 */
	public static RuleSet constructModifiedRuleSetForType(
			final RuleSet baseRuleSet, RuleSet modifications) {
		if (nonNull(modifications) && nonNull(baseRuleSet)) {
			final RuleSet baseRules = new RuleSet(baseRuleSet);
			final List<String> modificationsTypes = new ArrayList<>(modifications.getAgentRules().stream()
					.map(AgentRule::getRuleType)
					.toList());
			baseRules.setName(modifications.getName());

			if (!modificationsTypes.isEmpty()) {
				final List<AgentRule> modifiableRules = baseRules.getAgentRules().stream()
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

				baseRules.getAgentRules()
						.removeIf(agentRule -> modificationsTypes.contains(agentRule.getRuleType()));
				baseRules.getAgentRules().addAll(remainingModifications);
			}
			return baseRules;
		}
		return baseRuleSet;
	}

	public static <E extends AgentProps, T extends AgentNode<E>> RuleSet getRuleSetTemplate(
			final String typeModifier, final RulesController<E, T> controller) {
		if (getAvailableRuleSets().containsKey(typeModifier)) {
			final RuleSet ruleSetTemplate = getAvailableRuleSets().get(typeModifier);
			return new RuleSet(ruleSetTemplate, controller);
		} else {
			logger.info("Rule set {} not found!", typeModifier);
			return null;
		}
	}

	private static List<AgentRule> performModificationOfCombinedRules(final List<AgentRule> originalRules,
			final RuleSet modifications, final List<String> modificationsTypes) {
		return originalRules.stream()
				.filter(agentRule -> agentRule.getAgentRuleType().equals(COMBINED))
				.map(AgentCombinedRule.class::cast)
				.map(agentRule -> modifyCombinedRule(agentRule, modifications, modificationsTypes))
				.flatMap(Collection::stream)
				.toList();
	}

	private static List<AgentRule> performModificationOfStepBasedRules(final List<AgentRule> originalRules,
			final RuleSet modifications, final List<String> modificationsTypes) {
		return originalRules.stream()
				.filter(agentRule -> stepBasedRules.contains(agentRule.getAgentRuleType()))
				.map(AgentBasicRule.class::cast)
				.map(agentRule -> modifyStepBasedRule(agentRule, modifications, modificationsTypes))
				.flatMap(Collection::stream)
				.toList();
	}

	private static <E extends AgentProps, T extends AgentNode<E>> List<AgentRule> modifyStepBasedRule(
			final AgentBasicRule<E, T> stepBasedRule, final RuleSet modifications,
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
			final AgentCombinedRule<E, T> combinedRule, final RuleSet modifications,
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
