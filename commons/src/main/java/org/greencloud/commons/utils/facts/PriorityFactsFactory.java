package org.greencloud.commons.utils.facts;

import static java.util.Optional.ofNullable;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB_PRIORITY_FACTS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.COMPUTE_JOB_PRIORITY_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PRE_EVALUATE_JOB_PRIORITY_RULE;
import static org.greencloud.commons.utils.facts.JobFactsFactory.constructFactsWithJob;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;

import javax.annotation.Nullable;

import org.greencloud.commons.domain.job.basic.PowerJob;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

/**
 * Factory constructing common rule set facts for priority estimation
 */
public class PriorityFactsFactory {

	/**
	 * Method construct facts passed to rules responsible for priority estimation.
	 *
	 * @param index           index of a rule set
	 * @param additionalFacts additional facts used in estimation
	 * @param job             job for which priority is to be estimated
	 * @return RuleSetFacts
	 */
	public static <T extends PowerJob> RuleSetFacts constructFactsForPriorityEstimation(final int index,
			final @Nullable RuleSetFacts additionalFacts, final T job) {
		final RuleSetFacts facts = new RuleSetFacts(index);
		final RuleSetFacts additionalJobFacts = ofNullable(additionalFacts).orElse(new RuleSetFacts(index));
		facts.put(RULE_TYPE, COMPUTE_JOB_PRIORITY_RULE);
		facts.put(JOB, job);
		facts.put(JOB_PRIORITY_FACTS, additionalJobFacts);

		return facts;
	}

	/**
	 * Method construct facts passed to rules responsible for priority pre-evaluation.
	 *
	 * @param index index of a rule set
	 * @param job   job for which priority is to be pre-evaluated
	 * @return RuleSetFacts
	 */
	public static <T extends PowerJob> RuleSetFacts constructFactsForPriorityPreEvaluation(final int index,
			final T job) {
		final RuleSetFacts preprocessingFacts = constructFactsWithJob(index, job);
		preprocessingFacts.put(RULE_TYPE, PRE_EVALUATE_JOB_PRIORITY_RULE);

		return preprocessingFacts;
	}

}
