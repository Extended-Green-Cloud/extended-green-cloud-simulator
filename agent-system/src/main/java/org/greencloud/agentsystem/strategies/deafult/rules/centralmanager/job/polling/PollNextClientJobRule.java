package org.greencloud.agentsystem.strategies.deafult.rules.centralmanager.job.polling;

import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_POLLING_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.POLL_NEXT_JOB_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentPeriodicRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

public class PollNextClientJobRule extends AgentPeriodicRule<CentralManagerAgentProps, CMANode> {

	private static final int SEND_NEXT_JOB_TIMEOUT = 2000;

	public PollNextClientJobRule(final RulesController<CentralManagerAgentProps, CMANode> controller) {
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
		return SEND_NEXT_JOB_TIMEOUT;
	}

	@Override
	protected void handleActionTrigger(final RuleSetFacts facts) {
		facts.put(RULE_TYPE, NEW_JOB_POLLING_RULE);
		facts.put(RULE_SET_IDX, controller.getLatestLongTermRuleSetIdx().get());
		controller.fire(facts);
	}

	@Override
	public AgentRule copy() {
		return new PollNextClientJobRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
