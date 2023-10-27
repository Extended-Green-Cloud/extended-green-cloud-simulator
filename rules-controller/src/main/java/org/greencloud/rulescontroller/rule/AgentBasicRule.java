package org.greencloud.rulescontroller.rule;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.rulescontroller.mvel.MVELObjectType.getObjectForType;
import static org.greencloud.rulescontroller.rule.AgentRuleType.BASIC;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.tomcat.util.buf.StringUtils;
import org.greencloud.commons.args.agent.AgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.enums.rules.RuleStepType;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rest.domain.RuleRest;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.core.BasicRule;
import org.mvel2.MVEL;
import org.slf4j.Logger;

import com.gui.agents.AgentNode;

import jade.core.Agent;
import lombok.Getter;

/**
 * Abstract class defining structure of a rule used in given system rule set
 */
@Getter
@SuppressWarnings("unchecked")
public class AgentBasicRule<T extends AgentProps, E extends AgentNode<T>> extends BasicRule implements AgentRule {

	private static final Logger logger = getLogger(AgentBasicRule.class);

	protected RulesController<T, E> controller;
	protected T agentProps;
	protected E agentNode;
	protected Agent agent;
	protected String agentType;
	protected String ruleType;
	protected String subRuleType;
	protected RuleStepType stepType;
	protected boolean isRuleStep;

	protected Map<String, Object> initialParameters;

	protected String imports;
	protected Serializable executeExpression;
	protected Serializable evaluateExpression;

	/**
	 * Constructor
	 *
	 * @param ruleRest rest representation of agent rule
	 */
	public AgentBasicRule(final RuleRest ruleRest) {
		super();
		this.isRuleStep = nonNull(ruleRest.getStepType());
		this.name = ruleRest.getName();
		this.description = ruleRest.getDescription();
		this.ruleType = ruleRest.getType();
		this.subRuleType = ruleRest.getSubType();
		this.stepType = ruleRest.getStepType();
		this.initialParameters = new HashMap<>();
		this.priority = ofNullable(ruleRest.getPriority()).orElse(super.priority);
		this.agentType = ruleRest.getAgentType();

		if (nonNull(ruleRest.getInitialParams())) {
			ruleRest.getInitialParams().forEach((key, value) -> initialParameters.put(key, getObjectForType(value)));
		}

		imports = StringUtils.join(ruleRest.getImports(), ' ');
		imports = imports + " import org.slf4j.MDC;";
		imports = imports + " import org.greencloud.commons.constants.LoggingConstants;";
		if (nonNull(ruleRest.getExecute())) {
			this.executeExpression = MVEL.compileExpression(imports + " " + ruleRest.getExecute());
		}
		if (nonNull(ruleRest.getEvaluate())) {
			this.evaluateExpression = MVEL.compileExpression(imports + " " + ruleRest.getEvaluate());
		}
	}

	/**
	 * Constructor
	 *
	 * @param rulesController rules controller connected to the agent
	 */
	protected AgentBasicRule(final RulesController<T, E> rulesController) {
		this.agent = rulesController.getAgent();
		this.agentProps = rulesController.getAgentProps();
		this.agentNode = rulesController.getAgentNode();
		this.controller = rulesController;
		this.isRuleStep = false;

		final AgentRuleDescription ruleDescription = initializeRuleDescription();
		this.name = ruleDescription.ruleName();
		this.stepType = ruleDescription.stepType();
		this.description = ruleDescription.ruleDescription();
		this.ruleType = ruleDescription.ruleType();
		this.subRuleType = ruleDescription.subType();
	}

	/**
	 * Constructor
	 *
	 * @param rulesController rules controller connected to the agent
	 * @param priority        priority of the rule execution
	 */
	protected AgentBasicRule(final RulesController<T, E> rulesController, final int priority) {
		this(rulesController);
		this.priority = priority;
	}

	/**
	 * Method connects agent rule with controller
	 *
	 * @param rulesController rules controller connected to the agent
	 */
	@Override
	public void connectToController(final RulesController<?, ?> rulesController) {
		this.agent = rulesController.getAgent();
		this.agentProps = (T) rulesController.getAgentProps();
		this.agentNode = (E) rulesController.getAgentNode();
		this.controller = (RulesController<T, E>) rulesController;

		if (nonNull(initialParameters)) {
			initialParameters.put("agent", agent);
			initialParameters.put("agentProps", agentProps);
			initialParameters.put("agentNode", agentNode);
			initialParameters.put("controller", controller);
			initialParameters.put("logger", logger);
			initialParameters.put("facts", null);
		}
	}

	@Override
	public AgentRuleType getAgentRuleType() {
		return BASIC;
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		return ruleType.equals(facts.get(RULE_TYPE));
	}

	@Override
	public boolean evaluate(final Facts facts) {
		if (isNull(evaluateExpression)) {
			return evaluateRule((RuleSetFacts) facts);
		} else {
			initialParameters.replace("facts", facts);
			return (boolean) MVEL.executeExpression(evaluateExpression, initialParameters);
		}
	}

	@Override
	public void execute(final Facts facts) throws Exception {
		if (isNull(executeExpression)) {
			executeRule((RuleSetFacts) facts);
		} else {
			initialParameters.replace("facts", facts);
			MVEL.executeExpression(executeExpression, initialParameters);
		}
	}
}
