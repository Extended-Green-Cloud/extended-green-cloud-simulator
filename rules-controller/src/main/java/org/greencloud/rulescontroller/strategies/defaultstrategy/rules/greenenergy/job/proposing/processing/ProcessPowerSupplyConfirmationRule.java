package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.greenenergy.job.proposing.processing;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_ID;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.JOB_MANUAL_FINISH_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_SCHEDULE_POWER_SUPPLY_CONFIRM_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_SCHEDULE_POWER_SUPPLY_RULE;
import static org.greencloud.commons.utils.messaging.factory.ReplyMessageFactory.prepareReply;
import static jade.lang.acl.ACLMessage.INFORM;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.schedule.ScheduleOnce;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.extended.JobWithProtocol;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.greenenergy.GreenEnergyNode;

import jade.lang.acl.ACLMessage;

public class ProcessPowerSupplyConfirmationRule
		extends AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ProcessPowerSupplyConfirmationRule.class);

	public ProcessPowerSupplyConfirmationRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> rulesController) {
		super(rulesController, 1);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROCESS_SCHEDULE_POWER_SUPPLY_RULE,
				PROCESS_SCHEDULE_POWER_SUPPLY_CONFIRM_RULE,
				"handle accept propose from Server - confirm",
				"rule handlers Accept Proposal message to given power supply offer");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		final ServerJob job = facts.get(JOB);
		final Optional<Double> power = facts.get(RESULT);
		return power.isPresent() && job.getEstimatedEnergy() <= power.get();
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final ServerJob job = facts.get(JOB);
		final JobWithProtocol jobWithProtocol = facts.get(JOB_ID);
		final ACLMessage proposal = facts.get(MESSAGE);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("Sending information regarding job {} acceptance back to server agent", job.getJobId());
		agentProps.getServerJobs().replace(job, JobExecutionStatusEnum.ACCEPTED);

		final StrategyFacts jobManualFinish = new StrategyFacts(facts.get(STRATEGY_IDX));
		jobManualFinish.put(JOB, job);
		agent.addBehaviour(ScheduleOnce.create(agent, jobManualFinish, JOB_MANUAL_FINISH_RULE, controller,
				f -> f.get(STRATEGY_IDX)));

		agent.send(prepareReply(proposal, jobWithProtocol.getJobInstanceIdentifier(), INFORM,
				jobWithProtocol.getReplyProtocol()));
	}

}
