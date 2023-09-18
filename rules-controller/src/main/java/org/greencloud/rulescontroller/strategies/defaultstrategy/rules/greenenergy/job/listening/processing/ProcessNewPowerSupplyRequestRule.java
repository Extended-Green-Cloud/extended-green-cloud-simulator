package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.greenenergy.job.listening.processing;

import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.PROCESSING;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.CHECK_WEATHER_FOR_NEW_POWER_SUPPLY_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_HANDLE_NEW_JOB_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToServerJob;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateRequest;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.basic.EnergyJob;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.greenenergy.GreenEnergyNode;

import jade.lang.acl.ACLMessage;

public class ProcessNewPowerSupplyRequestRule extends AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	public ProcessNewPowerSupplyRequestRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> rulesController) {
		super(rulesController, 1);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_RECEIVER_HANDLER_RULE, NEW_JOB_RECEIVER_HANDLE_NEW_JOB_RULE,
				"handles new Server power supply request - request weather",
				"handling new request for power supply coming from Server");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		return !agentProps.isHasError();
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final EnergyJob job = facts.get(MESSAGE_CONTENT);
		final ACLMessage message = facts.get(MESSAGE);

		final ServerJob serverJob = mapToServerJob(job, message.getSender());
		agentProps.addJob(serverJob, facts.get(STRATEGY_IDX), PROCESSING);

		final StrategyFacts weatherCheckFacts = new StrategyFacts(facts.get(STRATEGY_IDX));
		weatherCheckFacts.put(JOB, serverJob);
		weatherCheckFacts.put(MESSAGE, message);
		agent.addBehaviour(
				InitiateRequest.create(agent, weatherCheckFacts, CHECK_WEATHER_FOR_NEW_POWER_SUPPLY_RULE, controller));
	}
}
