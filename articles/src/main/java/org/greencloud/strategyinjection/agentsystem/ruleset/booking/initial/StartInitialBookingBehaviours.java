package org.greencloud.strategyinjection.agentsystem.ruleset.booking.initial;

import static org.greencloud.commons.enums.rules.RuleType.SEARCH_OWNED_AGENTS_RULE;
import static org.greencloud.commons.enums.rules.RuleType.SENSE_EVENTS_RULE;

import java.util.Set;

import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.schedule.SchedulePeriodically;
import org.greencloud.rulescontroller.behaviour.search.SearchForAgents;
import org.greencloud.rulescontroller.rule.simple.AgentBehaviourRule;
import org.greencloud.strategyinjection.agentsystem.agents.booking.node.BookingNode;
import org.greencloud.strategyinjection.agentsystem.agents.booking.props.BookingProps;

import jade.core.behaviours.Behaviour;

public class StartInitialBookingBehaviours extends AgentBehaviourRule<BookingProps, BookingNode> {

	public StartInitialBookingBehaviours(
			final RulesController<BookingProps, BookingNode> controller) {
		super(controller);
	}

	/**
	 * Method initialize set of behaviours that are to be added
	 */
	@Override
	protected Set<Behaviour> initializeBehaviours() {
		return Set.of(
				SearchForAgents.create(agent, new RuleSetFacts(controller.getLatestLongTermRuleSetIdx().get()),
						SEARCH_OWNED_AGENTS_RULE, controller),
				SchedulePeriodically.create(agent, new RuleSetFacts(controller.getLatestLongTermRuleSetIdx().get()),
						SENSE_EVENTS_RULE, controller)
		);
	}
}
