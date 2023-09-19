package org.greencloud.rulescontroller.strategy;

import static java.util.Collections.emptyList;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_STEP;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.greencloud.commons.args.agent.client.agent.ClientAgentProps;
import org.greencloud.commons.args.agent.cloudnetwork.agent.CloudNetworkAgentProps;
import org.greencloud.commons.args.agent.greenenergy.agent.GreenEnergyAgentProps;
import org.greencloud.commons.args.agent.monitoring.agent.MonitoringAgentProps;
import org.greencloud.commons.args.agent.scheduler.agent.SchedulerAgentProps;
import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.mvel.MVELRuleMapper;
import org.greencloud.rulescontroller.rest.domain.StrategyRest;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;

import com.gui.agents.client.ClientNode;
import com.gui.agents.cloudnetwork.CloudNetworkNode;
import com.gui.agents.greenenergy.GreenEnergyNode;
import com.gui.agents.monitoring.MonitoringNode;
import com.gui.agents.scheduler.SchedulerNode;
import com.gui.agents.server.ServerNode;

import lombok.Getter;

/**
 * Class represents strategy of a given system part
 */
@Getter
@SuppressWarnings("unchecked")
public class Strategy {

	protected final RulesEngine rulesEngine;
	private final String name;
	protected RulesController<?, ?> rulesController;
	private List<AgentRule> agentRules;

	/**
	 * Constructor
	 *
	 * @param strategyRest JSON Rest object from which strategy is to be created
	 */
	public Strategy(final StrategyRest strategyRest) {
		this.rulesEngine = new DefaultRulesEngine();
		this.name = strategyRest.getName();
		this.agentRules = strategyRest.getRules().stream()
				.map(MVELRuleMapper::getRuleForType)
				.map(AgentRule.class::cast)
				.toList();
	}

	/**
	 * Constructor
	 *
	 * @param strategy   strategy template from strategy map
	 * @param controller controller which runs given strategy
	 */
	public Strategy(final Strategy strategy, final RulesController<?, ?> controller) {
		this.rulesEngine = new DefaultRulesEngine();
		this.rulesController = controller;
		this.name = strategy.getName();
		this.agentRules = strategy.getAgentRules().stream()
				.filter(rule -> rule.getAgentType().equals(controller.getAgentProps().getAgentType())).toList();
		agentRules.forEach(agentRule -> agentRule.connectToController(controller));
	}

	/**
	 * Constructor
	 *
	 * @param name       name of the strategy
	 * @param controller controller which runs given strategy
	 */
	protected Strategy(final String name, final RulesController<?, ?> controller) {
		this.rulesEngine = new DefaultRulesEngine();
		this.agentRules = new ArrayList<>();
		this.name = name;
		this.rulesController = controller;
		initializeRules(rulesController);
	}

	/**
	 * Method fires agent strategy for a set of facts
	 *
	 * @param facts set of facts based on which actions are going to be taken
	 */
	public void fireStrategy(final StrategyFacts facts) {
		final Rules rules = new Rules();
		agentRules.stream()
				.filter(agentRule -> agentRule.getRuleType().equals(facts.get(RULE_TYPE)))
				.map(AgentRule::getRules)
				.flatMap(Collection::stream)
				.filter(agentRule -> agentRule.isRuleStep()
						? agentRule.getStepType().equals(facts.get(RULE_STEP))
						: agentRule.getRuleType().equals(facts.get(RULE_TYPE)))
				.forEach(rules::register);

		if (!rules.isEmpty()) {
			rulesEngine.fire(rules, facts);
		}
	}

	/**
	 * Method initialize rules applicable for Client Agent
	 */
	protected List<AgentRule> getClientRules(RulesController<ClientAgentProps, ClientNode> rulesController) {
		return emptyList();
	}

	/**
	 * Method initialize rules applicable for Scheduler Agent
	 */
	protected List<AgentRule> getSchedulerRules(RulesController<SchedulerAgentProps, SchedulerNode> rulesController) {
		return emptyList();
	}

	/**
	 * Method initialize rules applicable for Cloud Network Agent
	 */
	protected List<AgentRule> getCNARules(RulesController<CloudNetworkAgentProps, CloudNetworkNode> rulesController) {
		return emptyList();
	}

	/**
	 * Method initialize rules applicable for Server Agent
	 */
	protected List<AgentRule> getServerRules(RulesController<ServerAgentProps, ServerNode> rulesController) {
		return emptyList();
	}

	/**
	 * Method initialize rules applicable for Green Energy Agent
	 */
	protected List<AgentRule> getGreenEnergyRules(
			RulesController<GreenEnergyAgentProps, GreenEnergyNode> rulesController) {
		return emptyList();
	}

	/**
	 * Method initialize rules applicable for Monitoring Agent
	 */
	protected List<AgentRule> getMonitoringRules(
			RulesController<MonitoringAgentProps, MonitoringNode> rulesController) {
		return emptyList();
	}

	private void initializeRules(RulesController<?, ?> rulesController) {
		agentRules = new ArrayList<>(switch (rulesController.getAgentProps().getAgentType()) {
			case SCHEDULER -> getSchedulerRules((RulesController<SchedulerAgentProps, SchedulerNode>) rulesController);
			case CLIENT -> getClientRules((RulesController<ClientAgentProps, ClientNode>) rulesController);
			case CLOUD_NETWORK ->
					getCNARules((RulesController<CloudNetworkAgentProps, CloudNetworkNode>) rulesController);
			case SERVER -> getServerRules((RulesController<ServerAgentProps, ServerNode>) rulesController);
			case GREEN_ENERGY ->
					getGreenEnergyRules((RulesController<GreenEnergyAgentProps, GreenEnergyNode>) rulesController);
			case MONITORING ->
					getMonitoringRules((RulesController<MonitoringAgentProps, MonitoringNode>) rulesController);
			default -> new ArrayList<AgentRule>();
		});
	}

}
