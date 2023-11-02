package org.greencloud.rulescontroller;

import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_STEP;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.rulescontroller.ruleset.RuleSetConstructor.constructModifiedRuleSetForType;
import static org.greencloud.rulescontroller.ruleset.RuleSetConstructor.constructRuleSet;
import static org.greencloud.rulescontroller.ruleset.RuleSetConstructor.constructRuleSetForType;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.gui.agents.AgentNode;
import org.greencloud.rulescontroller.ruleset.RuleSet;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.Agent;
import lombok.Getter;

/**
 * Class provides functionalities that handle agent behaviours via rule sets
 */
@Getter
public class RulesController<T extends AgentProps, E extends AgentNode<T>> {

	private static final Logger logger = getLogger(RulesController.class);

	protected Agent agent;
	protected E agentNode;
	protected T agentProps;
	protected AtomicInteger latestRuleSet;
	protected AtomicInteger latestAdaptedRuleSet;
	protected ConcurrentMap<Integer, RuleSet> ruleSets;
	protected String baseRuleSet;

	public RulesController() {
		latestRuleSet = new AtomicInteger(0);
		latestAdaptedRuleSet = new AtomicInteger(0);
		ruleSets = new ConcurrentHashMap<>();
	}

	/**
	 * Method fires agent rule set for a set of facts
	 *
	 * @param facts set of facts based on which actions are going to be taken
	 */
	public void fire(final RuleSetFacts facts) {
		try {
			final RuleSet ruleSet = ruleSets.get((int) facts.get(RULE_SET_IDX));
			ruleSet.fireRuleSet(facts);
		} catch (NullPointerException e) {
			logger.warn("Couldn't find any rule set of given index! Rule type: {} Rule step: {}",
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
	public void setAgent(Agent agent, T agentProps, E agentNode, String baseRuleSet) {
		this.agent = agent;
		this.agentProps = agentProps;
		this.agentNode = agentNode;
		this.baseRuleSet = baseRuleSet;
		this.ruleSets.put(latestRuleSet.get(), constructRuleSet(baseRuleSet, this));
	}

	/**
	 * Method adds new agent's rule set
	 *
	 * @param type type of rule set that is to be added
	 * @param idx  index of the added rule set
	 */
	public void addModifiedRuleSet(final String type, final int idx) {
		this.ruleSets.put(idx, constructRuleSetForType(baseRuleSet, type, this));
		this.latestRuleSet.set(idx);
	}

	/**
	 * Method adds new agent's rule set
	 *
	 * @param modifications modifications to current rule set that are to be applied
	 */
	public void addModifiedRuleSetFromCurrent(final RuleSet modifications, final int idx) {
		final RuleSet connectedRuleSet = new RuleSet(modifications, this);
		this.ruleSets.put(idx, constructModifiedRuleSetForType(ruleSets.get(latestRuleSet.get()), connectedRuleSet));
	}

	/**
	 * Method adds new agent's rule set
	 *
	 * @param type type of rule set that is to be added
	 * @param idx  index of the added ruleSet
	 */
	public void addNewRuleSet(final String type, final int idx) {
		this.ruleSets.put(idx, constructRuleSet(type, this));
		this.latestRuleSet.set(idx);
	}

	/**
	 * Method verifies if the rule set is to be removed from the controller
	 *
	 * @param ruleSetForObject map containing rule sets assigned to given objects
	 * @param ruleSetIdx       index of the rule set removed along with the object
	 * @return flag indicating if the rule set was removed
	 */
	public boolean removeRuleSet(final ConcurrentMap<String, Integer> ruleSetForObject, final int ruleSetIdx) {
		if (ruleSetIdx != latestRuleSet.get()
				&& ruleSetForObject.values().stream().noneMatch(val -> val == ruleSetIdx)) {

			MDC.put(MDC_RULE_SET_ID, valueOf(ruleSetIdx));
			logger.info("Removing rule set {} from the map.", ruleSetIdx);
			ruleSets.remove(ruleSetIdx);
			return true;
		}
		return false;
	}
}
