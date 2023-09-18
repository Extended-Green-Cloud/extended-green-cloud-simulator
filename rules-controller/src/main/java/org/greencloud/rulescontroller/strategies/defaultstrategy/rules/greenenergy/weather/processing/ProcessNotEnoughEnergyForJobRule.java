package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.greenenergy.weather.processing;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.NOT_ENOUGH_ENERGY_FOR_JOB_RULE;
import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.greencloud.commons.domain.facts.StrategyFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.job.basic.ServerJob;
import com.gui.agents.greenenergy.GreenEnergyNode;

public class ProcessNotEnoughEnergyForJobRule extends AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final Logger logger = getLogger(ProcessNotEnoughEnergyForJobRule.class);


	public ProcessNotEnoughEnergyForJobRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NOT_ENOUGH_ENERGY_FOR_JOB_RULE,
				"not enough energy to re-supply the job",
				"when Green Source does not have enough energy to re-supply the job, print message");
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final ServerJob job = facts.get(JOB);
		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("There is not enough available energy to put job back in progress. Leaving the job {} on hold",
				job.getJobId());
	}
}
