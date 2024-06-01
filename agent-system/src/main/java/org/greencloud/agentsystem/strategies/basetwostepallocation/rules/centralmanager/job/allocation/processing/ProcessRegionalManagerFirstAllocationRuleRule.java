package org.greencloud.agentsystem.strategies.basetwostepallocation.rules.centralmanager.job.allocation.processing;

import static java.lang.String.valueOf;
import static org.greencloud.agentsystem.strategies.algorithms.allocation.ResourceAllocator.allocate;
import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LOOK_FOR_JOB_EXECUTOR_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_NEW_JOB_ALLOCATION_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToJobWithServer;
import static org.greencloud.commons.utils.facts.JobAllocationFactsFactory.constructFactsForUnsuccessfulDataAllocationPreparation;
import static org.greencloud.commons.utils.facts.JobFactsFactory.constructFactsWithJobs;
import static org.jrba.rulesengine.constants.FactTypeConstants.AGENT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RESULT;
import static org.jrba.rulesengine.constants.FactTypeConstants.RULE_SET_IDX;
import static org.jrba.rulesengine.constants.LoggingConstants.MDC_RULE_SET_ID;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Map;

import org.greencloud.commons.args.agent.centralmanager.agent.CentralManagerAgentProps;
import org.greencloud.commons.domain.allocation.AllocationData;
import org.greencloud.commons.domain.job.basic.ClientJob;
import org.greencloud.commons.domain.job.basic.ClientJobWithServer;
import org.greencloud.gui.agents.centralmanager.CMANode;
import org.jrba.rulesengine.RulesController;
import org.jrba.rulesengine.behaviour.initiate.InitiateRequest;
import org.jrba.rulesengine.rule.AgentBasicRule;
import org.jrba.rulesengine.rule.AgentRule;
import org.jrba.rulesengine.rule.AgentRuleDescription;
import org.jrba.rulesengine.ruleset.RuleSetFacts;
import org.slf4j.Logger;
import org.slf4j.MDC;

import jade.core.AID;

public class ProcessRegionalManagerFirstAllocationRuleRule extends AgentBasicRule<CentralManagerAgentProps, CMANode> {

	private static final Logger logger = getLogger(ProcessRegionalManagerFirstAllocationRuleRule.class);

	public ProcessRegionalManagerFirstAllocationRuleRule(
			final RulesController<CentralManagerAgentProps, CMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROCESS_NEW_JOB_ALLOCATION_RULE,
				"handle next jobs allocation",
				"handles allocation of next job batch between RMAs without Server's allocation");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final List<ClientJob> jobs = facts.get(JOBS);

		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Performing the allocation of the jobs to the regions.");

		final AllocationData allocationData = facts.get(RESULT);
		final Map<String, List<String>> allocatedJobs = allocate(allocationData, agentProps);

		controller.fire(
				constructFactsForUnsuccessfulDataAllocationPreparation(facts.get(RULE_SET_IDX), allocatedJobs, jobs));
		allocatedJobs.forEach((rmaName, jobsAllocatedToRMA) ->
				initiateJobsAllocationRequest(facts, mapToJobs(jobsAllocatedToRMA, jobs), rmaName));
	}

	private void initiateJobsAllocationRequest(final RuleSetFacts facts, final List<ClientJobWithServer> jobs,
			final String rmaName) {
		final AID rma = agentProps.getRMAByName(rmaName);
		final RuleSetFacts allocationFacts = constructFactsWithJobs(facts.get(RULE_SET_IDX), jobs);
		allocationFacts.put(AGENT, rma);

		agent.addBehaviour(InitiateRequest.create(agent, allocationFacts, LOOK_FOR_JOB_EXECUTOR_RULE, controller));
	}

	private List<ClientJobWithServer> mapToJobs(final List<String> jobNames, final List<ClientJob> jobs) {
		return jobs.stream()
				.filter(job -> jobNames.contains(job.getJobId()))
				.map(job -> mapToJobWithServer(job, null))
				.toList();
	}

	@Override
	public AgentRule copy() {
		return new ProcessRegionalManagerFirstAllocationRuleRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
