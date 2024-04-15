package org.greencloud.agentsystem.strategies.deault.rules.server.job.price;

import static java.util.Optional.ofNullable;
import static org.jrba.rulesengine.constants.FactTypeConstants.INPUT_DATA;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.COMPUTE_PRICE_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getJobById;
import static org.greencloud.commons.utils.time.TimeConverter.convertToHourDuration;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.agent.GreenSourceData;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.AgentBasicRule;

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
	public void executeRule(final RuleSetFacts facts) {
		final GreenSourceData data = facts.get(INPUT_DATA);

		final ClientJob job = ofNullable(getJobById(data.getJobId(), agentProps.getServerJobs())).orElseThrow();
		final double computationCost =
				convertToHourDuration(job.getStartTime(), job.getEndTime()) * agentProps.getPricePerHour();

		facts.put(RESULT, data.getPriceForEnergySupply() + computationCost);
	}
}
