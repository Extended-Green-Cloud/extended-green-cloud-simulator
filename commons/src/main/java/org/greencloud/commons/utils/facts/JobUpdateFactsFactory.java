package org.greencloud.commons.utils.facts;

import static org.greencloud.commons.constants.EGCSFactTypeConstants.COMPUTE_FINAL_PRICE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_ADD_JOB_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_VERIFICATION_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_VERIFY_DEADLINE_RULE;
import static org.greencloud.commons.utils.facts.JobFactsFactory.constructFactsWithJob;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;

import org.greencloud.commons.domain.job.basic.PowerJob;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

import jade.lang.acl.ACLMessage;

/**
 * Factory constructing common rule set facts for job updates
 */
public class JobUpdateFactsFactory {
	/**
	 * Method construct facts passed to rules responsible for job removal
	 *
	 * @param index index of a rule set
	 * @param job   job that is to be removed
	 * @return RuleSetFacts
	 */
	public static <T extends PowerJob> RuleSetFacts constructFactsForJobRemoval(final int index, final T job) {
		final RuleSetFacts jobRemovalFacts = constructFactsWithJob(index, job);
		jobRemovalFacts.put(RULE_TYPE, FINISH_JOB_EXECUTION_RULE);

		return jobRemovalFacts;
	}

	/**
	 * Method construct facts passed to rules responsible for job removal
	 *
	 * @param index        index of a rule set
	 * @param job          job that is to be removed
	 * @param computePrice flag indicating if final price should be computed
	 * @return RuleSetFacts
	 */
	public static <T extends PowerJob> RuleSetFacts constructFactsForJobRemovalWithPrice(final int index, final T job,
			final boolean computePrice) {
		final RuleSetFacts jobRemovalFacts = constructFactsWithJob(index, job);
		jobRemovalFacts.put(RULE_TYPE, FINISH_JOB_EXECUTION_RULE);
		jobRemovalFacts.put(COMPUTE_FINAL_PRICE, computePrice);

		return jobRemovalFacts;
	}

	/**
	 * Method construct facts passed to rules responsible for job removal
	 *
	 * @param index             index of a rule set
	 * @param job               job that is to be removed
	 * @param informAboutFinish flag indicating if the information about job execution finish should be sent
	 * @return RuleSetFacts
	 */
	public static <T extends PowerJob> RuleSetFacts constructFactsForJobRemovalWithFinishUpdate(final int index,
			final T job, final boolean informAboutFinish) {
		final RuleSetFacts jobRemovalFacts = constructFactsWithJob(index, job);
		jobRemovalFacts.put(RULE_TYPE, FINISH_JOB_EXECUTION_RULE);
		jobRemovalFacts.put(COMPUTE_FINAL_PRICE, informAboutFinish);

		return jobRemovalFacts;
	}

	/**
	 * Method construct facts passed to rules responsible for job execution finish in Green Energy Source
	 *
	 * @param index         index of a rule set
	 * @param job           job that is to be finished
	 * @param jobStatus     status received from the server
	 * @param serverMessage message received from the server
	 * @return RuleSetFacts
	 */
	public static <T extends PowerJob> RuleSetFacts constructFactsForJobRemovalGS(final int index, final T job,
			final JobWithStatus jobStatus, final ACLMessage serverMessage) {
		final RuleSetFacts jobRemoveFacts = constructFactsForJobRemovalWithPrice(index, job, true);
		jobRemoveFacts.put(MESSAGE, serverMessage);
		jobRemoveFacts.put(MESSAGE_CONTENT, jobStatus);

		return jobRemoveFacts;
	}

	/**
	 * Method construct facts passed to rules responsible for job deadline verification
	 *
	 * @param index index of a rule set
	 * @param job   job which time frames are to be verified
	 * @return RuleSetFacts
	 */
	public static <T extends PowerJob> RuleSetFacts constructFactsForJobDeadlineVerification(final int index,
			final T job) {
		final RuleSetFacts timeFrameFacts = constructFactsWithJob(index, job);
		timeFrameFacts.put(RULE_TYPE, NEW_JOB_VERIFY_DEADLINE_RULE);

		return timeFrameFacts;
	}

	/**
	 * Method construct facts passed to rules responsible for job adding
	 *
	 * @param index index of a rule set
	 * @param job   job that is to be added
	 * @return RuleSetFacts
	 */
	public static <T extends PowerJob> RuleSetFacts constructFactsForAddingNewJob(final int index, final T job) {
		final RuleSetFacts addingJobFacts = constructFactsWithJob(index, job);
		addingJobFacts.put(RULE_TYPE, NEW_JOB_ADD_JOB_RULE);

		return addingJobFacts;
	}

	/**
	 * Method construct facts passed to rules responsible for job execution possibilities verification
	 *
	 * @param index index of a rule set
	 * @param job   new job which execution possibilities are to be verified
	 * @return RuleSetFacts
	 */
	public static <T extends PowerJob> RuleSetFacts constructFactsForJobVerification(final int index,
			final T job, final ACLMessage allocatorMessage) {
		final RuleSetFacts facts = constructFactsWithJob(index, job);
		facts.put(RULE_TYPE, NEW_JOB_VERIFICATION_RULE);
		facts.put(MESSAGE, allocatorMessage);

		return facts;
	}
}
