package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.job.priority;

import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.COMPUTE_JOB_PRIORITY_RULE;
import static org.greencloud.agentsystem.strategies.algorithms.priority.PriorityEstimator.evaluatePriorityBasedOnDeadline;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

public class ComputeJobPriorityRule extends AgentBasicRule<CentralManagerAgentProps, CMANode> {

	public ComputeJobPriorityRule(final RulesController<CentralManagerAgentProps, CMANode> controller) {
		super(controller);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		final double timeToDeadline = evaluatePriorityBasedOnDeadline(job);

		facts.put(RESULT, timeToDeadline);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(COMPUTE_JOB_PRIORITY_RULE,
				"compute priority of client job",
				"when Central Manager receives new job, it computes its priority based on deadline");
	}

	@Override
	public AgentRule copy() {
		return new ComputeJobPriorityRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
