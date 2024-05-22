package org.greencloud.agentsystem.strategies.deault.rules.greenenergy.sensor;

import static java.util.Objects.nonNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.GREEN_ENERGY;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SENSE_EVENTS_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.EVENT_CAUSE;
import static org.jrba.rulesengine.constants.FactTypeConstants.EVENT_DURATION;
import static org.jrba.rulesengine.constants.FactTypeConstants.EVENT_IS_FINISHED;
import static org.jrba.rulesengine.constants.FactTypeConstants.EVENT_TIME;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;

import java.util.Optional;

import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.gui.agents.greenenergy.GreenEnergyNode;
import org.greencloud.gui.event.PowerShortageEvent;
import org.greencloud.gui.event.WeatherDropEvent;
import org.jrba.environment.domain.ExternalEvent;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.template.AgentPeriodicRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

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
		final Optional<ExternalEvent> latestEvent = agentNode.getEvent();

		latestEvent.ifPresent(event -> {
			facts.put(EVENT_TIME, event.getOccurrenceTime());
			facts.put(RULE_TYPE, event.getEventType().getRuleType());

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

	@Override
	public AgentRule copy() {
		return new SenseExternalGreenSourceEventsRule(controller);
	}

	@Override
	public String getAgentType() {
		return GREEN_ENERGY.getName();
	}
}
