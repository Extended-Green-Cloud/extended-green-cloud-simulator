package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.greenenergy.sensor;

import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.FactTypeConstants.EVENT_CAUSE;
import static org.greencloud.commons.constants.FactTypeConstants.EVENT_DURATION;
import static org.greencloud.commons.constants.FactTypeConstants.EVENT_IS_FINISHED;
import static org.greencloud.commons.constants.FactTypeConstants.EVENT_TIME;
import static org.greencloud.commons.constants.FactTypeConstants.RESULT;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.enums.rules.RuleType.SENSE_EVENTS_RULE;

import java.util.Optional;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.greencloud.gui.event.AbstractEvent;
import org.greencloud.gui.event.PowerShortageEvent;
import org.greencloud.gui.event.WeatherDropEvent;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.template.AgentPeriodicRule;

public class SenseExternalGreenSourceEventsRule extends AgentPeriodicRule<GreenEnergyAgentProps, GreenEnergyNode> {

	private static final long GREEN_SOURCE_ENVIRONMENT_SENSOR_TIMEOUT = 100;

	public SenseExternalGreenSourceEventsRule(
			final RulesController<GreenEnergyAgentProps, GreenEnergyNode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(SENSE_EVENTS_RULE,
				"sense external Green Source events",
				"rule listens for external events sent to the Green Source");
	}

	@Override
	protected long specifyPeriod() {
		return GREEN_SOURCE_ENVIRONMENT_SENSOR_TIMEOUT;
	}

	@Override
	protected boolean evaluateBeforeTrigger(final RuleSetFacts facts) {
		return nonNull(agentNode);
	}

	@Override
	protected void handleActionTrigger(final RuleSetFacts facts) {
		final Optional<AbstractEvent> latestEvent = agentNode.getEvent();

		latestEvent.ifPresent(event -> {
			facts.put(EVENT_TIME, event.getOccurrenceTime());
			facts.put(RULE_TYPE, event.getEventTypeEnum().getRuleType());

			if (event instanceof PowerShortageEvent shortageEvent) {
				facts.put(EVENT_CAUSE, shortageEvent.getCause());
				facts.put(EVENT_IS_FINISHED, shortageEvent.isFinished());
				facts.put(RESULT, 0D);
			}
			if (event instanceof WeatherDropEvent weatherDropEvent) {
				facts.put(EVENT_DURATION, weatherDropEvent.getDuration());
			}

			controller.fire(facts);
		});
	}
}
