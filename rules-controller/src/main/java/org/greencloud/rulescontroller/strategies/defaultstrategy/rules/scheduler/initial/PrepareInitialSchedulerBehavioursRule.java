package org.greencloud.rulescontroller.strategies.defaultstrategy.rules.scheduler.initial;

import static org.greencloud.commons.enums.rules.RuleType.INITIALIZE_BEHAVIOURS_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.POLL_NEXT_JOB_RULE;
import static org.greencloud.commons.enums.rules.RuleType.SENSE_EVENTS_RULE;
import static org.greencloud.commons.enums.rules.RuleType.SUBSCRIBE_OWNED_AGENTS_SERVICE_RULE;

import java.util.Set;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateSubscription;
import org.greencloud.rulescontroller.behaviour.listen.ListenForMessages;
import org.greencloud.rulescontroller.behaviour.schedule.SchedulePeriodically;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.simple.AgentBehaviourRule;

import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.scheduler.SchedulerNode;

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
				InitiateSubscription.create(agent, new StrategyFacts(controller.getLatestStrategy().get()),
						SUBSCRIBE_OWNED_AGENTS_SERVICE_RULE, controller),
				SchedulePeriodically.create(agent, new StrategyFacts(controller.getLatestStrategy().get()),
						POLL_NEXT_JOB_RULE, controller),
				SchedulePeriodically.create(agent, new StrategyFacts(controller.getLatestStrategy().get()),
						SENSE_EVENTS_RULE, controller),
				new ListenForMessages(agent, NEW_JOB_RECEIVER_RULE, controller),
				new ListenForMessages(agent, JOB_STATUS_RECEIVER_RULE, controller, true)
		);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(INITIALIZE_BEHAVIOURS_RULE,
				"preparing initial Scheduler behaviours",
				"when Scheduler agent is connected to RulesController, it starts initial set of behaviours");
	}
}
