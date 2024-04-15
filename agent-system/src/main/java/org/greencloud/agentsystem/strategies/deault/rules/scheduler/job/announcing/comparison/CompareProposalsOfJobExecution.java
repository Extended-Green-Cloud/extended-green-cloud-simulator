package org.greencloud.agentsystem.strategies.deault.rules.scheduler.job.announcing.comparison;

import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.COMPARE_EXECUTION_PROPOSALS;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.greencloud.commons.domain.job.extended.JobWithPrice;
import org.greencloud.gui.agents.scheduler.SchedulerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.slf4j.Logger;

public class CompareProposalsOfJobExecution extends AgentBasicRule<SchedulerAgentProps, SchedulerNode> {

	private static final Logger logger = getLogger(CompareProposalsOfJobExecution.class);

	public CompareProposalsOfJobExecution(final RulesController<SchedulerAgentProps, SchedulerNode> rulesController) {
		super(rulesController);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(COMPARE_EXECUTION_PROPOSALS,
				"rule compares proposals of job execution made by Regional Manager Agent",
				"rule executed when RMAs send job execution proposals");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final JobWithPrice bestProposal = facts.get("BEST_PROPOSAL_CONTENT");
		final JobWithPrice newProposal = facts.get("NEW_PROPOSAL_CONTENT");

		logger.info("Comparing RMA offers using default comparator for job $jobId.");
		facts.put(RESULT, (int) (bestProposal.getPriceForJob() - newProposal.getPriceForJob()));
	}
}
