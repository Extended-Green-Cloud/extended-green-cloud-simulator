package org.greencloud.rulescontroller.strategies.weatherdropstrategy.rules.server.newjob.price;

import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.enums.rules.RuleType.COMPUTE_PRICE_RULE;
import static org.greencloud.commons.utils.time.TimeConverter.convertToHourDuration;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.server.ServerNode;

public class CalculateServerPriceWeatherDropRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	public CalculateServerPriceWeatherDropRule(final RulesController<ServerAgentProps, ServerNode> rulesController) {
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
		final ClientJob job = facts.get(JOB);
		facts.put(RESULT, convertToHourDuration(job.getStartTime(), job.getEndTime()) * agentProps.getPricePerHour());
	}
}
