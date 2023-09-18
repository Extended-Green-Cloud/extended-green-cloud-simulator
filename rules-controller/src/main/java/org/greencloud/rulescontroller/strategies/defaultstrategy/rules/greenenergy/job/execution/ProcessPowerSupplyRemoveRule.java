package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.greenenergy.job.execution;

import static org.greencloud.commons.constants.FactTypeConstants.AGENT;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_SERVER_DISCONNECTION_RULE;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateRequest;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.commons.domain.facts.StrategyFacts;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.basic.ServerJob;
import com.gui.agents.greenenergy.GreenEnergyNode;

import jade.core.AID;

public class ProcessPowerSupplyRemoveRule extends AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	public ProcessPowerSupplyRemoveRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> rulesController) {
		super(rulesController);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(FINISH_JOB_EXECUTION_RULE,
				"handle finish job power supply",
				"rule executes handler which completes job power supply");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		final ServerJob job = facts.get(JOB);
		agentProps.removeJob(job);

		return agentProps.getGreenSourceDisconnection().isBeingDisconnectedFromServer();
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final AID server = agentProps.getGreenSourceDisconnection().getServerToBeDisconnected();
		final boolean isLastJobRemoved = agentProps.getServerJobs().keySet().stream()
				.noneMatch(serverJob -> serverJob.getServer().equals(server));

		if (isLastJobRemoved) {
			final StrategyFacts disconnectionFacts = new StrategyFacts(facts.get(STRATEGY_IDX));
			disconnectionFacts.put(AGENT, server);
			disconnectionFacts.put(MESSAGE, agentProps.getGreenSourceDisconnection().getOriginalAdaptationMessage());

			agent.addBehaviour(
					InitiateRequest.create(agent, disconnectionFacts, PROCESS_SERVER_DISCONNECTION_RULE, controller));
		}
	}
}
