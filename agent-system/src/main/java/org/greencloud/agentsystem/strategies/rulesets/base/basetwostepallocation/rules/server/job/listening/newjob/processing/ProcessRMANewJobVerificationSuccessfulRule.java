package org.greencloud.agentsystem.strategies.rulesets.base.basetwostepallocation.rules.server.job.listening.newjob.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB_ACCEPTED;
import static org.greencloud.commons.enums.job.JobExecutionResultEnum.ACCEPTED;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.ACCEPTED_JOB_STATUSES;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_RECEIVER_HANDLE_NEW_JOB_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_VERIFICATION_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToClientJob;
import static org.greencloud.commons.utils.job.JobUtils.getJobCount;
import static org.greencloud.commons.utils.resources.ResourcesUtilization.areSufficient;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJobWithServer;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ProcessRMANewJobVerificationSuccessfulRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessRMANewJobVerificationSuccessfulRule.class);

	public ProcessRMANewJobVerificationSuccessfulRule(final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_VERIFICATION_RULE, NEW_JOB_RECEIVER_HANDLE_NEW_JOB_RULE,
				"handles new RMA job request",
				"handling new job sent by RMA");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ClientJobWithServer job = facts.get(JOB);
		return areSufficient(agentProps.resources(), job.getRequiredResources()) &&
				!agentProps.isHasError()
				&& agentProps.canTakeIntoProcessing();
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJobWithServer job = facts.get(JOB);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Accepting RMA job {} execution request.", job.getJobId());

		agentProps.stoppedJobProcessing();
		agentProps.incrementJobCounter(job.getJobId(), ACCEPTED);
		agentNode.updateClientNumber(getJobCount(agentProps.getServerJobs(), ACCEPTED_JOB_STATUSES));
		agentProps.getJobsForExecutionQueue().add(mapToClientJob(job));

		logger.info("Announcing new job {} in network!", job.getJobId());
		agentNode.announceClientJob();

		facts.put(JOB_ACCEPTED, job);
	}

	@Override
	public AgentRule copy() {
		return new ProcessRMANewJobVerificationSuccessfulRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
