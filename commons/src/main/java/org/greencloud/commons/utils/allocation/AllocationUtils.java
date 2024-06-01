package org.greencloud.commons.utils.allocation;

import static java.lang.String.valueOf;
import static java.util.Optional.ofNullable;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB_ACCEPTED;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB_REFUSED;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_AGENT_NAME;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.greencloud.commons.domain.allocation.AllocatedJobs;
import org.greencloud.commons.domain.job.basic.ClientJobWithServer;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

/**
 * Class with supporting allocation methods
 */
public class AllocationUtils {

	private static final Logger logger = getLogger(AllocationUtils.class);

	/**
	 * Method initiates the verification of the jobs allocated for execution.
	 *
	 * @param facts               set of facts passed to the rule
	 * @param index               index of the rule set
	 * @param agentName           name of the agent for which jobs were allocated
	 * @param performVerification function responsible for the verification of jobs execution possibility
	 */
	public static void verifyJobsForAllocation(final RuleSetFacts facts, final int index, final String agentName,
			final Function<ClientJobWithServer, RuleSetFacts> performVerification,
			final BiConsumer<List<ClientJobWithServer>, List<ClientJobWithServer>> processJobsExecutionDecision) {
		final AllocatedJobs allocation = facts.get(MESSAGE_CONTENT);

		final List<ClientJobWithServer> acceptedJobs = new ArrayList<>();
		final List<ClientJobWithServer> refusedJobs = new ArrayList<>();

		allocation.getAllocationJobs().forEach(
				job -> evaluateJobAcceptance(agentName, job, index, acceptedJobs, refusedJobs, performVerification));
		processJobsExecutionDecision.accept(acceptedJobs, refusedJobs);
	}

	private static void evaluateJobAcceptance(final String agentName, final ClientJobWithServer job,
			final int ruleSetIdx,
			final List<ClientJobWithServer> acceptedJobs,
			final List<ClientJobWithServer> refusedJobs,
			final Function<ClientJobWithServer, RuleSetFacts> performVerification) {
		MDC.put(MDC_AGENT_NAME, agentName);
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf(ruleSetIdx));
		logger.info("Evaluating available resources for job {}!", job.getJobId());

		final RuleSetFacts verifierFacts = performVerification.apply(job);

		ofNullable(verifierFacts.get(JOB_ACCEPTED)).ifPresent(acceptedJob -> addAcceptedJob(acceptedJob, acceptedJobs));
		ofNullable(verifierFacts.get(JOB_REFUSED)).ifPresent(refusedJob -> addRefusedJob(refusedJob, refusedJobs));
	}

	private static void addAcceptedJob(final Object job, final List<ClientJobWithServer> acceptedJobs) {
		acceptedJobs.add((ClientJobWithServer) job);
	}

	private static void addRefusedJob(final Object job, final List<ClientJobWithServer> acceptedJobs) {
		acceptedJobs.add((ClientJobWithServer) job);
	}
}
