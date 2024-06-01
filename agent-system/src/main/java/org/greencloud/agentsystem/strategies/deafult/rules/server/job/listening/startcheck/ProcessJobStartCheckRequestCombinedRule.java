package org.greencloud.agentsystem.strategies.deafult.rules.server.job.listening.startcheck;

import static java.lang.String.valueOf;
import static java.util.Optional.ofNullable;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB_ID;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.JOB_STATUS_HANDLER_RULE;
import static org.greencloud.commons.utils.job.JobUtils.getCurrentJobInstance;
import static org.jrba.rulesengine.constants.FactTypeConstants.MESSAGE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.jrba.rulesengine.types.rulecombinationtype.AgentCombinedRuleTypeEnum.EXECUTE_FIRST;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Map;

import org.greencloud.agentsystem.strategies.deafult.rules.server.job.listening.startcheck.processing.ProcessJobStartCheckJobNotFoundRule;
import org.greencloud.agentsystem.strategies.deafult.rules.server.job.listening.startcheck.processing.ProcessJobStartCheckJobNotStartedRule;
import org.greencloud.agentsystem.strategies.deafult.rules.server.job.listening.startcheck.processing.ProcessJobStartCheckJobStartedRule;
import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.enums.job.JobExecutionStatusEnum;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.rule.combined.AgentCombinedRule;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.lang.acl.ACLMessage;

public class ProcessJobStartCheckRequestCombinedRule extends AgentCombinedRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessJobStartCheckRequestCombinedRule.class);

	public ProcessJobStartCheckRequestCombinedRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller, EXECUTE_FIRST);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(JOB_STATUS_HANDLER_RULE,
				"handles start check request",
				"processing RMA message checking job start status");
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
	public void executeRule(final RuleSetFacts facts) {
		final String jobId = ((ACLMessage) facts.get(MESSAGE)).getContent();
		final Map.Entry<ClientJob, JobExecutionStatusEnum> jobInstance =
				getCurrentJobInstance(jobId, agentProps.getServerJobs());

		facts.put(JOB_ID, ofNullable(jobInstance));
		MDC.put(MDC_JOB_ID, jobId);
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Received request to verify job start status {}", jobId);
	}

	@Override
	public AgentRule copy() {
		return new ProcessJobStartCheckRequestCombinedRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
