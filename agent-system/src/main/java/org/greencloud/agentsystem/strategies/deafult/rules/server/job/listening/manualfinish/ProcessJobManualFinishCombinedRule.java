package org.greencloud.agentsystem.strategies.deafult.rules.server.job.listening.manualfinish;

import static java.util.Objects.nonNull;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_MANUAL_FINISH_HANDLER_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getJobByInstanceId;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE_CONTENT;
import static org.jrba.rulesengine.types.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;

import java.util.List;

import org.greencloud.agentsystem.strategies.deafult.rules.server.job.listening.manualfinish.processing.ProcessJobManualFinishInProgressRule;
import org.greencloud.agentsystem.strategies.deafult.rules.server.job.listening.manualfinish.processing.ProcessJobManualFinishNonExecutedRule;
import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.instance.JobInstanceIdentifier;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;

public class ProcessJobManualFinishCombinedRule extends AgentCombinedRule<ServerAgentProps, ServerNode> {

	public ProcessJobManualFinishCombinedRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller, EXECUTE_FIRST);
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

	@Override
	public AgentRule copy() {
		return new ProcessJobManualFinishCombinedRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
