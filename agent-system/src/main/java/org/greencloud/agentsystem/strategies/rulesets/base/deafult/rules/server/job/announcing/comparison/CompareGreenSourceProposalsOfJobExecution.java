package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.announcing.comparison;

import static java.lang.Math.signum;
import static java.util.Optional.ofNullable;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.BEST_PROPOSAL;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.NEW_PROPOSAL;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.COMPARE_EXECUTION_PROPOSALS;
import static org.greencloud.commons.utils.messaging.MessageComparator.compareMessages;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;

import java.time.temporal.ValueRange;
import java.util.Comparator;
import java.util.concurrent.ConcurrentMap;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.agent.GreenSourceData;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class CompareGreenSourceProposalsOfJobExecution extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final ValueRange MAX_POWER_DIFFERENCE = ValueRange.of(-10, 10);

	public CompareGreenSourceProposalsOfJobExecution(
			final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(COMPARE_EXECUTION_PROPOSALS,
				"rule compares proposals of job execution made by Green Sources",
				"rule executed when Green Sources send job execution proposals");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ACLMessage bestProposalMsg = facts.get(BEST_PROPOSAL);
		final ACLMessage newProposalMsg = facts.get(NEW_PROPOSAL);

		final ConcurrentMap<AID, Integer> greenSourceWeights = agentProps.getWeightsForGreenSourcesMap();

		final int comparisonResult = ofNullable(greenSourceWeights.get(bestProposalMsg.getSender()))
				.filter(bestProposalGs -> greenSourceWeights.containsKey(newProposalMsg.getSender()))
				.map(bestProposalGsWeight -> compareMessages(bestProposalMsg, newProposalMsg, GreenSourceData.class,
						getComparator(greenSourceWeights, bestProposalGsWeight, newProposalMsg)))
				.orElse(0);

		facts.put(RESULT, comparisonResult);
	}

	private Comparator<GreenSourceData> getComparator(final ConcurrentMap<AID, Integer> greenSourceWeights,
			final int weight1, final ACLMessage newProposalMsg) {
		final int weight2 = greenSourceWeights.get(newProposalMsg.getSender());

		return (msg1, msg2) -> {
			double powerDiff = msg1.getAvailablePowerInTime() * weight2 - msg2.getAvailablePowerInTime() * weight1;
			double errorDiff = (msg1.getPowerPredictionError() - msg2.getPowerPredictionError());
			int priceDiff = (int) (msg1.getPriceForEnergySupply() - msg2.getPriceForEnergySupply());

			return (int) (errorDiff != 0 ? signum(errorDiff) :
					MAX_POWER_DIFFERENCE.isValidValue((long) powerDiff) ? priceDiff : signum(powerDiff));
		};
	}

	@Override
	public AgentRule copy() {
		return new CompareGreenSourceProposalsOfJobExecution(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
