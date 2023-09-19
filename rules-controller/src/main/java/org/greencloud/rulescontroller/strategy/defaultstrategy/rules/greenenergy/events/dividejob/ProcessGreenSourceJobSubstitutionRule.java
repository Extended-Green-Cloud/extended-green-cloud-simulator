package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.greenenergy.events.dividejob;

import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.JOB_MANUAL_FINISH_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_JOB_SUBSTITUTION_RULE;
import static org.greencloud.rulescontroller.strategy.StrategySelector.SELECT_BY_FACTS_IDX;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.commons.domain.job.basic.ServerJob;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.schedule.ScheduleOnce;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;

import com.gui.agents.greenenergy.GreenEnergyNode;

public class ProcessGreenSourceJobSubstitutionRule extends AgentBasicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	public ProcessGreenSourceJobSubstitutionRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> rulesController) {
		super(rulesController);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROCESS_JOB_SUBSTITUTION_RULE,
				"substituting job instances with new ones",
				"rule substitutes old job instances with new ones");
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final ServerJob newJobInstance = facts.get(JOB);
		final StrategyFacts jobManualFinish = new StrategyFacts(facts.get(STRATEGY_IDX));
		jobManualFinish.put(JOB, newJobInstance);
		agent.addBehaviour(
				ScheduleOnce.create(agent, jobManualFinish, JOB_MANUAL_FINISH_RULE, controller, SELECT_BY_FACTS_IDX));
	}
}
