package org.greencloud.agentsystem.strategies.deault.rules.regionalmanager.job.priority;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PRE_EVALUATE_JOB_PRIORITY_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class PreEvaluateJobPriorityForRMARule extends AgentBasicRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(PreEvaluateJobPriorityForRMARule.class);

	public PreEvaluateJobPriorityForRMARule(final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PRE_EVALUATE_JOB_PRIORITY_RULE,
				"perform job pre-evaluation",
				"rule evaluates and collects data needed to prioritize new job in RMA");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Job {} does not require pre-processing. Computing job priority.", job.getJobId());

		agentProps.getJobsToBeExecuted().put(job);
	}

	@Override
	public AgentRule copy() {
		return new PreEvaluateJobPriorityForRMARule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
