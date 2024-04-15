package org.greencloud.agentsystem.strategies.deault.rules.scheduler.sensor;

import static java.util.Objects.nonNull;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SENSE_EVENTS_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.EVENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;

import java.util.Optional;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.gui.agents.scheduler.SchedulerNode;
import org.jrba.environment.domain.ExternalEvent;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentPeriodicRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

public class SenseExternalSchedulerEventsRule extends AgentPeriodicRule<SchedulerAgentProps, SchedulerNode> {

	private static final long SERVER_ENVIRONMENT_SENSOR_TIMEOUT = 100;

	public SenseExternalSchedulerEventsRule(final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(SENSE_EVENTS_RULE,
				"sense external events",
				"rule listens for external events sent to the Server");
	}

	@Override
	protected long specifyPeriod() {
		return SERVER_ENVIRONMENT_SENSOR_TIMEOUT;
	}

	@Override
	protected boolean evaluateBeforeTrigger(final RuleSetFacts facts) {
		return nonNull(agentNode);
	}

	@Override
	protected void handleActionTrigger(final RuleSetFacts facts) {
		final Optional<ExternalEvent> latestEvent = agentNode.getEvent();

		latestEvent.ifPresent(event -> {
			facts.put(RULE_TYPE, event.getEventType().getRuleType());
			facts.put(EVENT, event);
			controller.fire(facts);
		});
	}
}
