package org.greencloud.agentsystem.strategies.rulesets.priority.executiontimeenhancedpriority.rules.centralmanager.job.priority;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB_PRIORITY_FACTS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.COMPUTE_JOB_PRIORITY_RULE;
import static org.greencloud.agentsystem.strategies.algorithms.priority.PriorityEstimator.evaluatePriorityBasedOnEnhancedFastestExecutionTime;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGES;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class ComputePriorityEnhancedExecutionTimeRule extends AgentBasicRule<CentralManagerAgentProps, CMANode> {

	private static final Logger logger = getLogger(ComputePriorityEnhancedExecutionTimeRule.class);

	public ComputePriorityEnhancedExecutionTimeRule(
			final RulesController<CentralManagerAgentProps, CMANode> controller) {
		super(controller);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		final RuleSetFacts relatedFacts = facts.get(JOB_PRIORITY_FACTS);
		final Collection<ACLMessage> informs = relatedFacts.get(MESSAGES);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));

		if (informs.isEmpty()) {
			logger.info("No responses received! Setting priority to 0.");
			facts.put(RESULT, 0);
			return;
		}
		final double priority = evaluatePriorityBasedOnEnhancedFastestExecutionTime(informs);
		logger.info("Priority of job {} is {}.", job.getJobId(), priority);

		facts.put(RESULT, priority);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(COMPUTE_JOB_PRIORITY_RULE,
				"compute job priority based on estimated execution time enhanced with estimation errors",
				"when Central Manager receives RMA info, it computes job priority");
	}

	@Override
	public AgentRule copy() {
		return new ComputePriorityEnhancedExecutionTimeRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
