package org.greencloud.rulescontroller;

import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_STEP;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.rulescontroller.strategy.StrategyConstructor.constructStrategy;
import static org.greencloud.rulescontroller.strategy.StrategyConstructor.constructStrategyForType;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.domain.facts.StrategyFacts;
import org.greencloud.rulescontroller.strategy.Strategy;
import org.slf4j.Logger;
import org.slf4j.MDC;

import com.gui.agents.AgentNode;

import jade.core.Agent;
import lombok.Getter;

/**
 * Class provides functionalities that handle agent behaviours via strategies
 */
@Getter
public class RulesController<T extends AgentProps, E extends AgentNode<T>> {

	private static final Logger logger = getLogger(RulesController.class);

	protected Agent agent;
	protected E agentNode;
	protected T agentProps;
	protected AtomicInteger latestStrategy;
	protected AtomicInteger latestAdaptedStrategy;
	protected ConcurrentMap<Integer, Strategy> strategies;
	protected String baseStrategy;

	public RulesController() {
		latestStrategy = new AtomicInteger(0);
		latestAdaptedStrategy = new AtomicInteger(0);
		strategies = new ConcurrentHashMap<>();
	}

	/**
	 * Method fires agent strategy for a set of facts
	 *
	 * @param facts set of facts based on which actions are going to be taken
	 */
	public void fire(final StrategyFacts facts) {
		try {
			final Strategy strategy = strategies.get((int) facts.get(STRATEGY_IDX));
			strategy.fireStrategy(facts);
		} catch (NullPointerException e) {
			logger.warn("Couldn't find any strategy of given index! Rule type: {} Rule step: {}",
					facts.get(RULE_TYPE), facts.get(RULE_STEP));
		}
	}

	/**
	 * Method initialize agent values
	 *
	 * @param agent      agent connected to the rules controller
	 * @param agentProps agent properties
	 * @param agentNode  GUI agent node
	 */
	public void setAgent(Agent agent, T agentProps, E agentNode, String baseStrategy) {
		this.agent = agent;
		this.agentProps = agentProps;
		this.agentNode = agentNode;
		this.baseStrategy = baseStrategy;
		this.strategies.put(latestStrategy.get(), constructStrategy(baseStrategy, this));
	}

	/**
	 * Method adds new agent's strategy
	 *
	 * @param type type of strategy that is to be added
	 * @param idx  index of the added strategy
	 */
	public void addModifiedStrategy(final String type, final int idx) {
		this.strategies.put(idx, constructStrategyForType(baseStrategy, type, this));
		this.latestStrategy.set(idx);
	}

	/**
	 * Method adds new agent's strategy
	 *
	 * @param type type of strategy that is to be added
	 * @param idx  index of the added strategy
	 */
	public void addNewStrategy(final String type, final int idx) {
		this.strategies.put(idx, constructStrategy(type, this));
		this.latestStrategy.set(idx);
	}

	/**
	 * Method verifies if the strategy is to be removed from the controller
	 *
	 * @param strategyForObject map containing strategies assigned to given objects
	 * @param strategyIdx    index of the strategy removed along with the object
	 * @return flag indicating if the strategy was removed
	 */
	public boolean removeStrategy(final ConcurrentMap<String, Integer> strategyForObject, final int strategyIdx) {
		if (strategyIdx != latestStrategy.get()
				&& strategyForObject.values().stream().noneMatch(val -> val == strategyIdx)) {

			MDC.put(MDC_STRATEGY_ID, valueOf(strategyIdx));
			logger.info("Removing strategy {} from the map.", strategyIdx);
			strategies.remove(strategyIdx);
			return true;
		}
		return false;
	}
}
