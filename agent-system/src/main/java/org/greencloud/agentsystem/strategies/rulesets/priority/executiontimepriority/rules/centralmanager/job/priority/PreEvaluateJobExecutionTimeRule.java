package org.greencloud.agentsystem.strategies.rulesets.priority.executiontimepriority.rules.centralmanager.job.priority;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.INITIAL_FACTS;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.ASK_FOR_FASTEST_EXECUTION_TIME;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PRE_EVALUATE_JOB_PRIORITY_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.initiate.InitiateRequest;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class PreEvaluateJobExecutionTimeRule extends AgentBasicRule<CentralManagerAgentProps, CMANode> {

	private static final Logger logger = getLogger(PreEvaluateJobExecutionTimeRule.class);

	public PreEvaluateJobExecutionTimeRule(
			final RulesController<CentralManagerAgentProps, CMANode> controller) {
		super(controller);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Asking RMAs about information of fastest execution time of job {}.", job.getJobId());

		facts.put(INITIAL_FACTS, facts);
		agent.addBehaviour(InitiateRequest.create(agent, facts, ASK_FOR_FASTEST_EXECUTION_TIME, controller));
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PRE_EVALUATE_JOB_PRIORITY_RULE,
				"get job execution time from RMAs",
				"when Central Manager receives new job, it evaluates its execution time");
	}

	@Override
	public AgentRule copy() {
		return new PreEvaluateJobExecutionTimeRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
