package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.job.announcing.comparison;

import static org.greencloud.commons.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.enums.rules.RuleType.COMPARE_EXECUTION_PROPOSALS;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.extended.JobWithPrice;
import org.greencloud.gui.agents.scheduler.SchedulerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;

public class CompareProposalsOfJobExecution extends AgentBasicRule<SchedulerAgentProps, SchedulerNode> {

	private static final Logger logger = getLogger(CompareProposalsOfJobExecution.class);

	public CompareProposalsOfJobExecution(final RulesController<SchedulerAgentProps, SchedulerNode> rulesController) {
		super(rulesController);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(COMPARE_EXECUTION_PROPOSALS,
				"rule compares proposals of job execution made by Cloud Network Agent",
				"rule executed when CNAs send job execution proposals");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final JobWithPrice bestProposal = facts.get("BEST_PROPOSAL_CONTENT");
		final JobWithPrice newProposal = facts.get("NEW_PROPOSAL_CONTENT");

		logger.info("Comparing CNA offers using default comparator for job $jobId.");
		facts.put(RESULT, (int) (bestProposal.getPriceForJob() - newProposal.getPriceForJob()));
	}
}
