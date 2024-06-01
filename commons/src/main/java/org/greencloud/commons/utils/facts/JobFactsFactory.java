package org.greencloud.commons.utils.facts;

import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;

import java.util.List;

import org.greencloud.commons.domain.job.basic.PowerJob;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

/**
 * Factory constructing common rule set facts that contain job/jobs
 */
public class JobFactsFactory {

	/**
	 * Method construct facts that include a list of jobs
	 *
	 * @param index index of a rule set
	 * @param jobs  list of jobs
	 * @return RuleSetFacts
	 */
	public static <T extends PowerJob> RuleSetFacts constructFactsWithJobs(final int index, final List<T> jobs) {
		final RuleSetFacts facts = new RuleSetFacts(index);
		facts.put(JOBS, jobs);
		return facts;
	}

	/**
	 * Method construct facts that include a list of jobs
	 *
	 * @param index index of a rule set
	 * @param job   job
	 * @return RuleSetFacts
	 */
	public static <T extends PowerJob> RuleSetFacts constructFactsWithJob(final int index, final T job) {
		final RuleSetFacts facts = new RuleSetFacts(index);
		facts.put(JOB, job);
		return facts;
	}
}
