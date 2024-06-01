package org.greencloud.agentsystem.strategies.deafult.rules.server.job.price;

import static java.util.Optional.ofNullable;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.COMPUTE_PRICE_RULE;
import static org.greencloud.commons.utils.time.TimeConverter.convertToHourDuration;
import static org.jrba.rulesengine.constants.FactTypeConstants.INPUT_DATA;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.agent.GreenSourceData;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

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
		final ClientJob clientJob = ofNullable((ClientJob) facts.get(JOB))
				.filter(job -> agentProps.getServerJobs().containsKey(job))
				.orElseThrow();

		final double computationCost = convertToHourDuration(clientJob.getDuration()) * agentProps.getPricePerHour();
		final double totalCost = ofNullable(facts.get(INPUT_DATA))
				.map(GreenSourceData.class::cast)
				.map(greenSourceData -> computationCost + greenSourceData.getPriceForEnergySupply())
				.orElse(computationCost);

		facts.put(RESULT, totalCost);
	}

	@Override
	public AgentRule copy() {
		return new CalculateServerPriceRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
