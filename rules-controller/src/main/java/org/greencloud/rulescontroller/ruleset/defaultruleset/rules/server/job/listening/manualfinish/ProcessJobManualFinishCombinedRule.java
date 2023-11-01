package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.manualfinish;

import static java.util.Objects.nonNull;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.greencloud.commons.enums.rules.RuleType.JOB_MANUAL_FINISH_HANDLER_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getJobByInstanceId;

import java.util.List;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.gui.agents.server.ServerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.combined.AgentCombinedRule;
import org.greencloud.rulescontroller.rule.combined.domain.AgentCombinedRuleType;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.manualfinish.processing.ProcessJobManualFinishInProgressRule;
import org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.manualfinish.processing.ProcessJobManualFinishNonExecutedRule;

public class ProcessJobManualFinishCombinedRule extends AgentCombinedRule<ServerAgentProps, ServerNode> {

	public ProcessJobManualFinishCombinedRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller, AgentCombinedRuleType.EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_MANUAL_FINISH_HANDLER_RULE,
				"handles job manual finish",
				"processing message about Job manual finish sent by Green Source");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessJobManualFinishInProgressRule(controller),
				new ProcessJobManualFinishNonExecutedRule(controller)
		);
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final JobInstanceIdentifier jobInstance = facts.get(MESSAGE_CONTENT);
		final ClientJob job = getJobByInstanceId(jobInstance.getJobInstanceId(), agentProps.getServerJobs());

		if (nonNull(job) && agentProps.getServerJobs().containsKey(job)) {
			facts.put(JOB, job);
			return true;
		}
		return false;
	}
}
