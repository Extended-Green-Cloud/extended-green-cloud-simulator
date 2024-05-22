package org.greencloud.agentsystem.strategies.executiontimebased;

import static org.apache.commons.collections4.CollectionUtils.union;
import static org.greencloud.agentsystem.strategies.domain.EGCSRuleSetTypes.EXECUTION_TIME_PRIORITIZATION_RULE_SET;

import java.util.List;

import org.greencloud.agentsystem.strategies.executiontimebased.centralmanager.job.priority.AskForFastestExecutionTime;
import org.greencloud.agentsystem.strategies.executiontimebased.centralmanager.job.priority.ComputeJobPriorityBasedOnExecutionTimeRule;
import org.greencloud.agentsystem.strategies.executiontimebased.centralmanager.job.priority.PreEvaluateJobExecutionTimeRule;
import org.greencloud.agentsystem.strategies.executiontimebased.regionalmanager.initial.StartInitialRegionalManagerBehaviours;
import org.greencloud.agentsystem.strategies.executiontimebased.regionalmanager.job.listening.ListenForJobTimeEstimationRequest;
import org.greencloud.agentsystem.strategies.executiontimebased.regionalmanager.job.listening.processing.ProcessJobTimeEstimationRequest;
import org.greencloud.agentsystem.strategies.executiontimebased.regionalmanager.job.request.RequestJobExecutionTime;
import org.greencloud.agentsystem.strategies.executiontimebased.server.initial.StartInitialServerBehaviours;
import org.greencloud.agentsystem.strategies.executiontimebased.server.job.listening.ListenForRMAJobTimeEstimationRequest;
import org.greencloud.agentsystem.strategies.executiontimebased.server.job.listening.processing.ProcessRMAJobTimeEstimationRequest;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.ruleset.RuleSet;

/**
 * Rule set applied when jobs are to be prioritized by their estimated execution time
 */
public class ExecutionTimeBasedPrioritizationRuleSet extends RuleSet {

	public ExecutionTimeBasedPrioritizationRuleSet() {
		super(EXECUTION_TIME_PRIORITIZATION_RULE_SET, false);
		this.agentRules = initialRules();
	}

	private List<AgentRule> initialRules() {
		return union(cmaRules(), union(rmaRules(), serverRules())).stream().toList();
	}

	protected List<AgentRule> cmaRules() {
		return List.of(
				new PreEvaluateJobExecutionTimeRule(null),
				new AskForFastestExecutionTime(null),
				new ComputeJobPriorityBasedOnExecutionTimeRule(null)
		);
	}

	protected List<AgentRule> rmaRules() {
		return List.of(
				new StartInitialRegionalManagerBehaviours(null),
				new ProcessJobTimeEstimationRequest(null),
				new ListenForJobTimeEstimationRequest(null, this),
				new RequestJobExecutionTime(null)
		);
	}

	protected List<AgentRule> serverRules() {
		return List.of(
				new StartInitialServerBehaviours(null),
				new ProcessRMAJobTimeEstimationRequest(null),
				new ListenForRMAJobTimeEstimationRequest(null, this)
		);
	}
}
