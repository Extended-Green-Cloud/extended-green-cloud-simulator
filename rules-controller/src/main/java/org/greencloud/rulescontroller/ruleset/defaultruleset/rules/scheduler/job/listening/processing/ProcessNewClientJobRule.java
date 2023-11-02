package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.listening.processing;

import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.CREATED;
import static org.greencloud.commons.enums.rules.RuleType.COMPARE_EXECUTION_PROPOSALS;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLE_JOB_DUPLICATE_RULE;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.SCHEDULED_JOB_ID;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static org.greencloud.rulescontroller.rule.AgentRuleType.BASIC;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.List;

import org.greencloud.commons.args.agent.AgentType;
import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.scheduler.SchedulerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rest.domain.RuleRest;
import org.greencloud.rulescontroller.rest.domain.RuleSetRest;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.rulescontroller.ruleset.RuleSet;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ProcessNewClientJobRule extends AgentBasicRule<SchedulerAgentProps, SchedulerNode> {

	private static final Logger logger = getLogger(ProcessNewClientJobRule.class);

	public ProcessNewClientJobRule(final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
		super(controller, 1);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_HANDLER_RULE, NEW_JOB_RECEIVER_HANDLE_JOB_DUPLICATE_RULE,
				"handles new client job",
				"rule runs when new client job was received");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(MESSAGE_CONTENT);
		return agentProps.getJobsToBeExecuted().offer(job);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(MESSAGE_CONTENT);
		int newStrategyIdx = facts.get(RULE_SET_IDX);

		if (nonNull(job.getSelectionPreference())) {
			newStrategyIdx = createRuleSetForCustomClientComparison(job.getSelectionPreference(),
					job.getJobId());
		}
		agentProps.addJob(job, newStrategyIdx, CREATED);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) newStrategyIdx));
		logger.info("Job {} has been successfully added to job scheduling queue", job.getJobId());

		agentNode.updateScheduledJobQueue(agentProps);
		agent.send(prepareJobStatusMessageForClient(job, SCHEDULED_JOB_ID, newStrategyIdx));
	}

	private int createRuleSetForCustomClientComparison(final String instructions, final String jobId) {
		final String ruleSetName = "CUSTOM_CLIENT_COMPARATOR_" + jobId;
		final RuleRest compareProposalsRule = createComparatorRule(instructions, jobId);

		final RuleSetRest ruleSetRest = new RuleSetRest();
		ruleSetRest.setName(ruleSetName);
		ruleSetRest.setRules(new ArrayList<>(List.of(compareProposalsRule)));

		final int newRuleSetIdx = controller.getLatestRuleSet().get() + 1;
		MDC.put(MDC_JOB_ID, jobId);
		MDC.put(MDC_RULE_SET_ID, valueOf(newRuleSetIdx));
		logger.info("Client with job {} requested to use custom server comparison. Adding rule set {}", jobId,
				ruleSetName);

		final RuleSet modifications = new RuleSet(ruleSetRest);
		controller.addModifiedRuleSetFromCurrent(modifications, newRuleSetIdx);
		return newRuleSetIdx;
	}

	private RuleRest createComparatorRule(final String instructions, final String jobId) {
		final RuleRest handleProposalsRule = new RuleRest();
		handleProposalsRule.setAgentRuleType(BASIC);
		handleProposalsRule.setAgentType(AgentType.SCHEDULER.name());
		handleProposalsRule.setType(COMPARE_EXECUTION_PROPOSALS);
		handleProposalsRule.setName(
				"compare proposals from servers according to custom instructions of client" + jobId);
		handleProposalsRule.setDescription(
				"compare proposals from servers according to custom instructions of client" + jobId);
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
				logger.info("Comparing CNA offers using custom comparator for job $jobId.");
				finalResult = computeServerComparison(bestP, newP);
				facts.put(FactTypeConstants.RESULT, finalResult);
				""".replace("$instruction", instructions).replace("$jobId", jobId));
		return handleProposalsRule;
	}
}