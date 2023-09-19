package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.scheduler.job.polling;

import static org.greencloud.commons.constants.FactTypeConstants.INITIATE_CFP;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.LOOK_FOR_JOB_EXECUTOR_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_POLLING_RULE;
import static org.greencloud.commons.enums.rules.RuleType.POLL_NEXT_JOB_RULE;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateCallForProposal;
import org.greencloud.rulescontroller.strategy.defaultstrategy.rules.scheduler.job.polling.domain.PollingConstants;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentPeriodicRule;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;

import com.gui.agents.scheduler.SchedulerNode;

public class PollNextClientJobRule extends AgentPeriodicRule<SchedulerAgentProps, SchedulerNode> {

	public PollNextClientJobRule(final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(POLL_NEXT_JOB_RULE,
				"trigger next scheduled job polling",
				"rule executed periodically which triggers polling next scheduled job");
	}

	@Override
	protected long specifyPeriod() {
		return PollingConstants.SEND_NEXT_JOB_TIMEOUT;
	}

	@Override
	protected void handleActionTrigger(final StrategyFacts facts) {
		facts.put(RULE_TYPE, NEW_JOB_POLLING_RULE);
		facts.put(INITIATE_CFP, false);
		facts.put(STRATEGY_IDX, controller.getLatestStrategy().get());
		controller.fire(facts);

		if (facts.get(INITIATE_CFP)) {
			agent.addBehaviour(InitiateCallForProposal.create(agent, facts, LOOK_FOR_JOB_EXECUTOR_RULE, controller));
		}
	}
}
