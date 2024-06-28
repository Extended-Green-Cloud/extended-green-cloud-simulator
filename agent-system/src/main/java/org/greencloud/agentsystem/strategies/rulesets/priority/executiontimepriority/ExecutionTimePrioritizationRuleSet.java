package org.greencloud.agentsystem.strategies.rulesets.priority.executiontimepriority;

import static org.apache.commons.collections4.CollectionUtils.union;
import static org.greencloud.agentsystem.strategies.domain.PrioritizationAlgorithmTypes.ESTIMATED_DURATION_BASED_PRIORITY;

import java.util.List;

import org.greencloud.agentsystem.strategies.rulesets.priority.executiontimepriority.rules.centralmanager.job.priority.AskForFastestExecutionTime;
import org.greencloud.agentsystem.strategies.rulesets.priority.executiontimepriority.rules.centralmanager.job.priority.ComputePriorityExecutionTimeRule;
import org.greencloud.agentsystem.strategies.rulesets.priority.executiontimepriority.rules.centralmanager.job.priority.PreEvaluateJobExecutionTimeRule;
import org.greencloud.agentsystem.strategies.rulesets.priority.executiontimepriority.rules.regionalmanager.initial.StartInitialRegionalManagerBehaviours;
import org.greencloud.agentsystem.strategies.rulesets.priority.executiontimepriority.rules.regionalmanager.listening.priority.ListenForJobTimeEstimationRequest;
import org.greencloud.agentsystem.strategies.rulesets.priority.executiontimepriority.rules.regionalmanager.listening.priority.processing.ProcessJobTimeEstimationRequest;
import org.greencloud.agentsystem.strategies.rulesets.priority.executiontimepriority.rules.regionalmanager.priority.RequestJobExecutionTimeRule;
import org.greencloud.agentsystem.strategies.rulesets.priority.executiontimepriority.rules.server.initial.StartInitialServerBehaviours;
import org.greencloud.agentsystem.strategies.rulesets.priority.executiontimepriority.rules.server.job.listening.priority.ListenForRMAJobTimeEstimationRequest;
import org.greencloud.agentsystem.strategies.rulesets.priority.executiontimepriority.rules.server.job.listening.priority.processing.ProcessRMAJobTimeEstimationRequest;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.ruleset.RuleSet;

/**
 * Rule set applied when jobs are to be prioritized by their estimated execution time
 */
public class ExecutionTimePrioritizationRuleSet extends RuleSet {

	public ExecutionTimePrioritizationRuleSet() {
		super(ESTIMATED_DURATION_BASED_PRIORITY, false);
		this.agentRules = initialRules();
	}

	private List<AgentRule> initialRules() {
		return union(cmaRules(), union(rmaRules(), serverRules())).stream().toList();
	}

	protected List<AgentRule> cmaRules() {
		return List.of(
				new PreEvaluateJobExecutionTimeRule(null),
				new AskForFastestExecutionTime(null),
				new ComputePriorityExecutionTimeRule(null)
		);
	}

	protected List<AgentRule> rmaRules() {
		return List.of(
				new StartInitialRegionalManagerBehaviours(null),
				new ProcessJobTimeEstimationRequest(null),
				new ListenForJobTimeEstimationRequest(null, this),
				new RequestJobExecutionTimeRule(null)
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
