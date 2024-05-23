package org.greencloud.commons.utils.facts;

import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.HANDLE_NEW_JOB_ALLOCATION_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_ALLOCATION_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;

import java.util.List;

import org.greencloud.commons.domain.job.basic.PowerJob;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

/**
 * Factory constructing common rule set facts for job allocation
 */
public class JobAllocationFactsFactory {

	/**
	 * Method construct facts passed to rules responsible for handling jobs allocation
	 *
	 * @param index index of a rule set
	 * @param jobs  jobs that are to be allocated
	 * @return RuleSetFacts
	 */
	public static <T extends PowerJob> RuleSetFacts constructFactsForJobsAllocationHandling(final int index,
			final List<T> jobs) {
		final RuleSetFacts facts = new RuleSetFacts(index);
		facts.put(JOBS, jobs);
		facts.put(RULE_TYPE, HANDLE_NEW_JOB_ALLOCATION_RULE);

		return facts;
	}

	/**
	 * Method construct facts passed to rules responsible for initiating jobs allocation
	 *
	 * @param index index of a rule set
	 * @param jobs  jobs that are to be allocated
	 * @return RuleSetFacts
	 */
	public static <T extends PowerJob> RuleSetFacts constructFactsForJobsAllocationInitiation(final int index,
			final List<T> jobs) {
		final RuleSetFacts facts = new RuleSetFacts(index);
		facts.put(JOBS, jobs);
		facts.put(RULE_TYPE, NEW_JOB_ALLOCATION_RULE);

		return facts;
	}
}
