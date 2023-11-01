package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.initial;

import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.enums.rules.RuleType.CHECK_AFFECTED_JOBS_RULE;
import static org.greencloud.commons.enums.rules.RuleType.GREEN_SOURCE_STATUS_CHANGE_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_MANUAL_FINISH_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_CHECK_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_RECEIVER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_JOB_TRANSFER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_POWER_SHORTAGE_FINISH_RULE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_RULE_SET_REMOVAL_RULE;
import static org.greencloud.commons.enums.rules.RuleType.LISTEN_FOR_RULE_SET_UPDATE_RULE;
import static org.greencloud.commons.enums.rules.RuleType.NEW_JOB_RECEIVER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.SENSE_EVENTS_RULE;
import static org.greencloud.commons.enums.rules.RuleType.SUBSCRIBE_OWNED_AGENTS_SERVICE_RULE;

import java.util.Set;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.gui.agents.server.ServerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.behaviour.initiate.InitiateSubscription;
import org.greencloud.rulescontroller.behaviour.listen.ListenForMessages;
import org.greencloud.rulescontroller.behaviour.schedule.SchedulePeriodically;
import org.greencloud.rulescontroller.rule.simple.AgentBehaviourRule;

import jade.core.behaviours.Behaviour;

public class StartInitialServerBehaviours extends AgentBehaviourRule<ServerAgentProps, ServerNode> {

	public StartInitialServerBehaviours(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller);
	}

	/**
	 * Method initialize set of behaviours that are to be added
	 */
	@Override
	protected Set<Behaviour> initializeBehaviours() {
		final RuleSetFacts facts = new RuleSetFacts(controller.getLatestRuleSet().get());
		facts.put(RULE_TYPE, "INITIALIZE_SERVER_RESOURCE_KNOWLEDGE");
		controller.fire(facts);

		return Set.of(
				InitiateSubscription.create(agent, new RuleSetFacts(controller.getLatestRuleSet().get()),
						SUBSCRIBE_OWNED_AGENTS_SERVICE_RULE, controller),
				ListenForMessages.create(agent, GREEN_SOURCE_STATUS_CHANGE_RULE, controller),
				ListenForMessages.create(agent, NEW_JOB_RECEIVER_RULE, controller),
				ListenForMessages.create(agent, JOB_STATUS_RECEIVER_RULE, controller),
				ListenForMessages.create(agent, JOB_STATUS_CHECK_RULE, controller),
				ListenForMessages.create(agent, "CNA_RESOURCE_REQUEST_RULE", controller, true),
				ListenForMessages.create(agent, JOB_MANUAL_FINISH_RULE, controller),
				ListenForMessages.create(agent, LISTEN_FOR_POWER_SHORTAGE_FINISH_RULE, controller),
				ListenForMessages.create(agent, LISTEN_FOR_JOB_TRANSFER_RULE, controller),
				ListenForMessages.create(agent, LISTEN_FOR_RULE_SET_UPDATE_RULE, controller),
				ListenForMessages.create(agent, LISTEN_FOR_RULE_SET_REMOVAL_RULE, controller),
				SchedulePeriodically.create(agent, new RuleSetFacts(controller.getLatestRuleSet().get()),
						CHECK_AFFECTED_JOBS_RULE, controller),
				SchedulePeriodically.create(agent, new RuleSetFacts(controller.getLatestRuleSet().get()),
						SENSE_EVENTS_RULE, controller)
		);
	}
}
