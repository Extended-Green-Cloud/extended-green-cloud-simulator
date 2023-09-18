package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.greenenergy.events.transfer.processing;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.REFUSED_TRANSFER_JOB_ALREADY_FINISHED_RULE;
import static org.greencloud.commons.enums.rules.RuleType.REFUSED_TRANSFER_JOB_RULE;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.greenenergy.GreenEnergyNode;

public class ProcessTransferRefuseJobAlreadyFinishedRule
		extends AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ProcessTransferRefuseJobAlreadyFinishedRule.class);

	public ProcessTransferRefuseJobAlreadyFinishedRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> rulesController) {
		super(rulesController, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(REFUSED_TRANSFER_JOB_RULE, REFUSED_TRANSFER_JOB_ALREADY_FINISHED_RULE,
				"process refused job transfer request - already finished",
				"rule processes refusal of job transfer request in Server");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		final ServerJob job = facts.get(JOB);
		return !agentProps.getServerJobs().containsKey(job);
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final ServerJob job = facts.get(JOB);
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("The job with id {} has finished before putting it on hold.", job.getJobId());
	}
}
