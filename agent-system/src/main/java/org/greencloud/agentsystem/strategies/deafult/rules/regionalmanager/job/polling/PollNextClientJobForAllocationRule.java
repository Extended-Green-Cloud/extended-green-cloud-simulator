package org.greencloud.agentsystem.strategies.deafult.rules.regionalmanager.job.polling;

import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_POLLING_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.POLL_NEXT_JOB_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentPeriodicRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

public class PollNextClientJobForAllocationRule extends AgentPeriodicRule<RegionalManagerAgentProps, RMANode> {

	private static final int POLL_NEXT_JOB_TIMEOUT = 2000;

	public PollNextClientJobForAllocationRule(final RulesController<RegionalManagerAgentProps, RMANode>
			controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(POLL_NEXT_JOB_RULE,
				"trigger next job for execution polling",
				"rule executed periodically which triggers polling next job for execution");
	}

	@Override
	protected long specifyPeriod() {
		return POLL_NEXT_JOB_TIMEOUT;
	}

	@Override
	protected boolean evaluateBeforeTrigger(final RuleSetFacts facts) {
		return !agentProps.getJobsToBeExecuted().isEmpty();
	}

	@Override
	protected void handleActionTrigger(final RuleSetFacts facts) {
		facts.put(RULE_TYPE, NEW_JOB_POLLING_RULE);
		facts.put(RULE_SET_IDX, controller.getLatestLongTermRuleSetIdx().get());
		controller.fire(facts);
	}

	@Override
	public AgentRule copy() {
		return new PollNextClientJobForAllocationRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
