package org.greencloud.agentsystem.strategies.rulesets.base.deafult.rules.centralmanager.job.allocation;

import static java.lang.String.valueOf;
import static org.apache.commons.lang3.StringUtils.join;
import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.enums.job.JobExecutionStateEnum.replaceStatusToActive;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.NEW_JOB_ALLOCATION_RULE;
import static org.greencloud.commons.utils.facts.JobAllocationFactsFactory.constructFactsForJobsAllocationHandling;
import static org.greencloud.commons.utils.messaging.constants.MessageConversationConstants.PROCESSING_JOB_ID;
import static org.greencloud.commons.utils.messaging.factory.JobStatusMessageFactory.prepareJobStatusMessageForClient;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.basic.PowerJob;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class AllocateNewClientJobsRule extends AgentBasicRule<CentralManagerAgentProps, CMANode> {
	private static final Logger logger = getLogger(AllocateNewClientJobsRule.class);

	public AllocateNewClientJobsRule(final RulesController<CentralManagerAgentProps, CMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(NEW_JOB_ALLOCATION_RULE,
				"allocate jobs between RMAs",
				"when batch of new job is received, it is being allocated between RMAs");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final List<ClientJob> jobs = facts.get(JOBS);
		final String indexes = join(jobs.stream().map(PowerJob::getJobId).toList(), ",");

		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Allocating next jobs batch for jobs with ids: {}.", indexes);

		jobs.forEach(job -> {
			replaceStatusToActive(agentProps.getClientJobs(), job);
			agent.send(prepareJobStatusMessageForClient(job, PROCESSING_JOB_ID, facts.get(RULE_SET_IDX)));
		});
		controller.fire(constructFactsForJobsAllocationHandling(facts.get(RULE_SET_IDX), jobs));
	}

	@Override
	public AgentRule copy() {
		return new AllocateNewClientJobsRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
