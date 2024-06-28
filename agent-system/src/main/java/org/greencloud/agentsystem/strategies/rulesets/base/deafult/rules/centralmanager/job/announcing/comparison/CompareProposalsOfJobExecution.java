package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.job.announcing.comparison;

import static java.util.Objects.requireNonNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.BEST_PROPOSAL_CONTENT;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.NEW_PROPOSAL_CONTENT;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.CPU;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.COMPARE_EXECUTION_PROPOSALS;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.commons.domain.job.extended.JobWithPrice;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

public class CompareProposalsOfJobExecution extends AgentBasicRule<CentralManagerAgentProps, CMANode> {

	private static final Logger logger = getLogger(CompareProposalsOfJobExecution.class);

	public CompareProposalsOfJobExecution(final RulesController<CentralManagerAgentProps, CMANode> rulesController) {
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
		final JobWithPrice bestProposal = facts.get(BEST_PROPOSAL_CONTENT);
		final JobWithPrice newProposal = facts.get(NEW_PROPOSAL_CONTENT);

		final double bestProposalCPU = requireNonNull(bestProposal.getAvailableResources()).get(CPU)
				.getAmountInCommonUnit();
		final double newProposalCPU = requireNonNull(newProposal.getAvailableResources()).get(CPU)
				.getAmountInCommonUnit();

		logger.info("Comparing RMA offers using default comparator for job $jobId.");
		facts.put(RESULT, (int) (bestProposalCPU - newProposalCPU));
	}

	@Override
	public AgentRule copy() {
		return new CompareProposalsOfJobExecution(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
