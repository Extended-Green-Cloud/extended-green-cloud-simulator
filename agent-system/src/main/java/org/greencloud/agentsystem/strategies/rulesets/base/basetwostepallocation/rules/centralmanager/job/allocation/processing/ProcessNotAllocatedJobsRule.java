package org.greencloud.agentsystem.strategies.rulesets.base.basetwostepallocation.rules.centralmanager.job.allocation.processing;

import static java.lang.String.valueOf;
import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.ALLOCATION;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LOOK_FOR_JOB_EXECUTOR_HANDLE_FAILURE_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_NEW_JOB_UNSUCCESSFUL_ALLOCATION_RULE;
import static org.greencloud.commons.utils.facts.JobFactsFactory.constructFactsWithJob;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_TYPE;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_JOB_ID;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class ProcessNotAllocatedJobsRule extends AgentBasicRule<CentralManagerAgentProps, CMANode> {

	private static final Logger logger = getLogger(ProcessNotAllocatedJobsRule.class);

	public ProcessNotAllocatedJobsRule(
			final RulesController<CentralManagerAgentProps, CMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROCESS_NEW_JOB_UNSUCCESSFUL_ALLOCATION_RULE,
				"handle jobs that were unsuccessfully allocated",
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

					final RuleSetFacts failureFacts = constructFactsWithJob(index, job);
					failureFacts.put(RULE_TYPE, LOOK_FOR_JOB_EXECUTOR_HANDLE_FAILURE_RULE);
					controller.fire(failureFacts);
				});
	}

	@Override
	public AgentRule copy() {
		return new ProcessNotAllocatedJobsRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
