package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.sensor;

import static org.greencloud.commons.constants.FactTypeConstants.EVENT;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.enums.rules.RuleType.SENSE_EVENTS_RULE;
import static java.util.Objects.nonNull;

import java.util.Optional;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentPeriodicRule;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import com.gui.agents.scheduler.SchedulerNode;
import com.gui.event.AbstractEvent;

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
		final Optional<AbstractEvent> latestEvent = agentNode.getEvent();

		latestEvent.ifPresent(event -> {
			facts.put(RULE_TYPE, event.getEventTypeEnum().getRuleType());
			facts.put(EVENT, event);
			controller.fire(facts);
		});
	}
}
