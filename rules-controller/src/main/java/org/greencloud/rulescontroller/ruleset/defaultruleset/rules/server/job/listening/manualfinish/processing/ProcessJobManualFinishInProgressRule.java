package org.greencloud.rulescontroller.ruleset.defaultruleset.rules.server.job.listening.manualfinish.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.constants.FactTypeConstants.JOB;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_FINISH_INFORM;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_SET_IDX;
import static org.greencloud.commons.constants.FactTypeConstants.RULE_TYPE;
import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.IN_PROGRESS;
import static org.greencloud.commons.enums.rules.RuleType.JOB_MANUAL_FINISH_HANDLER_RULE;
import static org.greencloud.commons.enums.rules.RuleType.JOB_MANUAL_FINISH_HANDLE_IN_PROGRESS_RULE;
import static org.greencloud.commons.enums.rules.RuleType.PROCESS_FINISH_JOB_EXECUTION_RULE;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.facts.RuleSetFacts;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.greencloud.gui.agents.server.ServerNode;
import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentBasicRule;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ProcessJobManualFinishInProgressRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessJobManualFinishInProgressRule.class);

	public ProcessJobManualFinishInProgressRule(final RulesController<ServerAgentProps, ServerNode> rulesController) {
		super(rulesController, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_MANUAL_FINISH_HANDLER_RULE, JOB_MANUAL_FINISH_HANDLE_IN_PROGRESS_RULE,
				"handles job manual finish - job in progress",
				"processing message about Job manual finish sent by Green Source");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		final JobExecutionStatusEnum statusEnum = agentProps.getServerJobs().get(job);

		return statusEnum.equals(IN_PROGRESS);
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.debug(
				"Information about finishing job with id {} does not reach the green source. Finished executing the job for {}",
				job.getJobId(), job.getClientIdentifier());

		final boolean informCNA = agentProps.getServerJobs().keySet().stream()
				.filter(clientJob -> clientJob.getJobId().equals(job.getJobId()))
				.count() == 1;

		facts.put(JOB_FINISH_INFORM, informCNA);
		facts.put(RULE_TYPE, PROCESS_FINISH_JOB_EXECUTION_RULE);
		controller.fire(facts);
	}
}
