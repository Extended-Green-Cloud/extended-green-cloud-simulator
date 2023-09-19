package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.server.job.listening.manualfinish.processing;

import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.IN_PROGRESS;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.enums.rules.RuleType.JOB_MANUAL_FINISH_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_MANUAL_FINISH_HANDLE_NON_EXECUTED_RULE;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.server.ServerNode;

public class ProcessJobManualFinishNonExecutedRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	public ProcessJobManualFinishNonExecutedRule(final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController, 1);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_MANUAL_FINISH_HANDLER_RULE, JOB_MANUAL_FINISH_HANDLE_NON_EXECUTED_RULE,
				"handles job manual finish - job not being executed",
				"processing message about Job manual finish sent by Green Source");
	}

	@Override
	public boolean evaluateRule(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		final JobExecutionStatusEnum statusEnum = agentProps.getServerJobs().get(job);
		return !statusEnum.equals(IN_PROGRESS);
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final ClientJob job = facts.get(JOB);
		agentProps.removeJob(job);
		agentProps.updateGUI();
	}
}