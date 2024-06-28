package org.greencloud.agentsystem.strategies.rulesets.base.baseonestepallocation.rules.centralmanager.job.allocation.processing;

import static java.lang.String.valueOf;
import static java.util.Objects.requireNonNull;
import static org.greencloud.agentsystem.strategies.algorithms.allocation.ResourceAllocator.allocate;
import static org.greencloud.commons.args.agent.EGCSAgentType.CENTRAL_MANAGER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.ALLOCATION_TIMER;
import static org.greencloud.commons.constants.EGCSFactTypeConstants.JOBS;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.LOOK_FOR_JOB_EXECUTOR_RULE;
import static org.greencloud.commons.enums.rules.EGCSDefaultRuleType.PROCESS_NEW_JOB_ALLOCATION_RULE;
import static org.greencloud.commons.mapper.JobMapper.mapToJobWithServer;
import static org.greencloud.commons.utils.facts.JobAllocationFactsFactory.constructFactsForUnsuccessfulDataAllocationPreparation;
import static org.greencloud.commons.utils.facts.JobFactsFactory.constructFactsWithJobs;
import static org.greencloud.commons.utils.time.TimeSimulation.getCurrentTime;
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
import org.greencloud.commons.domain.timer.Timer;
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

public class ProcessRegionalManagerAllocationRule extends AgentBasicRule<CentralManagerAgentProps, CMANode> {

	private static final Logger logger = getLogger(ProcessRegionalManagerAllocationRule.class);

	public ProcessRegionalManagerAllocationRule(
			final RulesController<CentralManagerAgentProps, CMANode> controller) {
		super(controller);
	}

	@Override
	public AgentRuleDescription initializeRuleDescription() {
		return new AgentRuleDescription(PROCESS_NEW_JOB_ALLOCATION_RULE,
				"handle next jobs allocation",
				"handles allocation of next job batch between RMAs with Server's allocation");
	}

	@Override
	public void executeRule(final RuleSetFacts facts) {
		final List<ClientJob> jobs = facts.get(JOBS);
		final Timer allocationTimer = facts.get(ALLOCATION_TIMER);

		MDC.put(MDC_RULE_SET_ID, valueOf((int) facts.get(RULE_SET_IDX)));
		logger.info("Performing the allocation of the jobs to the servers.");

		final AllocationData allocationData = facts.get(RESULT);
		final Map<String, List<String>> allocatedJobs = allocate(allocationData, agentProps);
		final Map<String, List<String>> serversPerRMAs = requireNonNull(allocationData.getServersPerRMA());
		final long allocationTime = allocationTimer.stopTimeMeasure(getCurrentTime());

		agentNode.reportJobAllocationPercentage(jobs, allocatedJobs, allocationTime, agentProps.getAgentName());
		controller.fire(
				constructFactsForUnsuccessfulDataAllocationPreparation(facts.get(RULE_SET_IDX), allocatedJobs, jobs));
		allocatedJobs.forEach((server, jobsToAllocate) ->
				initiateJobsAllocationRequest(facts, mapToJobs(jobsToAllocate, jobs, server), server, serversPerRMAs));
	}

	private void initiateJobsAllocationRequest(final RuleSetFacts facts, final List<ClientJobWithServer> jobs,
			final String serverName, final Map<String, List<String>> serversPerRMAs) {
		final AID rma = selectRmaForServer(serversPerRMAs, serverName);
		final RuleSetFacts allocationFacts = constructFactsWithJobs(facts.get(RULE_SET_IDX), jobs);
		allocationFacts.put(AGENT, rma);

		agent.addBehaviour(InitiateRequest.create(agent, allocationFacts, LOOK_FOR_JOB_EXECUTOR_RULE, controller));
	}

	private AID selectRmaForServer(final Map<String, List<String>> serversPerRMAs, final String serverName) {
		return serversPerRMAs.entrySet().stream()
				.filter(entry -> entry.getValue().contains(serverName))
				.findFirst()
				.map(Map.Entry::getKey)
				.map(agentProps::getRMAByName)
				.orElseThrow();
	}

	private List<ClientJobWithServer> mapToJobs(final List<String> jobNames, final List<ClientJob> jobs,
			final String server) {
		return jobs.stream()
				.filter(job -> jobNames.contains(job.getJobId()))
				.map(job -> mapToJobWithServer(job, server))
				.toList();
	}

	@Override
	public AgentRule copy() {
		return new ProcessRegionalManagerAllocationRule(controller);
	}

	@Override
	public String getAgentType() {
		return CENTRAL_MANAGER.getName();
	}
}
