package org.greencloud.agentsystem.strategies.rulesets.priority.executiontimepriority.rules.server.initial;

import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.CHECK_AFFECTED_JOBS_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.GREEN_SOURCE_STATUS_CHANGE_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.INITIALIZE_SERVER_RESOURCE_KNOWLEDGE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_ENERGY_PRICE_RECEIVER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_EXECUTION_TIME_ESTIMATION_LISTENER;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_MANUAL_FINISH_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_CHECK_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_RECEIVER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_JOB_TRANSFER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_POWER_SHORTAGE_FINISH_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_RULE_SET_REMOVAL_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LISTEN_FOR_RULE_SET_UPDATE_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.POLL_NEXT_JOB_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.RMA_RESOURCE_REQUEST_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SENSE_EVENTS_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.SUBSCRIBE_OWNED_AGENTS_SERVICE_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;

import java.util.Set;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.initiate.InitiateSubscription;
import org.jrba.rulesengine.behaviour.listen.ListenForMessages;
import org.jrba.rulesengine.behaviour.schedule.SchedulePeriodically;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.simple.AgentBehaviourRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

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
		final RuleSetFacts facts = new RuleSetFacts(controller.getLatestLongTermRuleSetIdx().get());
		facts.put(RULE_TYPE, INITIALIZE_SERVER_RESOURCE_KNOWLEDGE);
		controller.fire(facts);

		return Set.of(
				InitiateSubscription.create(agent, new RuleSetFacts(controller.getLatestLongTermRuleSetIdx().get()),
						SUBSCRIBE_OWNED_AGENTS_SERVICE_RULE, controller),
				ListenForMessages.create(agent, GREEN_SOURCE_STATUS_CHANGE_RULE, controller),
				ListenForMessages.create(agent, NEW_JOB_RECEIVER_RULE, controller),
				ListenForMessages.create(agent, JOB_STATUS_RECEIVER_RULE, controller),
				ListenForMessages.create(agent, JOB_STATUS_CHECK_RULE, controller),
				ListenForMessages.create(agent, RMA_RESOURCE_REQUEST_RULE, controller, true),
				ListenForMessages.create(agent, JOB_MANUAL_FINISH_RULE, controller),
				ListenForMessages.create(agent, LISTEN_FOR_POWER_SHORTAGE_FINISH_RULE, controller),
				ListenForMessages.create(agent, LISTEN_FOR_JOB_TRANSFER_RULE, controller),
				ListenForMessages.create(agent, LISTEN_FOR_RULE_SET_UPDATE_RULE, controller),
				ListenForMessages.create(agent, LISTEN_FOR_RULE_SET_REMOVAL_RULE, controller),
				ListenForMessages.create(agent, JOB_ENERGY_PRICE_RECEIVER_RULE, controller),
				ListenForMessages.create(agent, JOB_EXECUTION_TIME_ESTIMATION_LISTENER, controller),
				SchedulePeriodically.create(agent, new RuleSetFacts(controller.getLatestLongTermRuleSetIdx().get()),
						POLL_NEXT_JOB_RULE, controller),
				SchedulePeriodically.create(agent, new RuleSetFacts(controller.getLatestLongTermRuleSetIdx().get()),
						CHECK_AFFECTED_JOBS_RULE, controller),
				SchedulePeriodically.create(agent, new RuleSetFacts(controller.getLatestLongTermRuleSetIdx().get()),
						SENSE_EVENTS_RULE, controller)
		);
	}

	@Override
	public AgentRule copy() {
		return new StartInitialServerBehaviours(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
