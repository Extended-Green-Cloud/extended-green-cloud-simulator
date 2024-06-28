package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.events.shortagegreensource.processing;

import static java.lang.String.valueOf;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.HANDLE_POWER_SHORTAGE_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_JOB_TRANSFER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_JOB_TRANSFER_HANDLE_TRANSFER_IN_GREEN_SOURCE_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.TRANSFER_JOB_IN_GS_RULE;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static org.jrba.rulesengine.constants.FactTypeConstants.AGENTS;
import static org.jrba.rulesengine.constants.FactTypeConstants.EVENT_TIME;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.SET_EVENT_ERROR;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.jrba.utils.rules.RuleSetSelector.SELECT_BY_FACTS_IDX;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.transfer.JobDivided;
import org.greencloud.commons.domain.job.transfer.JobPowerShortageTransfer;
import org.greencloud.gui.agents.server.ServerNode;
import org.jeasy.rules.api.Facts;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.initiate.InitiateCallForProposal;
import org.jrba.rulesengine.behaviour.schedule.ScheduleOnce;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class ProcessPowerShortageTransferRequestGreenSourceRule
		extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessPowerShortageTransferRequestGreenSourceRule.class);

	public ProcessPowerShortageTransferRequestGreenSourceRule(
			final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController, 1);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_JOB_TRANSFER_HANDLER_RULE,
				LISTEN_FOR_JOB_TRANSFER_HANDLE_TRANSFER_IN_GREEN_SOURCE_RULE,
				"handles job transfer in Green Source",
				"rule handles the transfer request coming from Green Source affected by power shortage");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		final AID sender = ((ACLMessage) facts.get(MESSAGE)).getSender();
		final List<AID> greenSources = agentProps.getRemainingAgents(sender, agentProps.getOwnedActiveGreenSources());

		return !greenSources.isEmpty() &&
				nonNull(job.getExpectedEndTime()) &&
				job.getExpectedEndTime().isAfter(getCurrentTime());
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		final JobPowerShortageTransfer transfer = facts.get(MESSAGE_CONTENT);

		final AID sender = ((ACLMessage) facts.get(MESSAGE)).getSender();
		final JobDivided<ClientJob> newJobInstances = schedulePowerShortageHandling(facts);
		final List<AID> greenSources = agentProps.getRemainingAgents(sender, agentProps.getOwnedActiveGreenSources());

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Sending call for proposal to Green Source Agents to transfer job with id {}", job.getJobId());

		facts.put(EVENT_TIME, transfer.getPowerShortageStart());
		facts.put(JOBS, newJobInstances);
		facts.put(AGENTS, greenSources);

		agent.addBehaviour(InitiateCallForProposal.create(agent, facts, TRANSFER_JOB_IN_GS_RULE, controller));
	}

	private JobDivided<ClientJob> schedulePowerShortageHandling(final Facts facts) {
		final ClientJob job = facts.get(JOB);
		final JobPowerShortageTransfer transfer = facts.get(MESSAGE_CONTENT);

		final RuleSetFacts errorFacts = new RuleSetFacts(facts.get(RULE_SET_IDX));
		errorFacts.put(SET_EVENT_ERROR, false);
		errorFacts.put(JOBS, singletonList(job));
		errorFacts.put(EVENT_TIME, transfer.getPowerShortageStart());

		agent.addBehaviour(ScheduleOnce.create(agent, errorFacts, HANDLE_POWER_SHORTAGE_RULE, controller,
				SELECT_BY_FACTS_IDX));

		final RuleSetFacts divisionFacts = agentProps.constructDivisionFacts(transfer, job, facts.get(RULE_SET_IDX));
		controller.fire(divisionFacts);
		return divisionFacts.get(RESULT);
	}

	@Override
	public AgentRule copy() {
		return new ProcessPowerShortageTransferRequestGreenSourceRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
