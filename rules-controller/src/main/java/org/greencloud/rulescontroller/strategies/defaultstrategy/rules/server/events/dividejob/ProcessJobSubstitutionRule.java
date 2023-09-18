package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.server.events.dividejob;

import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_FINISH_INFORM;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_IS_STARTED;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_START_INFORM;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.FINISH_JOB_EXECUTION_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_JOB_SUBSTITUTION_RULE;
import static org.greencloud.commons.enums.rules.RuleType.START_JOB_EXECUTION_RULE;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.schedule.ScheduleOnce;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.server.ServerNode;

public class ProcessJobSubstitutionRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	public ProcessJobSubstitutionRule(final RulesController<ServerAgentProps, ServerNode> rulesController) {
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
		final boolean hasStarted = facts.get(JOB_IS_STARTED);
		final ClientJob newJobInstance = facts.get(JOB);

		if (hasStarted) {
			final StrategyFacts jobFinishFacts = new StrategyFacts(facts.get(STRATEGY_IDX));

			jobFinishFacts.put(RULE_TYPE, FINISH_JOB_EXECUTION_RULE);
			jobFinishFacts.put(JOB, newJobInstance);
			jobFinishFacts.put(JOB_FINISH_INFORM, true);

			agent.addBehaviour(ScheduleOnce.create(agent, jobFinishFacts, FINISH_JOB_EXECUTION_RULE, controller,
					f -> f.get(STRATEGY_IDX)));
		} else {
			final StrategyFacts jobStartFacts = new StrategyFacts(facts.get(STRATEGY_IDX));

			jobStartFacts.put(RULE_TYPE, START_JOB_EXECUTION_RULE);
			jobStartFacts.put(JOB, newJobInstance);
			jobStartFacts.put(JOB_START_INFORM, true);
			jobStartFacts.put(JOB_FINISH_INFORM, true);

			agent.addBehaviour(ScheduleOnce.create(agent, jobStartFacts, START_JOB_EXECUTION_RULE, controller,
					f -> f.get(STRATEGY_IDX)));
		}
	}
}
