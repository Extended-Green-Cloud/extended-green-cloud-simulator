package org.greencloud.commons.utils.rules;

import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.COMPARE_EXECUTION_PROPOSALS;
import static org.jrba.rulesengine.types.ruletype.AgentRuleTypeEnum.BASIC;

import java.util.ArrayList;
import java.util.List;

import org.jrba.agentmodel.types.AgentType;
import org.jrba.rulesengine.rest.domain.RuleRest;
import org.jrba.rulesengine.rest.domain.RuleSetRest;

/**
 * Class with methods containing common rule sets constructors.
 */
public class CustomRulesConstructor {

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
			final String ruleSetName, final String log, final String jobId, final AgentType agentType) {
		RuleRest compareProposalsRule = createComparatorRule(instructions, log, jobId, agentType);
		RuleSetRest ruleSetRest = new RuleSetRest();
		ruleSetRest.setName(ruleSetName);
		ruleSetRest.setRules(new ArrayList<>(List.of(compareProposalsRule)));
		return ruleSetRest;
	}

	private static RuleRest createComparatorRule(final String instructions, final String log, final String jobId,
			final AgentType agentType) {
		final RuleRest handleProposalsRule = new RuleRest();
		handleProposalsRule.setAgentRuleType(BASIC.getType());
		handleProposalsRule.setAgentType(agentType.getName());
		handleProposalsRule.setType(COMPARE_EXECUTION_PROPOSALS);
		handleProposalsRule.setName(
				"compare proposals from servers according to custom instructions of job" + jobId);
		handleProposalsRule.setDescription(
				"compare proposals from servers according to custom instructions of job" + jobId);
		handleProposalsRule.setImports(List.of(
				"import org.jrba.rulesengine.constants.FactTypeConstants;",
				"import org.greencloud.commons.domain.job.extended.JobWithPrice;",
				"import org.greencloud.commons.constants.EGCSFactTypeConstants;"
		));
		handleProposalsRule.setExecute("""
				bestP = facts.get(EGCSFactTypeConstants.BEST_PROPOSAL_CONTENT);
				newP = facts.get(EGCSFactTypeConstants.NEW_PROPOSAL_CONTENT);
				def computeServerComparison(bestProposal, newProposal) { $instruction }
				MDC.put(LoggingConstants.MDC_JOB_ID, $jobId);
				MDC.put(LoggingConstants.MDC_RULE_SET_ID, LoggingConstants.getIdxFromFacts.apply(facts));
				logger.info("$log (job: $jobId).");
				finalResult = computeServerComparison(bestP, newP);
				intResult = facts.put(FactTypeConstants.RESULT, finalResult);
				"""
				.replace("$instruction", instructions)
				.replace("$log", log)
				.replace("$jobId", jobId));
		return handleProposalsRule;
	}
}
