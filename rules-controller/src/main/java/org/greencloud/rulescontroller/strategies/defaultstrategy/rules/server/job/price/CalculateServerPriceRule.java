package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.server.job.price;

import static org.greencloud.commons.constants.FactTypeConstants.INPUT_DATA;
import static org.greencloud.commons.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.enums.rules.RuleType.COMPUTE_PRICE_RULE;
import static org.greencloud.commons.utils.time.TimeConverter.convertToHourDuration;
import static org.greencloud.commons.utils.job.JobUtils.getJobById;
import static java.util.Optional.ofNullable;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.agent.GreenSourceData;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.server.ServerNode;

public class CalculateServerPriceRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	public CalculateServerPriceRule(final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(COMPUTE_PRICE_RULE,
				"rule computing job price",
				"rule executed when overall job price is to be computed");
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final GreenSourceData data = facts.get(INPUT_DATA);

		final ClientJob job = ofNullable(getJobById(data.getJobId(), agentProps.getServerJobs())).orElseThrow();
		final double computationCost =
				convertToHourDuration(job.getStartTime(), job.getEndTime()) * agentProps.getPricePerHour();

		facts.put(RESULT, data.getPriceForEnergySupply() + computationCost);
	}
}
