package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.server.events.shortagegreensource.processing;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.constants.FactTypeConstants.EVENT_TIME;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.JOBS;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_ID;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.constants.FactTypeConstants.SET_EVENT_ERROR;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.HANDLE_POWER_SHORTAGE_RULE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_JOB_TRANSFER_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_JOB_TRANSFER_HANDLE_TRANSFER_IN_CNA_RULE;
import static org.greencloud.commons.enums.rules.RuleType.TRANSFER_JOB_FOR_GS_IN_CNA_RULE;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
import static java.lang.String.valueOf;
import static java.util.Collections.singletonList;
import static org.greencloud.rulescontroller.strategy.StrategySelector.SELECT_BY_FACTS_IDX;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateRequest;
import org.greencloud.rulescontroller.behaviour.schedule.ScheduleOnce;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.jeasy.rules.api.Facts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.transfer.JobPowerShortageTransfer;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.server.ServerNode;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class ProcessPowerShortageTransferRequestTransferCNARule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessPowerShortageTransferRequestTransferCNARule.class);

	public ProcessPowerShortageTransferRequestTransferCNARule(
			final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(LISTEN_FOR_JOB_TRANSFER_HANDLER_RULE,
				LISTEN_FOR_JOB_TRANSFER_HANDLE_TRANSFER_IN_CNA_RULE,
				"handles job transfer in CNA",
				"rule handles the transfer request coming from Green Source affected by power shortage");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		final AID sender = ((ACLMessage) facts.get(MESSAGE)).getSender();
		final List<AID> greenSources = agentProps.getRemainingAgents(sender, agentProps.getOwnedActiveGreenSources());

		if (greenSources.isEmpty() && job.getEndTime().isAfter(getCurrentTime())) {
			schedulePowerShortageHandling(facts);
			return true;
		}
		return false;
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("No green sources available. Sending transfer request to cloud network");
		passTransferRequestToCNA(facts.get(MESSAGE), facts);
	}

	private void passTransferRequestToCNA(final ACLMessage transferRequest, final Facts facts) {
		final JobPowerShortageTransfer transfer = facts.get(MESSAGE_CONTENT);

		final StrategyFacts cnaTransferFacts = new StrategyFacts(facts.get(STRATEGY_IDX));
		cnaTransferFacts.put(JOB, transfer);
		cnaTransferFacts.put(JOB_ID, transfer.getSecondJobInstanceId());
		cnaTransferFacts.put(MESSAGE, transferRequest);

		agent.addBehaviour(
				InitiateRequest.create(agent, cnaTransferFacts, TRANSFER_JOB_FOR_GS_IN_CNA_RULE, controller));
	}

	private void schedulePowerShortageHandling(final Facts facts) {
		final ClientJob job = facts.get(JOB);
		final JobPowerShortageTransfer transfer = facts.get(MESSAGE_CONTENT);

		final StrategyFacts errorFacts = new StrategyFacts(facts.get(STRATEGY_IDX));
		errorFacts.put(SET_EVENT_ERROR, false);
		errorFacts.put(JOBS, singletonList(job));
		errorFacts.put(EVENT_TIME, transfer.getPowerShortageStart());

		agent.addBehaviour(ScheduleOnce.create(agent, errorFacts, HANDLE_POWER_SHORTAGE_RULE, controller,
				SELECT_BY_FACTS_IDX));

		final StrategyFacts divisionFacts = agentProps.constructDivisionFacts(transfer, job, facts.get(STRATEGY_IDX));
		controller.fire(divisionFacts);
	}
}
