package org.greencloud.agentsystem.strategies.deafult.rules.greenenergy.job.listening.newjob.processing;

import static org.greencloud.commons.args.agent.EGCSAgentType.GREEN_ENERGY;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.PROCESSING;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.CHECK_WEATHER_FOR_NEW_POWER_SUPPLY_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_HANDLE_NEW_JOB_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToServerJob;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.basic.EnergyJob;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.initiate.InitiateRequest;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

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
	public boolean evaluateRule(final RuleSetFacts facts) {
		return !agentProps.isHasError();
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final EnergyJob job = facts.get(MESSAGE_CONTENT);
		final ACLMessage message = facts.get(MESSAGE);

		final ServerJob serverJob = mapToServerJob(job, message.getSender());
		agentProps.addJob(serverJob, facts.get(RULE_SET_IDX), PROCESSING);

		final RuleSetFacts weatherCheckFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
		weatherCheckFacts.put(JOB, serverJob);
		weatherCheckFacts.put(MESSAGE, message);
		agent.addBehaviour(InitiateRequest.create(agent, weatherCheckFacts, CHECK_WEATHER_FOR_NEW_POWER_SUPPLY_RULE,
				controller));
	}

	@Override
	public AgentRule copy() {
		return new ProcessNewPowerSupplyRequestRule(controller);
	}

	@Override
	public String getAgentType() {
		return GREEN_ENERGY.getName();
	}
}
