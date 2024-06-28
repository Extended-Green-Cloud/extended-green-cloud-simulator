package org.greencloud.agentsystem.strategies.rulesets.base.basetwostepallocation.rules.regionalmanager.job.allocation.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.REGIONAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.ALLOCATION;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_NEW_JOB_UNSUCCESSFUL_ALLOCATION_RULE;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.greencloud.commons.args.agent.regionalmanager.agent.RegionalManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.regionalmanager.RMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ProcessNotAllocatedServerJobsRule extends AgentBasicRule<RegionalManagerAgentProps, RMANode> {

	private static final Logger logger = getLogger(ProcessNotAllocatedServerJobsRule.class);

	public ProcessNotAllocatedServerJobsRule(
			final RulesController<RegionalManagerAgentProps, RMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROCESS_NEW_JOB_UNSUCCESSFUL_ALLOCATION_RULE,
				"handle jobs that were unsuccessfully allocated to the Servers",
				"handles the remaining jobs that couldn't be allocated");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final List<ClientJob> jobs = facts.get(JOBS);
		final Map<String, List<String>> allocation = facts.get(ALLOCATION);
		final int index = facts.get(RULE_SET_IDX);

		final List<String> allocatedJobs = allocation.values().stream()
				.flatMap(Collection::stream)
				.toList();

		jobs.stream()
				.filter(job -> !allocatedJobs.contains(job.getJobId()))
				.forEach(job -> {
					MDC.put(MDC_JOB_ID, job.getJobId());
					MDC.put(MDC_RULE_SET_ID, valueOf(index));
					logger.info("Job {} was not allocated.", job.getJobId());

					agentProps.getJobsToBeExecuted().add(job);
				});
	}

	@Override
	public AgentRule copy() {
		return new ProcessNotAllocatedServerJobsRule(controller);
	}

	@Override
	public String getAgentType() {
		return REGIONAL_MANAGER.getName();
	}
}
