package org.greencloud.commons.utils.facts;

import static org.greencloud.commons.constants.EGCSFactTypeConstants.ALLOCATION;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.HANDLE_NEW_JOB_ALLOCATION_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_ALLOCATION_REQUEST_DATA;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_ALLOCATION_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PREPARE_DATA_FOR_JOB_ALLOCATION_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_NEW_JOB_ALLOCATION_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_NEW_JOB_UNSUCCESSFUL_ALLOCATION_RULE;
import static org.greencloud.commons.utils.facts.JobFactsFactory.constructFactsWithJob;
import static org.greencloud.commons.utils.facts.JobFactsFactory.constructFactsWithJobs;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_TYPE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;

import java.util.List;
import java.util.Map;

import org.greencloud.commons.domain.allocation.AllocatedJobs;
import org.greencloud.commons.domain.allocation.AllocationData;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.basic.PowerJob;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

import jade.lang.acl.ACLMessage;

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
		final RuleSetFacts facts = constructFactsWithJobs(index, jobs);
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
		final RuleSetFacts facts = constructFactsWithJobs(index, jobs);
		facts.put(RULE_TYPE, NEW_JOB_ALLOCATION_RULE);

		return facts;
	}

	/**
	 * Method construct facts passed to rules responsible for requesting required data used in allocation
	 *
	 * @param index index of a rule set
	 * @param jobs  jobs that are to be allocated
	 * @return RuleSetFacts
	 */
	public static RuleSetFacts constructFactsForJobsAllocationDataRequest(final int index,
			final List<ClientJob> jobs) {
		final RuleSetFacts facts = new RuleSetFacts(index);
		facts.put(JOBS, jobs);
		facts.put(RULE_TYPE, NEW_JOB_ALLOCATION_REQUEST_DATA);

		return facts;
	}

	/**
	 * Method construct facts passed to rules responsible for initiating allocation.
	 *
	 * @param index          index of a rule set
	 * @param jobs           jobs that are to be allocated
	 * @param allocationData data used in allocation algorithm
	 * @return RuleSetFacts
	 */
	public static RuleSetFacts constructFactsForJobsAllocationPreparation(final int index,
			final List<ClientJob> jobs, final AllocationData allocationData) {
		final RuleSetFacts facts = new RuleSetFacts(index);
		facts.put(JOBS, jobs);
		facts.put(RESULT, allocationData);
		facts.put(RULE_TYPE, PROCESS_NEW_JOB_ALLOCATION_RULE);

		return facts;
	}

	/**
	 * Method construct facts passed to rules responsible for creating response with RMA data
	 *
	 * @param index        index of a rule set
	 * @param job          job based on which data will be prepared
	 * @param message      message to which the agent should respond
	 * @param performative performative of tge message
	 * @return RuleSetFacts
	 */
	public static <T extends PowerJob> RuleSetFacts constructFactsForDataAllocationPreparation(final int index,
			final ACLMessage message, final T job, final int performative) {
		final RuleSetFacts facts = constructFactsWithJob(index, job);
		facts.put(RULE_TYPE, PREPARE_DATA_FOR_JOB_ALLOCATION_RULE);
		facts.put(MESSAGE, message);
		facts.put(MESSAGE_TYPE, performative);

		return facts;
	}

	/**
	 * Method construct facts passed to rules responsible for creating response with RMA data
	 *
	 * @param index        index of a rule set
	 * @param jobs         jobs based on which data will be prepared
	 * @param message      message to which the agent should respond
	 * @param performative performative of tge message
	 * @return RuleSetFacts
	 */
	public static RuleSetFacts constructFactsForDataAllocationPreparation(final int index,
			final ACLMessage message, final AllocatedJobs jobs, final int performative) {
		final RuleSetFacts facts = new RuleSetFacts(index);
		facts.put(JOBS, jobs);
		facts.put(MESSAGE, message);
		facts.put(MESSAGE_TYPE, performative);
		facts.put(RULE_TYPE, PREPARE_DATA_FOR_JOB_ALLOCATION_RULE);

		return facts;
	}

	/**
	 * Method construct facts passed to rules responsible for handling jobs that weren't allocated
	 *
	 * @param index      index of a rule set
	 * @param jobs       all jobs that were supposed to be allocated
	 * @param allocation resulting allocation
	 * @return RuleSetFacts
	 */
	public static RuleSetFacts constructFactsForUnsuccessfulDataAllocationPreparation(final int index,
			final Map<String, List<String>> allocation, final List<ClientJob> jobs) {
		final RuleSetFacts facts = new RuleSetFacts(index);
		facts.put(JOBS, jobs);
		facts.put(ALLOCATION, allocation);
		facts.put(RULE_TYPE, PROCESS_NEW_JOB_UNSUCCESSFUL_ALLOCATION_RULE);

		return facts;
	}
}
