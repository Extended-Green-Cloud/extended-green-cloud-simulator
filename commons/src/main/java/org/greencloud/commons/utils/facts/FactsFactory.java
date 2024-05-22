package org.greencloud.commons.utils.facts;

import static org.greencloud.commons.constants.EGCSFactTypeConstants.COMPUTE_FINAL_PRICE;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.ADAPTATION_REQUEST_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.FINISH_JOB_EXECUTION_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.ADAPTATION_PARAMS;
import static org.jrba.rulesengine.constants.FactTypeConstants.ADAPTATION_TYPE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;

import org.greencloud.commons.args.adaptation.AdaptationActionParameters;
import org.greencloud.commons.domain.job.basic.PowerJob;
import org.greencloud.commons.domain.job.extended.JobWithStatus;
import org.greencloud.commons.enums.adaptation.AdaptationActionTypeEnum;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

import jade.lang.acl.ACLMessage;

/**
 * Factory constructing common rule set facts
 */
public class FactsFactory {

	/**
	 * Method construct facts passed to rules responsible for handling adaptation
	 *
	 * @param index            index of a rule set
	 * @param adaptationType   type of adaptation action
	 * @param actionParameters adaptation parameters
	 * @return RuleSetFacts
	 */
	public static RuleSetFacts constructFactsForAdaptationRequest(final int index,
			final AdaptationActionTypeEnum adaptationType,
			final AdaptationActionParameters actionParameters) {
		final RuleSetFacts facts = new RuleSetFacts(index);
		facts.put(RULE_TYPE, ADAPTATION_REQUEST_RULE);
		facts.put(ADAPTATION_PARAMS, actionParameters);
		facts.put(ADAPTATION_TYPE, adaptationType);

		return facts;
	}

	/**
	 * Method construct facts passed to rules responsible for job removal
	 *
	 * @param index index of a rule set
	 * @param job   job that is to be removed
	 * @return RuleSetFacts
	 */
	public static <T extends PowerJob> RuleSetFacts constructFactsForJobRemoval(final int index, final T job) {
		final RuleSetFacts jobRemovalFacts = new RuleSetFacts(index);
		jobRemovalFacts.put(RULE_TYPE, FINISH_JOB_EXECUTION_RULE);
		jobRemovalFacts.put(JOB, job);

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
		final RuleSetFacts jobRemoveFacts = new RuleSetFacts(index);
		jobRemoveFacts.put(JOB, job);
		jobRemoveFacts.put(MESSAGE, serverMessage);
		jobRemoveFacts.put(MESSAGE_CONTENT, jobStatus);
		jobRemoveFacts.put(RULE_TYPE, FINISH_JOB_EXECUTION_RULE);
		jobRemoveFacts.put(COMPUTE_FINAL_PRICE, true);

		return jobRemoveFacts;
	}
}
