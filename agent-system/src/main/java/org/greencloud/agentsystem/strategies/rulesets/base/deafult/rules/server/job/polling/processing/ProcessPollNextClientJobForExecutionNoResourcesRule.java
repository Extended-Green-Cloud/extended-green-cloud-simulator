package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.server.job.polling.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.SERVER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOB;
import static org.greencloud.commons.enums.job.JobExecutionStatusEnum.PROCESSING;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_POLLING_HANDLE_NO_RESOURCES_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_POLLING_RULE;
import static org.greencloud.commons.utils.resources.ResourcesUtilization.areSufficient;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESOURCES;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;

import org.greencloud.commons.args.agent.server.agent.ServerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.resources.Resource;
import org.greencloud.gui.agents.server.ServerNode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ProcessPollNextClientJobForExecutionNoResourcesRule extends AgentBasicRule<ServerAgentProps, ServerNode> {

	private static final Logger logger = getLogger(ProcessPollNextClientJobForExecutionNoResourcesRule.class);

	public ProcessPollNextClientJobForExecutionNoResourcesRule(
			final RulesController<ServerAgentProps, ServerNode> controller) {
		super(controller, 2);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_POLLING_RULE, NEW_JOB_POLLING_HANDLE_NO_RESOURCES_RULE,
				"do not execute job when there are not enough Server resources",
				"when there are not enough Server resources, postpone job execution");
	}

	@Override
	public boolean evaluateRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);
		final Map<String, Resource> availableResources = facts.get(RESOURCES);

		return !areSufficient(availableResources, job.getRequiredResources());
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final ClientJob job = facts.get(JOB);

		MDC.put(MDC_JOB_ID, job.getJobId());
		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Not enough resources to being execution of job {}. Putting the job back to the queue.",
				job.getJobId());

		agentProps.getServerJobs().replace(job, PROCESSING);
		agentProps.getJobsForExecutionQueue().add(job);
	}

	@Override
	public AgentRule copy() {
		return new ProcessPollNextClientJobForExecutionNoResourcesRule(controller);
	}

	@Override
	public String getAgentType() {
		return SERVER.getName();
	}
}
