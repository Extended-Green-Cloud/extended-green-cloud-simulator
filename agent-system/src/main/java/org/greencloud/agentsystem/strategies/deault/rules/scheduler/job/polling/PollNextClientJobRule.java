package org.greencloud.agentsystem.strategies.deault.rules.scheduler.job.polling;

import static org.greencloud.commons.constants.EGCSFactTypeConstants.INITIATE_CFP;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LOOK_FOR_JOB_EXECUTOR_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_POLLING_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.POLL_NEXT_JOB_RULE;

import org.greencloud.agentsystem.strategies.deault.rules.scheduler.job.polling.domain.PollingConstants;
import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.greencloud.gui.agents.scheduler.SchedulerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.initiate.InitiateCallForProposal;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentPeriodicRule;

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
	protected void handleActionTrigger(final RuleSetFacts facts) {
		facts.put(RULE_TYPE, NEW_JOB_POLLING_RULE);
		facts.put(INITIATE_CFP, false);
		facts.put(RULE_SET_IDX, controller.getLatestLongTermRuleSetIdx().get());
		controller.fire(facts);

		if (facts.get(INITIATE_CFP)) {
			agent.addBehaviour(InitiateCallForProposal.create(agent, facts, LOOK_FOR_JOB_EXECUTOR_RULE, controller));
		}
	}
}
