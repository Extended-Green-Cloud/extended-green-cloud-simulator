package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.scheduler.initial;

import static org.greencloud.commons.enums.rules.RuleType.INITIALIZE_BEHAVIOURS_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.POLL_NEXT_JOB_RULE;
import static org.greencloud.commons.enums.rules.RuleType.SENSE_EVENTS_RULE;
import static org.greencloud.commons.enums.rules.RuleType.SUBSCRIBE_OWNED_AGENTS_SERVICE_RULE;

import java.util.Set;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.gui.agents.scheduler.SchedulerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateSubscription;
import org.greencloud.rulescontroller.behaviour.listen.ListenForMessages;
import org.greencloud.rulescontroller.behaviour.schedule.SchedulePeriodically;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.simple.AgentBehaviourRule;

import jade.core.behaviours.Behaviour;

public class PrepareInitialSchedulerBehavioursRule extends AgentBehaviourRule<SchedulerAgentProps, SchedulerNode> {

	public PrepareInitialSchedulerBehavioursRule(final RulesController<SchedulerAgentProps, SchedulerNode> controller) {
		super(controller);
	}

	/**
	 * Method initialize set of behaviours that are to be added
	 */
	@Override
	protected Set<Behaviour> initializeBehaviours() {
		return Set.of(
				InitiateSubscription.create(agent, new RuleSetFacts(controller.getLatestRuleSet().get()),
						SUBSCRIBE_OWNED_AGENTS_SERVICE_RULE, controller),
				SchedulePeriodically.create(agent, new RuleSetFacts(controller.getLatestRuleSet().get()),
						POLL_NEXT_JOB_RULE, controller),
				SchedulePeriodically.create(agent, new RuleSetFacts(controller.getLatestRuleSet().get()),
						SENSE_EVENTS_RULE, controller),
				ListenForMessages.create(agent, NEW_JOB_RECEIVER_RULE, controller),
				ListenForMessages.create(agent, JOB_STATUS_RECEIVER_RULE, controller, true)
		);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(INITIALIZE_BEHAVIOURS_RULE,
				"preparing initial Scheduler behaviours",
				"when Scheduler agent is connected to RulesController, it starts initial set of behaviours");
	}
}
