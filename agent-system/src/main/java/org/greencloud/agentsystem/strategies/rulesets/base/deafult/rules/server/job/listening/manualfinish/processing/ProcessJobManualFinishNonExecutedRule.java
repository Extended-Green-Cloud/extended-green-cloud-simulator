package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.listening.manualfinish.processing;

import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.IN_PROGRESS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_MANUAL_FINISH_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_MANUAL_FINISH_HANDLE_NON_EXECUTED_RULE;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;

public class ProcessJobManualFinishNonExecutedRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessJobManualFinishNonExecutedRule.class);

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
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		final JobExecutionStatusEnum statusEnum = agentProps.getServerJobs().get(job);
		return !statusEnum.equals(IN_PROGRESS);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		agentProps.removeJob(job);

		if (agentProps.isDisabled() && agentProps.getServerJobs().isEmpty()) {
			logger.info("Server completed all planned jobs and is fully disabled.");
			agentNode.disableServer();
		}

		agentProps.updateGUI();
	}

	@Override
	public AgentRule copy() {
		return new ProcessJobManualFinishNonExecutedRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
