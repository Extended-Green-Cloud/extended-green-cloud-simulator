package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.cloudnetwork.job.announcing.comparison;

import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.rules.RuleType.COMPARE_EXECUTION_PROPOSALS;
import static org.slf4j.LoggerFactory.getLogger;

import java.time.temporal.ValueRange;

import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.domain.agent.ServerData;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.cloudnetwork.CloudNetworkNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class CompareServersProposalsOfJobExecution extends AgentBasicRule<CloudNetworkAgentProps, CloudNetworkNode> {

	private static final Logger logger = getLogger(CompareServersProposalsOfJobExecution.class);
	private static final ValueRange MAX_POWER_DIFFERENCE = ValueRange.of(-10, 10);

	public CompareServersProposalsOfJobExecution(
			final RulesController<CloudNetworkAgentProps, CloudNetworkNode> rulesController) {
		super(rulesController);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(COMPARE_EXECUTION_PROPOSALS,
				"rule compares proposals of job execution made by Servers",
				"rule executed when Servers send job execution proposals");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		final ACLMessage bestProposalMsg = facts.get("BEST_PROPOSAL");
		final ACLMessage newProposalMsg = facts.get("NEW_PROPOSAL");

		final ServerData bestProposal = facts.get("BEST_PROPOSAL_CONTENT");
		final ServerData newProposal = facts.get("NEW_PROPOSAL_CONTENT");

		final int weight1 = agentProps.getWeightsForServersMap().get(bestProposalMsg.getSender());
		final int weight2 = agentProps.getWeightsForServersMap().get(newProposalMsg.getSender());

		final double powerDiff =
				(bestProposal.getPowerConsumption() * weight2) - (newProposal.getPowerConsumption() * weight1);
		final double priceDiff = ((bestProposal.getPriceForJob() * 1 / weight1) - (newProposal.getPriceForJob() * 1
				/ weight2));

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Comparing Servers offers using default comparator.");
		final int comparisonResult = MAX_POWER_DIFFERENCE.isValidIntValue((int) powerDiff) ?
				(int) priceDiff :
				(int) powerDiff;
		facts.put(RESULT, comparisonResult);
	}
}
