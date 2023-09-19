package org.greencloud.rulescontroller.strategy.defaultstrategy.rules.server.job.listening.startcheck;

import static org.greencloud.commons.constants.LoggingConstants.MDC_JOB_ID;
import static org.greencloud.commons.constants.LoggingConstants.MDC_STRATEGY_ID;
import static org.greencloud.commons.constants.FactTypeConstants.JOB_ID;
import static org.greencloud.commons.constants.FactTypeConstants.MESSAGE;
import static org.greencloud.commons.constants.FactTypeConstants.STRATEGY_IDX;
import static org.greencloud.commons.enums.rules.RuleType.JOB_STATUS_HANDLER_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getCurrentJobInstance;
import static java.lang.String.valueOf;
import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Map;

import org.greencloud.rulescontroller.RulesController;
import org.greencloud.rulescontroller.strategy.defaultstrategy.rules.server.job.listening.startcheck.processing.ProcessJobStartCheckJobNotFoundRule;
import org.greencloud.rulescontroller.strategy.defaultstrategy.rules.server.job.listening.startcheck.processing.ProcessJobStartCheckJobNotStartedRule;
import org.greencloud.rulescontroller.strategy.defaultstrategy.rules.server.job.listening.startcheck.processing.ProcessJobStartCheckJobStartedRule;
import org.greencloud.rulescontroller.domain.AgentRuleDescription;
import org.greencloud.rulescontroller.rule.AgentRule;
import org.greencloud.rulescontroller.rule.combined.AgentCombinedRule;
import org.greencloud.rulescontroller.rule.combined.domain.AgentCombinedRuleType;
import org.slf4j.Logger;
import org.slf4j.MDC;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.greencloud.commons.domain.facts.StrategyFacts;
import com.gui.agents.server.ServerNode;

import jade.lang.acl.ACLMessage;

public class ProcessJobStartCheckRequestCombinedRule extends AgentCombinedRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessJobStartCheckRequestCombinedRule.class);

	public ProcessJobStartCheckRequestCombinedRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller, AgentCombinedRuleType.EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_HANDLER_RULE,
				"handles start check request",
				"processing CNA message checking job start status");
	}

	@Override
	protected List<AgentRule> constructRules() {
		return List.of(
				new ProcessJobStartCheckJobNotFoundRule(controller),
				new ProcessJobStartCheckJobNotStartedRule(controller),
				new ProcessJobStartCheckJobStartedRule(controller)
		);
	}

	@Override
	public void executeRule(final StrategyFacts facts) {
		final String jobId = ((ACLMessage) facts.get(MESSAGE)).getContent();
		final Map.Entry<ClientJob, JobExecutionStatusEnum> jobInstance =
				getCurrentJobInstance(jobId, agentProps.getServerJobs());

		facts.put(JOB_ID, ofNullable(jobInstance));
		MDC.put(MDC_JOB_ID, jobId);
		MDC.put(MDC_STRATEGY_ID, valueOf((int) facts.get(STRATEGY_IDX)));
		logger.info("Received request to verify job start status {}", jobId);
	}
}
